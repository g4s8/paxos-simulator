package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {
        final int nReplicas = 3;
        final AtomicInteger counter = new AtomicInteger();
        final ExecutorService accexec = Executors.newCachedThreadPool();


        Map<Integer, List<Acceptor<Decision>>> acceptors =
                Stream.generate(() -> new AtomicReference<>(Decision.NONE))
                .limit(nReplicas * (nReplicas + 1))
                .map(InMemoryAcceptor::new)
                //.map(a -> new DropAcceptor<>(0.2, a))
                //.map(a -> new TimeoutAcceptor<>(200, a))
                .map(a -> new AsyncAcceptor<>(accexec, a))
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / (1 + nReplicas)));

        TransactionManager tm = new StupidTransactionManager();

        List<StupidResourceManager<Decision>> replicas =
                IntStream.rangeClosed(1, nReplicas).mapToObj(
                id ->
                        new StupidResourceManager<>(
                                id,
                                new Proposer<>(
                                        id,
                                        acceptors.get(id-1)
                                ),
                                acceptors.get(id-1)
                        )
        ).collect(Collectors.toList());

        String uid = UUID.randomUUID().toString();
        tm.update(uid,0, 2, replicas);

        Executors.newScheduledThreadPool(5).schedule(
                () -> {
                    Decision decision = tm.sync(replicas);
                    if(decision.equals(Decision.COMMIT)) {
                        try {
                            tm.commit(uid, 0, 2, replicas);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        replicas.forEach(replica -> {
                            System.out.println(replica.id + " has new value: " + replica.storage.value());
                        });
                        accexec.shutdown();
                    }

                }, 500, TimeUnit.MILLISECONDS);

    }
}

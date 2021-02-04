package wtf.g4s8.examples.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import wtf.g4s8.examples.spaxos.*;
import wtf.g4s8.examples.system.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Builder
@AllArgsConstructor
public class TransactionTest {
    private final int nUpdaters;
    private final int nReplicas;
    private final ExecutorService accexec;
    private final boolean withDrops;
    private final double dropRate;
    private final boolean withTimeout;
    private final int timeout;
    private final boolean async;
    private final int syncDelay;


    public void test() throws Exception {
        writeConcurrent(
                nUpdaters,
                new StupidTransactionManager(
                        syncDelay,
                        resourceManagerCluster()
                )
        );
    }


    private void writeConcurrent(int nUpdaters, StupidTransactionManager tm) throws Exception {
        CountDownLatch cd = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(nUpdaters);
        ExecutorService exec = Executors.newCachedThreadPool();
        List<String> transactionsID =
                Stream.generate(UUID::randomUUID)
                .map(UUID::toString)
                .limit(nUpdaters)
                .collect(Collectors.toList());
        for (int i = 0; i < nUpdaters; i++) {
            final int val = i;
            final String uid = transactionsID.get(i);
            exec.submit(() -> {
                try {
                    cd.await();
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    return;
                }
                tm.update(uid,0, val+10);
                done.countDown();
            });
        }
        cd.countDown();
        done.await();
    }

    /**
     * Creates {@link #nReplicas} ResourceManagers with Paxos instance connected to TM and all RMs.
     */
    private List<ResourceManager> resourceManagerCluster() {
        Stream<ResourceManager> rmStream = IntStream.rangeClosed(1, nReplicas).mapToObj(
                StupidResourceManager::new
        );
        if (withDrops) {
            rmStream = rmStream
                    .map(a -> new DropResourceManager(dropRate, a));
        }
        if (withTimeout) {
            rmStream = rmStream
                    .map(a -> new TimeoutResourceManager(timeout, a));
        }
        if (async) {
            rmStream = rmStream
                    .map(a -> new AsyncResourceManager(accexec, a));
        }
        return rmStream.collect(Collectors.toList());

    }


    /**
     * Creates nReplicas * (nReplicas + 1) acceptors and groups them by belonging to Proposer.
     * Each ResourceManager (nReplicas) has an acceptor on every Replica, including itself. (nReplicas * nReplicas)
     * TransactionManager also has an acceptor for every ResourceManager. (nReplicas * (nReplicas + 1))
     */
    public static List<Acceptor<Decision>> acceptors(int serverId, String transactionId) {
        Stream<Acceptor<Decision>> acceptorStream = IntStream.rangeClosed(0, Config.nReplicas)
                .limit(Config.nReplicas + 1)
                .mapToObj(id -> new InMemoryAcceptor<>(new AtomicReference<>(Decision.NONE), serverId, id, transactionId));

        if (Config.withDrops) {
            acceptorStream = acceptorStream
                    .map(a -> new DropAcceptor<>(Config.dropRate, a));
        }
        if (Config.withTimeout) {
            acceptorStream = acceptorStream
                    .map(a -> new TimeoutAcceptor<>(Config.timeout, a));
        }
        if (Config.async) {
            acceptorStream = acceptorStream
                    .map(a -> new AsyncAcceptor<>(Config.accexec, a));
        }
        return acceptorStream
                .collect(
                        Collectors.toList()
                );
    }

}

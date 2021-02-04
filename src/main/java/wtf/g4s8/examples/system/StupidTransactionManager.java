package wtf.g4s8.examples.system;

import wtf.g4s8.examples.configuration.TransactionTest;
import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.spaxos.Proposer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StupidTransactionManager implements TransactionManager{

    private static final Random RNG = new Random();

    private final int timeout;
    private final List<ResourceManager> replicas;
    private final  ConcurrentMap<String, ConcurrentMap<Integer, List<Decision>>>  sync;
    private final int quorum;
    private final AtomicInteger repeat = new AtomicInteger(3);

    public StupidTransactionManager(int syncTimeOutInSeconds, List<ResourceManager> resourceManagers) {
        this.replicas = resourceManagers;
        this.timeout = syncTimeOutInSeconds;
        this.sync = new ConcurrentHashMap<>();
        //replicas.parallelStream().forEach(ac -> sync.put(ac.id(), Collections.synchronizedList(new ArrayList<>())));
        this.quorum = ((replicas.size() + 1)/ 2) + 1;
    }

    @Override
    public void update(String uid, int currentValue, int proposedValue)  {
        Patch patch = new Patch(uid, currentValue, proposedValue);

        for (ResourceManager rm : replicas) {
            rm.update(patch, TransactionTest.acceptors(rm.id(), uid));
        }
        final ScheduledExecutorService pool = Executors.newScheduledThreadPool(5);
        pool.schedule(() -> {
            sync(uid);
            pool.schedule(() -> {
                Decision decision = decision(uid);
                if(Decision.PREPARE.equals(decision)) {
                    System.out.printf("[%s] COMMITTING\n", patch.uid);
                    commit(patch.uid);
                    pool.schedule(() -> {
                        replicas.forEach(replica -> {
                            System.out.printf("RM-%s value: %s\n", replica.id(), replica.storage().value().toString());
                        });
                    }, timeout, TimeUnit.SECONDS);
                } else {
                    System.out.printf("[%s] ABORTING\n", patch.uid);
                    abort(patch.uid);
                    if (repeat.getAndDecrement() > 0) {
                        pool.schedule(() -> {
                            this.update(UUID.randomUUID().toString(), currentValue, proposedValue);
                        }, RNG.nextInt(20), TimeUnit.SECONDS);
                    } else {
                        System.out.printf("[%s] FAILED\n", patch.uid);
                    }
                }
            }, timeout, TimeUnit.SECONDS);
        }, timeout, TimeUnit.SECONDS);
    }

    private void abort(String transactionId) {
        for (ResourceManager rm : replicas) {
            rm.abort(transactionId);
        }
    }

    private void commit(String transactionId) {
        for (ResourceManager rm : replicas) {
            rm.commit(transactionId);
        }
    }

    private void sync(String transactionId) {
        sync.put(
                transactionId,
                new ConcurrentHashMap<>()
                );
        replicas.parallelStream().forEach(ac -> sync.get(transactionId).put(ac.id(), Collections.synchronizedList(new ArrayList<>())));
        replicas.parallelStream().forEach(r -> {
            r.storage().activeAcceptors().getOrDefault(transactionId, new ArrayList<>()).forEach(a -> a.requestValue(value -> {
                if (!Decision.NONE.equals(value)) {
                    System.out.printf("[%s] VOTE RECEIVED: %s\n", transactionId, value);
                    sync.get(transactionId).get(r.id()).add(value);
                }
            }));
        });
    }

    private Decision decision(String uid) {
        return sync.get(uid).values().stream().anyMatch(rmAcceptors ->
                rmAcceptors.stream()
                        .filter(d -> d.equals(Decision.ABORT))
                        .count() >= quorum)
                ? Decision.ABORT :
                sync.get(uid).values().stream().allMatch(rmAcceptors ->
                        rmAcceptors.stream()
                                .filter(d -> d.equals(Decision.PREPARE))
                                .count() >= quorum)
                        ? Decision.PREPARE : Decision.NONE;
    }
}






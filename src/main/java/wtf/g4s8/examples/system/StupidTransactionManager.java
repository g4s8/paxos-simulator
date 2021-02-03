package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Proposer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class StupidTransactionManager implements TransactionManager{

    private final int timeout;
    private final List<ResourceManager> replicas;
    private final ConcurrentMap<Integer, List<Decision>> sync;
    private final int quorum;

    public StupidTransactionManager(int syncTimeOutInSeconds, List<ResourceManager> resourceManagers) {
        this.replicas = resourceManagers;
        this.timeout = syncTimeOutInSeconds;
        this.sync = new ConcurrentHashMap<>();
        replicas.parallelStream().forEach(ac -> sync.put(ac.id(), Collections.synchronizedList(new ArrayList<>())));
        this.quorum = ((replicas.size() + 1)/ 2) + 1;
    }

    @Override
    public void update(String uid, int currentValue, int proposedValue)  {
        Patch patch = new Patch(uid, currentValue, proposedValue);

        for (ResourceManager rm : replicas) {
            rm.update(patch);
        }
        final ScheduledExecutorService pool = Executors.newScheduledThreadPool(5);
        pool.schedule(() -> {
            Proposer.EXEC_TIMEOUT.shutdown();
            sync();
            pool.schedule(() -> {
                if(Decision.PREPARE.equals(decision())) {
                    commit(patch.uid);
                    pool.schedule(() -> {
                        System.out.println("COMMITTED");
                        replicas.forEach(replica -> {
                            System.out.printf("RM-%s has new value: %s\n", replica.id(), replica.storage().value().toString());
                        });
                        System.exit(0);
                    }, timeout, TimeUnit.SECONDS);
                } else {
                    System.out.println("TODO --");
                    System.exit(0);
                }
            }, timeout, TimeUnit.SECONDS);
        }, timeout, TimeUnit.SECONDS);
    }

    private void commit(String transactionId) {
        for (ResourceManager rm : replicas) {
            rm.commit(transactionId);
        }
    }

    private void sync() {
        replicas.parallelStream().forEach(r -> {
            r.acceptors().forEach(a -> a.requestValue(value -> {
                if (!Decision.NONE.equals(value)) {
                    System.out.println("SENDING VOTE:" + value);
                    sync.get(r.id()).add(value);
                }
            }));
        });
    }

    private Decision decision() {
        return sync.values().stream().anyMatch(rmAcceptors ->
                rmAcceptors.stream()
                        .filter(d -> d.equals(Decision.PREPARE))
                        .count() < quorum)
                ? Decision.ABORT : Decision.PREPARE;
    }
}






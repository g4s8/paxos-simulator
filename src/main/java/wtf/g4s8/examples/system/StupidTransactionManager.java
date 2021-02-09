package wtf.g4s8.examples.system;

import wtf.g4s8.examples.configuration.Config;
import wtf.g4s8.examples.configuration.TransactionTest;
import wtf.g4s8.examples.*;

import java.util.*;
import java.util.concurrent.*;

public class StupidTransactionManager implements TransactionManager{

    public final static ScheduledExecutorService POOL = Executors.newScheduledThreadPool(Config.nUpdaters * Config.nReplicas);
    private static final Random RNG = new Random();
    private final Log.Logger log;

    private final int timeout;
    private final List<ResourceManager> replicas;
    private final  ConcurrentMap<String, ConcurrentMap<Integer, List<Decision>>>  sync;
    private final int quorum;

    public StupidTransactionManager(int syncTimeOutInSeconds, List<ResourceManager> resourceManagers) {
        this.replicas = resourceManagers;
        this.timeout = syncTimeOutInSeconds;
        this.sync = new ConcurrentHashMap<>();
        this.quorum = ((replicas.size() + 1)/ 2) + 1;
        this.log = Log.logger(this);
    }

    @Override
    public void update(String uid, int currentValue, int proposedValue)  {
        update(uid, currentValue, proposedValue, 0);
    }

    public void update(String uid, int currentValue, int proposedValue, int nTries)  {
        Patch patch = new Patch(uid, currentValue, proposedValue);

        for (ResourceManager rm : replicas) {
            rm.update(patch, TransactionTest.acceptors(rm.id(), uid));
        }
        POOL.schedule(() -> {
            sync(uid);
            POOL.schedule(() -> {
                Decision decision = decision(uid);
                if(Decision.PREPARE.equals(decision)) {
                    log.logf("commiting patch `%s`", patch.uid);
                    commit(patch.uid);
                    TransactionTest.done.countDown();
                } else {
                    log.logf("aborting patch `%s`", patch.uid);
                    abort(patch.uid);
                    if (nTries < Config.nRetries) {
                        POOL.schedule(() -> {
                            this.update(String.valueOf(TransactionTest.transactionId.getAndIncrement()), currentValue, proposedValue, nTries + 1);
                        }, RNG.nextInt(Config.retryUpdateMaxTimeOutInSeconds - Config.retryUpdateMinTimeOutInSeconds) + Config.retryUpdateMinTimeOutInSeconds, TimeUnit.SECONDS);
                    } else {
                        log.logf("failed to update from %s to %s", patch.lastKnownValue, patch.newValue);
                        TransactionTest.done.countDown();
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
                    log.logf("txn `%s` received a vote: `%s`", transactionId, value);
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

    public String toString() {
        return "TM";
    }
}






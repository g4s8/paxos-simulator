package wtf.g4s8.examples.system;


import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.spaxos.Proposer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class StupidTransactionManager implements TransactionManager{

    @Override
    public void update(String uid, int currentValue, int proposedValue, List<? extends ResourceManager> replicas) throws Exception {

        CountDownLatch cd = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(replicas.size());
        ExecutorService exec = Executors.newCachedThreadPool();

        Patch patch = new Patch(uid, currentValue, proposedValue, replicas);

        for (ResourceManager rm : replicas) {
            exec.submit(() -> {
                try {
                    cd.await();
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    System.out.println("WTF" + rm.update(patch).get());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                done.countDown();
            });
        }
        cd.countDown();
        done.await();
        exec.shutdown();
        Proposer.EXEC_TIMEOUT.shutdown();
    }

    @Override
    public void commit(String uid, int currentValue, int proposedValue, List<? extends ResourceManager> replicas) throws Exception {
        CountDownLatch cd = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(replicas.size());
        ExecutorService exec = Executors.newCachedThreadPool();

        Patch patch = new Patch(uid, currentValue, proposedValue, replicas);

        for (ResourceManager rm : replicas) {
            exec.submit(() -> {
                try {
                    cd.await();
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    rm.commit(patch);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                done.countDown();
            });
        }
        cd.countDown();
        done.await();
        exec.shutdown();


    }

    @Override
    public Decision sync(List<StupidResourceManager<Decision>> replicas) {
        ConcurrentMap<Integer, CopyOnWriteArrayList<Decision>> sync = new ConcurrentHashMap<>();

        replicas.parallelStream().forEach(ac -> sync.put(ac.id, new CopyOnWriteArrayList<>()));

        replicas.parallelStream().forEach(r -> {
            r.accs.forEach(a -> a.requestValue(value -> {
                sync.get(r.id).add(value);
            }));
        });


        return sync.values().stream().anyMatch(rmAcceptors ->
                rmAcceptors.stream().filter(d ->
                        d.equals(Decision.COMMIT)).count() < ((rmAcceptors.size() / 2) + 1)) ? Decision.ABORT : Decision.COMMIT;
    }

}






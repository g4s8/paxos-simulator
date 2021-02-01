package wtf.g4s8.examples.system;


import wtf.g4s8.examples.spaxos.AsyncAcceptor;
import wtf.g4s8.examples.spaxos.Proposer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                    //System.out.printf("[" + Thread.currentThread().getName() + "]" + "proposed %d get %s\n", val, res);
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
                    //System.out.printf("[" + Thread.currentThread().getName() + "]" + "proposed %d get %s\n", val, res);
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
    public Decision sync(Map<Integer, List<AsyncAcceptor<Decision>>> acceptors) {
        return acceptors.values().stream().anyMatch(rmAcceptors ->
                rmAcceptors.stream().filter(acceptor ->
                        acceptor.getDecision().equals(Decision.COMMIT)).count() < ((rmAcceptors.size() / 2) + 1)) ? Decision.ABORT : Decision.COMMIT;
        //return acceptors.get(0).stream().map(Acceptor::getDecision).allMatch(decision -> decision.equals(Decision.COMMIT)) ? Decision.COMMIT : Decision.ABORT;
    }

}






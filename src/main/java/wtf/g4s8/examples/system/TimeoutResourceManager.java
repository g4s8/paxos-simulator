package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.system.storage.Storage;

import java.util.List;
import java.util.Random;

public class TimeoutResourceManager implements ResourceManager {

    private static final Random RNG = new Random();

    private final int toms;
    private final ResourceManager origin;

    public TimeoutResourceManager(int timeout, ResourceManager origin) {
        this.toms = timeout;
        this.origin = origin;
    }

    @Override
    public void prepare(Patch patch, List<Acceptor<Decision>> acceptors) {
        sleep();
        this.origin.prepare(patch, acceptors);
    }

    @Override
    public void commit(String transactionId) {
        sleep();
        this.origin.commit(transactionId);
    }

    @Override
    public void abort(String transactionId) {
        sleep();
        this.origin.abort(transactionId);
    }

    @Override
    public Integer id() {
        return this.origin.id();
    }

    @Override
    public List<Acceptor<Decision>> acceptors(String transactionId) {
        return this.origin.acceptors(transactionId);
    }

    @Override
    public Storage storage() {
        return this.origin.storage();
    }

    private void sleep() {
        try {
            Thread.sleep(RNG.nextInt(toms));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

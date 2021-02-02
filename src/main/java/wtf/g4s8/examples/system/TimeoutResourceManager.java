package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;

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
    public void update(Patch patch) {
        sleep();
        this.origin.update(patch);

    }

    @Override
    public void commit(Patch patch) {
        sleep();
        this.origin.commit(patch);
    }

    @Override
    public Integer id() {
        return this.origin.id();
    }

    @Override
    public List<Acceptor<Decision>> acceptors() {
        return this.origin.acceptors();
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

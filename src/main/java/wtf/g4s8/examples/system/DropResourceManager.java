package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;

import java.util.List;
import java.util.Random;

public class DropResourceManager implements ResourceManager {

    private static final Random RNG = new Random();

    private final double prob;
    private final ResourceManager origin;

    public DropResourceManager(double dropRate, ResourceManager origin) {
        this.prob = dropRate;
        this.origin = origin;
    }

    @Override
    public void update(Patch patch) {
        if (alive()) {
            this.origin.update(patch);
        }
    }

    @Override
    public void commit(Patch patch) {
        if (alive()) {
            this.origin.commit(patch);
        }
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

    private boolean alive() {
        return !(RNG.nextDouble() < prob);
    }
}

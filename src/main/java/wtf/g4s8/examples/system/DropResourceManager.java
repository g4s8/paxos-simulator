package wtf.g4s8.examples.system;

import wtf.g4s8.examples.configuration.TransactionTest;
import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.system.storage.Storage;

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
    public void update(Patch patch, List<Acceptor<Decision>> acceptors) {
        if (alive()) {
            this.origin.update(patch, acceptors);
        }
    }

    @Override
    public void commit(String transactionId) {
        if (alive()) {
            this.origin.commit(transactionId);
        }
    }

    @Override
    public void abort(String transactionId) {
        if (alive()) {
            this.origin.abort(transactionId);
        }
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

    private boolean alive() {
        return !(RNG.nextDouble() < prob);
    }
}

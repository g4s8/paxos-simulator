package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;

import java.util.List;
import java.util.concurrent.Executor;

public class AsyncResourceManager implements ResourceManager {

    private final Executor exec;
    private final ResourceManager origin;


    public AsyncResourceManager(Executor exec, ResourceManager origin) {
        this.exec = exec;
        this.origin = origin;
    }

    @Override
    public void update(Patch patch) {
        this.exec.execute(() -> this.origin.update(patch));
    }

    @Override
    public void commit(String transactionId) {
        this.exec.execute(() -> this.origin.commit(transactionId));
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
}

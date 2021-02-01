package wtf.g4s8.examples.spaxos;

import wtf.g4s8.examples.system.Decision;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncAcceptor<T> implements Acceptor<T> {

    private final Executor exec;
    private final Acceptor<T> origin;

    public AsyncAcceptor(Executor exec, Acceptor<T> origin) {
        this.exec = exec;
        this.origin = origin;
    }

    @Override
    public void prepare(Proposal prop, PrepareCallback<T> callback) {
        this.exec.execute(() -> this.origin.prepare(prop, new PrepareCallback<T>() {
            @Override
            public void promise(Proposal prop) {
                exec.execute(() -> callback.promise(prop));
            }

            @Override
            public void promise(Proposal prop, T val) {
                exec.execute(() -> callback.promise(prop, val));
            }
        }));
    }

    @Override
    public void accept(Proposal prop, T value, AcceptCallback<T> callback) {
        this.exec.execute(() -> this.origin.accept(prop, value, new AcceptCallback<T>() {
            @Override
            public void accepted(Proposal prop, T value) {
                exec.execute(() -> callback.accepted(prop, value));
            }
        }));
    }

    @Override
    public T getDecision() {
        CompletableFuture<T> d = new CompletableFuture<>();
        this.exec.execute(() -> d.complete(this.origin.getDecision()));
        try {
            return d.get();
        } catch (Exception e) {
            return (T) Decision.UNKNOWN;
        }
    }
}

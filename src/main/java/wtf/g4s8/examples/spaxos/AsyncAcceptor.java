package wtf.g4s8.examples.spaxos;

import java.util.concurrent.Executor;

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
}

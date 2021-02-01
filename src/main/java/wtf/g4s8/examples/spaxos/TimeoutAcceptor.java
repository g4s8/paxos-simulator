package wtf.g4s8.examples.spaxos;

import wtf.g4s8.examples.system.Sync;

import java.util.Random;

public class TimeoutAcceptor<T> implements Acceptor<T> {
    private static final Random RNG = new Random();

    private final int toms;
    private final Acceptor<T> acc;

    public TimeoutAcceptor(int toms, Acceptor<T> acc) {
        this.toms = toms;
        this.acc = acc;
    }

    @Override
    public void prepare(Proposal prop, PrepareCallback<T> callback) {
        sleep();
        this.acc.prepare(prop, new PrepareCallback<T>() {
            @Override
            public void promise(Proposal prop) {
                sleep();
                callback.promise(prop);
            }

            @Override
            public void promise(Proposal prop, T val) {
                sleep();
                callback.promise(prop, val);
            }
        });
    }

    @Override
    public void accept(Proposal prop, T value, AcceptCallback<T> callback) {
        sleep();
        this.acc.accept(prop, value, new AcceptCallback<T>() {
            @Override
            public void accepted(Proposal prop, T value) {
                sleep();
                callback.accepted(prop, value);
            }
        });
    }

    @Override
    public void requestValue(Sync.Receiver<T> callback) {
        sleep();
        acc.requestValue(callback);
    }

    private void sleep() {
        try {
            Thread.sleep(RNG.nextInt(toms));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

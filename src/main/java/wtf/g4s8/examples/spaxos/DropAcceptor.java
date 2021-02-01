package wtf.g4s8.examples.spaxos;

import wtf.g4s8.examples.system.Decision;

import java.util.Random;

public class DropAcceptor<T> implements Acceptor<T> {

    private static final Random RNG = new Random();

    private final double prob;
    private final Acceptor<T> acc;

    public DropAcceptor(double prob, Acceptor<T> acc) {
        this.prob = prob;
        this.acc = acc;
    }

    @Override
    public void prepare(Proposal prop, PrepareCallback<T> callback) {
        if (!drop()) {
            this.acc.prepare(prop, new PrepareCallback<T>() {
                @Override
                public void promise(Proposal prop) {
                    if (!drop()) {
                        callback.promise(prop);
                    }
                }

                @Override
                public void promise(Proposal prop, T val) {
                    if (!drop()) {
                        callback.promise(prop, val);
                    }
                }
            });
        }
    }

    @Override
    public void accept(Proposal prop, T value, AcceptCallback<T> callback) {
        if (!drop()) {
            this.acc.accept(prop, value, new AcceptCallback<T>() {
                @Override
                public void accepted(Proposal prop, T value) {
                    if (!drop()) {
                        callback.accepted(prop, value);
                    }
                }
            });
        }
    }

    @Override
    public T getDecision() {
        if (!drop()) {
            return acc.getDecision();
        }
        return (T) Decision.UNKNOWN;
    }

    private boolean drop() {
        return RNG.nextDouble() < prob;
    }
}

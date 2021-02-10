package wtf.g4s8.examples.spaxos;

import wtf.g4s8.examples.system.Sync;

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
                public void promise(Proposal prop, String metadata) {
                    if (!drop()) {
                        callback.promise(prop, metadata);
                    }
                }

                @Override
                public void promise(Proposal prop, T val, String metadata) {
                    if (!drop()) {
                        callback.promise(prop, val, metadata);
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
                public void accepted(Proposal prop, T value, String metadata) {
                    if (!drop()) {
                        callback.accepted(prop, value, metadata);
                    }
                }
            });
        }
    }

    @Override
    public void requestValue(Sync.Receiver<T> callback) {
        if (!drop()) {
            acc.requestValue(new Sync.Receiver<T>(){
                @Override
                public void receive(T value, String metadata) {
                    if (!drop()) {
                        callback.receive(value, metadata);
                    }
                }
            });
        }
    }

    private boolean drop() {
        return RNG.nextDouble() < prob;
    }
}

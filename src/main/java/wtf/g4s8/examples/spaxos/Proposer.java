/*
 * MIT License
 *
 * Copyright (c) 2020 Kirill
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package wtf.g4s8.examples.spaxos;

import wtf.g4s8.examples.Log;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Paxos proposer.
 * <p>
 * The logic of proposer on proposing new value is follow:
 * <ol>
 * <li>It choses next proposal number by incrementing proposal.</li>
 * <li>It sends prepare request to quorum of live acceptors.</li>
 * <li>It checks the result of prepare, if any promise has accepted value,
 * greater than proposal number, then proposer changes inital value with
 * accepted value returned from acceptor.</li>
 * <li>It sends accept request to quorum of live acceptors using origin
 * proposal number and maybe updated value.</li>
 * <li>If any acceptor rejected accept request and returned proposal greater than
 * origin proposal, then proposer updates self proposal number with new proposal
 * and tries to propose possible updated value using next proposal again.</li>
 * </p>
 *
 * @since 1.0
 */
public final class Proposer<T> {
    private static final Random RNG = new Random();
    public static final ScheduledExecutorService EXEC_TIMEOUT = Executors.newScheduledThreadPool(5);

    /**
     * Proposer id for logging.
     */

    /**
     * Current proposal.
     */
    private Proposal prop;

    /**
     * List of acceptors. (assuming all aceptors are alive now).
     */
    private final List<? extends Acceptor<T>> acceptors;

    private final CompletableFuture<T> future;

    /**
     * New proposer with server id and list of acceptors.
     */
    public Proposer(final int server, final List<? extends Acceptor<T>> acc) {
        this(Proposal.init(server), acc, new CompletableFuture<>());
    }

    private Proposer(final Proposal prop, final List<? extends Acceptor<T>> acceptors, final CompletableFuture<T> future) {
        this.prop = prop;
        this.acceptors = acceptors;
        this.future = future;
    }

    public Future<T> propose(final T value) {
        final Proposal next = this.prop.next();
        Log.d("proposing %s : %s", next, value);
        final QuorumPrepared<T> callback = new QuorumPrepared<>(next, value, this.acceptors, this);
        this.acceptors.parallelStream().forEach(
                acc -> acc.prepare(next, callback)
        );
        EXEC_TIMEOUT.schedule(callback::timeout, 300 + RNG.nextInt(50), TimeUnit.MILLISECONDS);
        return this.future;
    }

    private Proposer<T> restart(Proposal prop) {
        return new Proposer<>(prop, this.acceptors, this.future);
    }


    private static final class QuorumPrepared<T> implements Acceptor.PrepareCallback<T> {

        private final List<? extends Acceptor<T>> acceptors;
        private final Proposal prop;
        private final Proposer<T> proposer;

        private volatile Proposal max;
        private volatile boolean done;
        private volatile T value;
        private volatile int cnt;

        private QuorumPrepared(final Proposal prop, T value, List<? extends Acceptor<T>> acceptors, Proposer<T> proposer) {
            this.value = value;
            this.acceptors = acceptors;
            this.max = prop;
            this.prop = prop;
            this.proposer = proposer;
        }

        @Override
        public synchronized void promise(Proposal prop) {
            if (!done) {
                Log.d("promise %s, cnt=%d", prop, this.cnt + 1);
            }
            this.cnt++;
            next(false);
        }

        @Override
        public synchronized void promise(final Proposal prop, final T value) {
            if (prop.compareTo(this.max) > 0) {
                this.max = prop;
                this.value = value;
                Log.d("prepare reject %s %s", prop, value);
            }
            next(false);
        }

        void timeout() {
            Log.d("prepare timeout %s %s", prop, value);
            next(true);
        }

        private synchronized void next(boolean force) {
            if (this.done) {
                return;
            }

            final int quorum = this.acceptors.size() / 2 + 1;
            if (this.cnt >= quorum) {
                this.done = true;
                Log.d("prepared by %d acceptors, sending accept %s : %s", quorum, this.prop, this.value);
                final AcceptCallback<T> callback = new AcceptCallback<>(
                        this.proposer, this.prop, quorum, this.value
                );
                EXEC_TIMEOUT.schedule(callback::timeout, 300 + RNG.nextInt(50), TimeUnit.MILLISECONDS);
                this.acceptors.parallelStream().forEach(
                        acc -> acc.accept(this.prop, this.value, callback)
                );
            } else if (force) {
                this.done = true;
                Log.d("prepared restart by timeout", quorum, this.prop, this.value);
                this.proposer.restart(this.prop).propose(this.value);
            }
        }
    }

    private static final class AcceptCallback<T> implements Acceptor.AcceptCallback<T> {

        private final Proposer<T> proposer;
        private final Proposal prop;
        private final int quorum;
        private final T val;

        private volatile int cnt;
        private volatile boolean done;
        private volatile Proposal max;

        private AcceptCallback(Proposer<T> proposer, Proposal prop, int quorum, T val) {
            this.proposer = proposer;
            this.prop = prop;
            this.quorum = quorum;
            this.max = prop;
            this.val = val;
        }

        @Override
        public synchronized void accepted(Proposal prop, T value) {
            Log.d("accepted %s : %s", prop, value);
            this.cnt++;
            next(false);
        }

        void timeout() {
            next(true);
        }

        private synchronized void next(boolean force) {
            if (this.done) {
                return;
            }
            if (this.cnt < this.quorum && !force) {
                return;
            }
            this.done = true;
            final boolean rejected = this.cnt < quorum;
            if (rejected) {
                Log.d("propose rejected, restarting");
                this.proposer.restart(this.prop.update(this.max)).propose(this.val);
            } else {
                Log.d("propose completed");
                this.proposer.future.complete(this.val);
            }
        }
    }
}

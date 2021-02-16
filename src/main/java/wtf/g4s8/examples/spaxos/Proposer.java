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
import wtf.g4s8.examples.configuration.Config;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static wtf.g4s8.examples.configuration.Config.cfg;

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
    private final String transactionId;
    private Boolean crashed;
    private static final Random RNG = new Random();
    public static final ScheduledExecutorService EXEC_TIMEOUT = Executors.newScheduledThreadPool(5);
    private final Log.Logger log;

    /**
     * Proposer id for logging.
     */

    private int serverId;
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
    public Proposer(final int server, final String transactionId, final List<? extends Acceptor<T>> acc) {
        this(server, Proposal.init(server), transactionId, acc, new CompletableFuture<>(), false);
    }

    private Proposer(final int serverId, final Proposal prop, final String transactionId, final List<? extends Acceptor<T>> acceptors, final CompletableFuture<T> future, Boolean crashed) {
        this.serverId = serverId;
        this.prop = prop;
        this.acceptors = acceptors;
        this.future = future;
        this.transactionId = transactionId;
        this.crashed = crashed;
        this.log = Log.logger(this);
    }

    public Future<T> propose(final T value) {
        if (crashed) {
            return null;
        }
        final Proposal next = this.prop.next();
        log.logf("propose %s `%s`", next, value);
        final QuorumPrepared<T> callback = new QuorumPrepared<>(next, value, this.acceptors, this);
        this.acceptors.parallelStream().forEach(
                acc -> acc.prepare(next, callback)
        );
        EXEC_TIMEOUT.schedule(callback::timeout, cfg.paxosProposerTimeOutMilliseconds + RNG.nextInt(cfg.paxosProposerTimeOutMilliseconds /6), TimeUnit.MILLISECONDS);
        return this.future;
    }

    private Proposer<T> restart(Proposal prop) {
        return new Proposer<>(this.serverId, prop, this.transactionId, this.acceptors, this.future, this.crashed);
    }

    public synchronized void kill() {
        this.crashed = true;
    }

    public synchronized boolean isDead() {
        return this.crashed;
    }

    @Override
    public String toString() {
        return String.format("proposer-(txn:%s, s:%s)", this.transactionId, this.serverId);
    }


    private static final class QuorumPrepared<T> implements Acceptor.PrepareCallback<T> {

        private final List<? extends Acceptor<T>> acceptors;
        private final Proposal prop;
        private final Proposer<T> proposer;
        private final Log.Logger log;

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
            this.log = Log.logger(proposer);
        }

        @Override
        public synchronized void promise(Proposal prop, String metadata) {
            if (this.proposer.isDead()) {
                return;
            }
            if (!done) {
                log.logf("promise for %s received from %s, cnt=%d",  prop, metadata, this.cnt + 1);
            }
            this.cnt++;
            next(false);
        }

        @Override
        public synchronized void promise(final Proposal prop, final T value, String metadata) {
            if (this.proposer.isDead()) {
                return;
            }
            if (prop.compareTo(this.max) > 0) {
                this.max = prop;
                this.value = value;
                log.logf("prepare rejected by %s. Already accepted value `%s` from higher proposer `%s`", metadata, value, prop);
            }
            else if (!this.value.equals(value)) {
                if (!this.done) {
                    log.logf("proposed value: `%s` can't be accepted by %s. Value: `%s` is already accepted from %s", this.value, metadata, value, prop);
                    this.done = true;
                    log.logf("restarting %s with other value: %s", this.prop, value);
                    this.proposer.restart(this.prop.update(this.max)).propose(value);
                }
            } else {
                log.logf("promise for %s received from %s, cnt=%d",  prop, metadata, this.cnt + 1);
                this.cnt++;
            }
            next(false);
        }

        void timeout() {
            if (this.done || this.proposer.isDead()) {
                return;
            }
            log.logf("prepare timeout %s %s", prop, value);
            next(true);
        }

        private synchronized void next(boolean force) {
            if (this.done || this.proposer.isDead()) {
                return;
            }

            final int quorum = this.acceptors.size() / 2 + 1;
            if (this.cnt >= quorum) {
                this.done = true;
                log.logf("prepared by %d acceptors, sending accept %s `%s`", quorum, this.prop, this.value);
                final AcceptCallback<T> callback = new AcceptCallback<>(
                        this.proposer, this.prop, quorum, this.value
                );
                EXEC_TIMEOUT.schedule(callback::timeout, 3000 + RNG.nextInt(500), TimeUnit.MILLISECONDS);
                this.acceptors.parallelStream().forEach(
                        acc -> acc.accept(this.prop, this.value, callback)
                );
            } else if (force) {
                this.done = true;
                log.logf("prepared restart by timeout %s: %s", this.prop, this.value);
                this.proposer.restart(this.prop).propose(this.value);
            }
        }
    }

    private static final class AcceptCallback<T> implements Acceptor.AcceptCallback<T> {

        private final Proposer<T> proposer;
        private final Proposal prop;
        private final int quorum;
        private final T val;
        private final Log.Logger log;

        private volatile int cnt;
        private volatile boolean done;
        private volatile Proposal max;

        private AcceptCallback(Proposer<T> proposer, Proposal prop, int quorum, T val) {
            this.proposer = proposer;
            this.prop = prop;
            this.quorum = quorum;
            this.max = prop;
            this.val = val;
            this.log = Log.logger(proposer);
        }

        @Override
        public synchronized void accepted(Proposal prop, T value, String metadata) {
            if (this.proposer.isDead()) {
                return;
            }
            log.logf("%s accepted %s `%s`",metadata, prop, value);
            this.cnt++;
            next(false);
        }

        void timeout() {
            next(true);
        }

        private synchronized void next(boolean force) {
            if (this.done || this.proposer.isDead()) {
                return;
            }
            if (this.cnt < this.quorum && !force) {
                return;
            }
            this.done = true;
            final boolean rejected = this.cnt < quorum;
            if (rejected) {
                log.logf("propose rejected, restarting %s", this.prop);
                this.proposer.restart(this.prop.update(this.max)).propose(this.val);
            } else {
                log.logf("propose completed %s", this.prop);
                this.proposer.future.complete(this.val);
            }
        }
    }
}

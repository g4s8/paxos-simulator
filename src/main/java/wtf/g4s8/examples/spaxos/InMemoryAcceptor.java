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

import wtf.g4s8.examples.system.Sync;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Acceptor implementation which stores values in memory.
 * See more details in {@link Acceptor} docs.
 *
 * @since 1.0
 */
public final class InMemoryAcceptor<T> implements Acceptor<T> {

    /**
     * Memory storage.
     */
    private final AtomicReference<T> mem;
    private final int proposerServerId;
    private final int parentServerId;
    private final String transactionId;

    /**
     * Minimal proposal.
     */
    private volatile Proposal min = Proposal.ZERO;

    /**
     * Accepted proposal.
     */
    private volatile Proposal acc = Proposal.ZERO;

    /**
     * New in memory acceptor with provided memory.
     */
    public InMemoryAcceptor(final AtomicReference<T> mem, int proposerServerId, int parentServerId, String transactionId) {
        this.mem = mem;
        this.proposerServerId = proposerServerId;
        this.parentServerId = parentServerId;
        this.transactionId = transactionId;
    }

    @Override
    public synchronized void prepare(final Proposal prop, final PrepareCallback<T> callback) {
        if (this.min.compareTo(prop) > 0) {
            return;
        }
        if (this.acc.compareTo(Proposal.ZERO) > 0) {
            callback.promise(this.acc, mem.get());
        } else {
            this.min = prop;
            callback.promise(prop);
        }
    }

    @Override
    public synchronized void accept(final Proposal prop, final T value, final AcceptCallback<T> callback) {
        if (prop.compareTo(this.min) >= 0) {
            this.acc = this.min = prop;
            this.mem.set(value);
            System.out.printf("[%s] ACCEPTOR(for %d on %d) accepting %s %s\n", transactionId, proposerServerId, parentServerId, prop, value);
            callback.accepted(prop, value);
        }
    }

    @Override
    public void requestValue(Sync.Receiver<T> callback) {
        final T value = mem.get();
        if (value != null) {
            System.out.printf("[%s] ACCEPTOR(for %d on %d) SENDING VOTE: %s\n", transactionId, proposerServerId, parentServerId, value);
            callback.receive(value);
        }
    }
}

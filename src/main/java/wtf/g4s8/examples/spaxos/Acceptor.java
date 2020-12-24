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

import java.util.Comparator;

/**
 * Paxos acceptor.
 * <p>
 * The therminology:
 * <ul>
 * <li>minimal proposal - a proposal number for which the acceptor promised
 * to not accept any proposal less than minimal. It can be updated by any proposal
 * greater than minimal in prepare or accept phases.</li>
 * <li>accepted proposal - a proposal number which was accepted by acceptor,
 * it's always returned in prepare phase and can be changed in accept phase
 * by proposal greater that minimal proposal.</li>
 * <li>accepted value - a value accepted by proposal in accept phase,
 * this value is always returned by prepare phase and can be changed in accept
 * phase with proposal greater than minimal proposal.</li>
 * </ul>
 * @since 1.0
 */
public interface Acceptor<T> {

    /**
     * Prepare - first phase of accepting value.
     * <p>
     * Acceptor checks if new proposal is greater than
     * minimal proposal stored by acceptor. If yes,
     * acceptor updates minimal proposal number to promise
     * not accept new proposals less than proposed number.
     * Otherwise, acceptor returns accepted proposal number with
     * accepted value.
     * </p>
     */
    Acceptor.Promise<T> prepare(Proposal prop);

    /**
     * Accept - second phase of accepting value.
     * <p>
     * Acceptor accepts proposal equals or greater than minimal
     * proposed value, in that case it updates accepted proposal and
     * accepted value. In any case it returns minimal proposed number
     * as a response.
     */
    Proposal accept(Proposal prop, T value);

    /**
     * A promise to not accept a value with fewer proposal number than
     * accepted.
     */
    final class Promise<T> {

        /**
         * Comparator of accepted result by proposal.
         */
        public static final Comparator<Promise<?>> CMP_BY_PROPOSAL =
            (left, right) -> left.prop.compareTo(right.prop);

        /**
         * Proposal.
         */
        private final Proposal prop;

        /**
         * Value accepted.
         */
        private final T val;

        public Promise(final Proposal prop, final T value) {
            this.prop = prop;
            this.val = value;
        }

        /**
         * Accepted value.
         */
        public T value() {
            return this.val;
        }

        public boolean isGreater(final Proposal prop) {
            return this.prop.compareTo(prop) > 0;
        }
    }
}

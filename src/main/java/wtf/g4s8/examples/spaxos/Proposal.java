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

/**
 * A proposal is a number which proposer uses to identify the request,
 * it contains sequentially incremented ballot number and unique server id.
 * <p>
 * The proposal is compared by comparing ballot number first and server id second.
 * This class is immutable.
 * </p>
 * @since 1.0
 */
public final class Proposal implements Comparable<Proposal> {

    /**
     * Zero proposal without server.
     */
    public static final Proposal ZERO = new Proposal(0, 0);

    /**
     * Ballot number.
     */
    private final int number;

    /**
     * Server ID.
     */
    private final int server;

    /**
     * New proposal.
     */
    private Proposal(final int number, final int server) {
        this.number = number;
        this.server = server;
    }

    /**
     * Proposal with next ballot number.
     * @return New proposal with same server ID
     */
    public Proposal next() {
        return new Proposal(this.number + 1, this.server);
    }

    /**
     * Update ballot number of proposal from other.
     * @return New proposal with updated ballot number
     */
    public Proposal update(final Proposal other) {
        return new Proposal(other.number, this.server);
    }

    @Override
    public int compareTo(final Proposal other) {
        return Integer.compare(this.number, other.number) * 2
            + Integer.compare(this.server, other.server);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Proposal && this.compareTo((Proposal) obj) == 0;
    }

    @Override
    public int hashCode() {
        return 13 * this.number + this.server;
    }

    @Override
    public String toString() {
        return String.format("proposal(bal:%d, s:%d)", this.number, this.server);
    }

    public static Proposal init(final int server) {
        return new Proposal(0, server);
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
 * @since 1.0
 */
public final class Proposer<T> {

    /**
     * Proposer id for logging.
     */
    private final int id;

    /**
     * Current proposal.
     */
    private Proposal prop;

    /**
     * List of acceptors. (assuming all aceptors are alive now).
     */
    private final List<? extends Acceptor<T>> acceptors;

    /**
     * New proposer with server id and list of acceptors.
     */
    public Proposer(final int server, final List<? extends Acceptor<T>> acc) {
        this.id = server;
        this.prop = Proposal.init(server);
        this.acceptors = acc;
    }

    public void propose(final T value) {
       final Proposal next = this.prop.next();
        debug("proposing %s value with %s", value, next);

       // shuffle a little just for simulation, it's not a part of paxos
       final List<? extends Acceptor<T>> copy = new ArrayList<>(this.acceptors);
       Collections.shuffle(copy);

       final int quorum = copy.size() / 2 + 1;

       final T change = copy.parallelStream().limit(quorum)
           .map(acc -> acc.prepare(next))
           .filter(promise -> promise.isGreater(next))
           .max(Acceptor.Promise.CMP_BY_PROPOSAL)
           .map(Acceptor.Promise::value)
           .orElse(value);
       debug("prepared, value=`%s`, %s", change, next);

       // shuffle again for simulation
       Collections.shuffle(copy);

       final Optional<Proposal> res = copy.parallelStream().limit(quorum)
           .map(acc -> acc.accept(next, change))
           .filter(mp -> mp.compareTo(next) > 0)
           .max(Comparator.naturalOrder());
        this.prop = res.map(next::update).orElse(next);
        if (res.isPresent()) {
            debug("rejected, %s", this.prop);
            this.propose(change);
        } else {
            debug("accepted, value=`%s`, %s", change, this.prop);
        }
    }

    private static boolean DEBUG = true;

    private void debug(final String msg, final Object... args) {
        if (DEBUG) {
            System.out.printf("proposer(" + id +"): " + msg + '\n', args);
        }
    }
}

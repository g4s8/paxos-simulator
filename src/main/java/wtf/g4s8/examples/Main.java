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

package wtf.g4s8.examples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import wtf.g4s8.examples.spaxos.*;

/**
 *
 * @since 1.0
 */
public final class Main {

    public static void main(final String... args) throws Exception {
        final int nProp = Integer.parseInt(args[0]);
        final int nAcc = Integer.parseInt(args[1]);

        List<AtomicReference<String>> memory = Stream.generate(() -> new AtomicReference<>(""))
            .limit(nAcc).collect(Collectors.toList());
        List<Acceptor<String>> acceptors = memory.stream()
            .map(InMemoryAcceptor::new).map(AcceptorDelaySimulator::new)
            .collect(Collectors.toList());
        List<Proposer<String>> proposers = Stream.generate(new ProposerGen<>(acceptors))
            .limit(nProp).collect(Collectors.toList());

        CountDownLatch cd = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(nProp);
        ExecutorService exec = Executors.newCachedThreadPool();

        for (int i = 0; i < nProp; i++) {
            final int val = i;
            exec.submit(() -> {
                try {
                    cd.await();
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    return;
                }
                proposers.get(val).propose(Integer.toString(val));
                done.countDown();
            });
        }
        cd.countDown();
        done.await();
        exec.shutdown();

        Map<String, Long> res = memory.stream().map(AtomicReference::get)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Map.Entry<String, Long>> entries = new ArrayList<>(res.entrySet());
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        System.out.println("Results:");
        entries.forEach(kv -> System.out.printf("%s: %d\n", kv.getKey(), kv.getValue()));
    }

    private static final class ProposerGen<T> implements Supplier<Proposer<T>> {

        private final AtomicInteger server;
        private final List<? extends Acceptor<T>> acceptors;

        public ProposerGen(final List<? extends Acceptor<T>> acceptors) {
            this.acceptors = acceptors;
            this.server = new AtomicInteger();
        }

        public Proposer<T> get() {
            return new Proposer<>(this.server.incrementAndGet(),this.acceptors);
        }
    }

    private static final class AcceptorDelaySimulator<T> implements Acceptor<T> {

        private static final Random RND = new Random();

        private final Acceptor<T> origin;

        public AcceptorDelaySimulator(final Acceptor<T> origin) {
            this.origin = origin;
        }

        public Acceptor.Promise<T> prepare(Proposal prop) {
            delay();
            return this.origin.prepare(prop);
        }

        public Proposal accept(Proposal prop, T value) {
            delay();
            return this.origin.accept(prop, value);
        }

        private static void delay() {
            try {
                Thread.sleep(RND.nextInt(15));
            } catch (InterruptedException iex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

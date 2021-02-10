package wtf.g4s8.examples.configuration;

import wtf.g4s8.examples.spaxos.*;
import wtf.g4s8.examples.system.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TransactionTest {

    public static ExecutorService exec = Executors.newCachedThreadPool();
    public static AtomicInteger transactionId = new AtomicInteger(1);
    public static CountDownLatch done;

    public void test() throws Exception {
        List<ResourceManager> resourceManagers = resourceManagerCluster();
        writeConcurrent(
                Config.nUpdaters,
                new StupidTransactionManager(
                        Config.syncDelayInSeconds,
                        resourceManagers
                )
        );
        done = new CountDownLatch(Config.nTransactions);
        done.await();
        exec.shutdown();
        Proposer.EXEC_TIMEOUT.shutdown();
        StupidTransactionManager.POOL.shutdown();
        System.out.println("---TEST FINISHED---");
        resourceManagers.forEach(replica -> {
            System.out.printf("RM-%s value: %s\n", replica.id(), replica.storage().value().toString());
        });
        System.exit(0);
    }


    private void writeConcurrent(int nUpdaters, StupidTransactionManager tm) throws Exception {
        CountDownLatch cd = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(nUpdaters);
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < nUpdaters; i++) {
            final int val = i;
            exec.submit(() -> {
                try {
                    cd.await();
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    return;
                }
                tm.update(String.valueOf(transactionId.getAndIncrement()),0, val+10);
                done.countDown();
            });
        }
        cd.countDown();
        done.await();
    }

    /**
     * Creates {@link Config#nReplicas} ResourceManagers with Paxos instance connected to TM and all RMs.
     */
    private List<ResourceManager> resourceManagerCluster() {
        Stream<ResourceManager> rmStream = IntStream.rangeClosed(1, Config.nReplicas).mapToObj(
                StupidResourceManager::new
        );
        if (Config.withDrops) {
            rmStream = rmStream
                    .map(a -> new DropResourceManager(Config.dropRate, a));
        }
        if (Config.withTimeout) {
            rmStream = rmStream
                    .map(a -> new TimeoutResourceManager(Config.timeoutMilliseconds, a));
        }
        if (Config.async) {
            rmStream = rmStream
                    .map(a -> new AsyncResourceManager(exec, a));
        }
        return rmStream.collect(Collectors.toList());

    }


    /**
     * Creates nReplicas * (nReplicas + 1) acceptors and groups them by belonging to Proposer.
     * Each ResourceManager (nReplicas) has an acceptor on every Replica, including itself. (nReplicas * nReplicas)
     * TransactionManager also has an acceptor for every ResourceManager. (nReplicas * (nReplicas + 1))
     */
    public static List<Acceptor<Decision>> acceptors(int serverId, String transactionId) {
        Stream<Acceptor<Decision>> acceptorStream = IntStream.rangeClosed(0, Config.nReplicas)
                .limit(Config.nReplicas + 1)
                .mapToObj(id -> new InMemoryAcceptor<>(new AtomicReference<>(Decision.NONE), id, transactionId));

        if (Config.withDrops) {
            acceptorStream = acceptorStream
                    .map(a -> new DropAcceptor<>(Config.dropRate, a));
        }
        if (Config.withTimeout) {
            acceptorStream = acceptorStream
                    .map(a -> new TimeoutAcceptor<>(Config.timeoutMilliseconds, a));
        }
        if (Config.async) {
            acceptorStream = acceptorStream
                    .map(a -> new AsyncAcceptor<>(exec, a));
        }
        return acceptorStream
                .collect(
                        Collectors.toList()
                );
    }

}

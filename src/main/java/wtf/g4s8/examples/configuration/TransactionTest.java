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

import static wtf.g4s8.examples.configuration.Config.cfg;

public class TransactionTest {

    public static ExecutorService exec = Executors.newCachedThreadPool();
    public static AtomicInteger transactionId = new AtomicInteger(1);
    public static CountDownLatch done;

    public void test() throws Exception {
        List<ResourceManager> resourceManagers = resourceManagerCluster();
        writeConcurrent(
                cfg.nUpdaters,
                new StupidTransactionManager(
                        cfg.syncDelayInSeconds,
                        resourceManagers
                )
        );
        done = new CountDownLatch(cfg.nTransactions);
        done.await();
        Proposer.EXEC_TIMEOUT.shutdown();
        StupidTransactionManager.POOL.shutdown();
        Thread.sleep(cfg.timeoutMilliseconds + 100);
        exec.shutdown();
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
        Stream<ResourceManager> rmStream = IntStream.rangeClosed(1, cfg.nReplicas).mapToObj(
                StupidResourceManager::new
        );
        if (cfg.withDrops) {
            rmStream = rmStream
                    .map(a -> new DropResourceManager(cfg.dropRate, a));
        }
        if (cfg.withTimeout) {
            rmStream = rmStream
                    .map(a -> new TimeoutResourceManager(cfg.timeoutMilliseconds, a));
        }
        if (cfg.async) {
            rmStream = rmStream
                    .map(a -> new AsyncResourceManager(exec, a));
        }
        return rmStream.collect(Collectors.toList());

    }


    public static List<Acceptor<Decision>> acceptors(int serverId, String transactionId) {
        Stream<Acceptor<Decision>> acceptorStream = IntStream.rangeClosed(0, cfg.nReplicas)
                .limit(cfg.nReplicas + 1)
                .mapToObj(id -> new InMemoryAcceptor<>(new AtomicReference<>(Decision.NONE), id, transactionId));

        if (cfg.withDrops) {
            acceptorStream = acceptorStream
                    .map(a -> new DropAcceptor<>(cfg.dropRate, a));
        }
        if (cfg.withTimeout) {
            acceptorStream = acceptorStream
                    .map(a -> new TimeoutAcceptor<>(cfg.timeoutMilliseconds, a));
        }
        if (cfg.async) {
            acceptorStream = acceptorStream
                    .map(a -> new AsyncAcceptor<>(exec, a));
        }
        return acceptorStream
                .collect(
                        Collectors.toList()
                );
    }

}

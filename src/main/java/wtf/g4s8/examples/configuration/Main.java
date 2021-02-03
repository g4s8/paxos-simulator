package wtf.g4s8.examples.configuration;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        Config.delay = 30;
        TransactionTest.builder()
                .nReplicas(3)
                .nUpdaters(1)
                .async(true)
                .accexec(Executors.newCachedThreadPool())
                .withTimeout(true)
                .timeout(20)
                .withDrops(true)
                .dropRate(0.2)
                .syncDelay(10)
                .build()
        .test();
    }
}

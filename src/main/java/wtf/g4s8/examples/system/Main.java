package wtf.g4s8.examples.system;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        TransactionTest.builder()
                .nReplicas(3)
                .nUpdaters(1)
                .async(true)
                .accexec(Executors.newCachedThreadPool())
                .withTimeout(true)
                .timeout(200)
                .withDrops(true)
                .dropRate(0.3)
                .syncDelay(30)
                .build()
        .test();
    }
}

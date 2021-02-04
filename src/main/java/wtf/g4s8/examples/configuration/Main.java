package wtf.g4s8.examples.configuration;

import java.util.concurrent.Executors;

public class Main {


    public static void main(String[] args) throws Exception {
        Config.delay = 30;
        Config.nReplicas = 3;
        Config.withDrops = false;
        Config.dropRate = 0.1;
        Config.withTimeout = false;
        Config.timeout = 0;
        Config.async = true;
        Config.isRmUnstable = true;
        Config.accexec = Executors.newCachedThreadPool();
        TransactionTest.builder()
                .nReplicas(3)
                .nUpdaters(3)
                .async(true)
                .accexec(Config.accexec)
//                .withTimeout(true)
//                .timeout(20)
//                .withDrops(true)
//                .dropRate(0.2)
                .syncDelay(4)
                .build()
        .test();
    }
}

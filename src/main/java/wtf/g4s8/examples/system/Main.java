package wtf.g4s8.examples.system;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        TransactionTest.builder()
                .nReplicas(3)
                .nUpdaters(1)
                .async(true)
                .accexec(Executors.newCachedThreadPool())
                //.withTimeout(true)
                //.timeout(2000)
//                .withDrops(true)
//                .dropRate(0.1)
                .build()
        .test();
    }
}

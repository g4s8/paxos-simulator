package wtf.g4s8.examples.configuration;


public class Main {


    public static void main(String[] args) throws Exception {
        Config.paxosProposerTimeOutMilliseconds = 300;
        Config.nReplicas = 3;
        Config.nUpdaters = 2;
        Config.nTransactions = 2;
        Config.withDrops = false;
        Config.dropRate = 0.1;
        Config.withTimeout = false;
        Config.timeoutMilliseconds = 0;
        Config.async = true;
        Config.rmCrashRate = 0;
        Config.syncDelayInSeconds = 5;
        Config.nRetries = 1;
        Config.retryUpdateMinTimeOutInSeconds = 0;
        Config.retryUpdateMaxTimeOutInSeconds = 20;
        new TransactionTest().test();
    }
}

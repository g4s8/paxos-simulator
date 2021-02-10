package wtf.g4s8.examples.configuration;


public class Config {
    /**
     * Cluster size
     */
    public static int nReplicas;
    /**
     * Amount of independent unsynchronized updaters, that tries to modify a value
     */
    public static int nUpdaters;
    /**
     * Total amount of write operations in test case
     */
    public static int nTransactions;
    /**
     * every call goes async
     */
    public static boolean async;
    /**
     * Restart proposing, if decision wasn't made in given time.
     */
    public static int paxosProposerTimeOutMilliseconds;
    /**
     * If proposer-acceptors & TM-RMs channels are unstable (messages between them could be lost)
     */
    public static boolean withDrops;
    public static double dropRate;

    /**
     * If proposers, acceptors, TMs and RMs need some time to respond.
     */
    public static boolean withTimeout;
    /**
     * Upper bound for timeout.
     */
    public static int timeoutMilliseconds;
    /**
     * How frequent RM goes down.
     */
    public static double rmCrashRate;
    /**
     * How much time RM needs to restart.
     */
    public static long rmRestartTimeOutInSeconds;
    /**
     * How much TM should wait before sync up.
     */
    public static int syncDelayInSeconds;
    /**
     * How many times try to repeat transaction.
     */
    public static int nRetries;
    /**
     * Lower border how much to wait before retry update.
     */
    public static int retryUpdateMinTimeOutInSeconds;
    /**
     * Upper border.
     */
    public static int retryUpdateMaxTimeOutInSeconds;
    /**
     * Whether to show thread name in logs.
     */
    public static boolean traceThreads;
}

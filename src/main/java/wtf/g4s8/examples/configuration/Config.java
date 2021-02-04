package wtf.g4s8.examples.configuration;

import java.util.concurrent.ExecutorService;

public class Config {
    public static int delay;
    public static boolean isRmUnstable = false;
    public static long restartTime;
    public static int nReplicas;
    public static boolean withDrops;
    public static double dropRate;
    public static boolean withTimeout;
    public static int timeout;
    public static boolean async;
    public static ExecutorService accexec;
}

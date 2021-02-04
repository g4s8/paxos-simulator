package wtf.g4s8.examples;

public class Log {

    private static final boolean DEBUG = true;

    public static synchronized void d(final String fmt, final Object... args) {
        System.out.printf(fmt + '\n', args);
    }

    public static synchronized void log(final String msg) {
        System.out.printf("[%s] %s", Thread.currentThread().getName(), msg);
    }

    public static synchronized void logf(final String fmt, final Object... args) {
        System.out.printf("[" + Thread.currentThread().getName() + "]" + fmt + '\n', args);
    }
}

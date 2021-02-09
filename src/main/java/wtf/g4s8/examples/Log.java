package wtf.g4s8.examples;

public class Log {

    public interface Logger {
        void log(String msg);
        void logf(String fmt, Object... args);
    }

    public static final Logger DEFAULT = new Logger() {
        public void log(String msg) {
            System.out.printf("[t:%s] %s\n", Thread.currentThread().getName(), msg);
        }
        public void logf(String fmt, Object... args) {
            System.out.printf("[t:" + Thread.currentThread().getName() + "] " + fmt + '\n', args);
        }
    };

    public static final class Prefixed implements Logger {
        private final String prefix;

        public Prefixed(String prefix) {
            this.prefix = prefix;
        }
        public void log(String msg) {
            System.out.printf("%s: [t:%s] %s\n", prefix, Thread.currentThread().getName(), msg);
        }
        public void logf(String fmt, Object... args) {
            System.out.printf(prefix + ": [t:" + Thread.currentThread().getName() + "] " + fmt + '\n', args);
        }
    }

    public static Logger logger(Object source) {
        return new Prefixed(source.toString());
    }

    private static final boolean DEBUG = true;

    public static synchronized void d(final String fmt, final Object... args) {
        if (DEBUG)
        System.out.printf(fmt + '\n', args);
    }

    public static synchronized void log(final String msg) {
        DEFAULT.log(msg);
    }

    public static synchronized void logf(final String fmt, final Object... args) {
        DEFAULT.logf(fmt, args);
    }
}

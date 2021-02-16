package wtf.g4s8.examples.system;

/**
 * Sync protocol to update current transaction state on TM.
 */
public interface Sync<T> {
    /**
     * Asks acceptor for its value.
     */
    void requestValue(Receiver<T> callback);

    interface Receiver<T> {
        void receive(T value, String metadata);
    }
}

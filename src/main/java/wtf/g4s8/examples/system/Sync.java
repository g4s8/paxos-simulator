package wtf.g4s8.examples.system;

public interface Sync<T> {
    void requestValue(Receiver<T> callback);

    interface Receiver<T> {
        void receive(T value);
    }
}

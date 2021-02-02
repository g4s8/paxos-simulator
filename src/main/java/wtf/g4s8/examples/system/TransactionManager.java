package wtf.g4s8.examples.system;

public interface TransactionManager {
    void update(String id, int currentValue, int proposedValue);
}

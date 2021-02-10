package wtf.g4s8.examples.system;

public interface TransactionManager {

    void update(String uid, int currentValue, int proposedValue);
}

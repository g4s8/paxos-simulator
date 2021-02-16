package wtf.g4s8.examples.system;

public interface TransactionManager {

    /**
     * Perform consistent update or abort on all RMs.
     * @param uid - transactionId
     * @param currentValue - expected value in resource manager's state machine, that should be updated.
     * @param proposedValue - new value to replace currentValue in resource manager's state machine.
     */
    void update(String uid, int currentValue, int proposedValue);
}

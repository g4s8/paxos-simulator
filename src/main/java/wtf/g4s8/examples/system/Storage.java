package wtf.g4s8.examples.system;

public interface Storage {


    Integer value();

    boolean isLocked();

    void lock(String uid);

    void saveDecision(String tansactionUID, Decision decision);

    void saveProposedValue(int newValue);

    Integer proposedValue();

    boolean isLockedBy(String uid);

    void flush();

    void updateValue(Integer proposedValue);
}

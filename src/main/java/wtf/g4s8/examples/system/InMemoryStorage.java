package wtf.g4s8.examples.system;

public class InMemoryStorage {

    public Integer value = 0;
    private Integer tmp;
    private String lockedBy = "";
    private Decision decision;

    public Integer value() {
        return value;
    }

    public boolean isLocked() {
        return !lockedBy.isEmpty();
    }

    public void lock(String uid) {
        this.lockedBy = uid;
    }

    public void saveDecision(Decision decision) {
        this.decision = decision;
    }

    public void saveProposedValue(int newValue) {
        this.tmp = newValue;
    }

    public Integer proposedValue() {
        return tmp;
    }

    public boolean isLockedBy(String uid) {
        return lockedBy.equals(uid);
    }

    public void flush() {
        this.decision = Decision.NONE;
        this.tmp = null;
        this.lockedBy = "";
    }
}

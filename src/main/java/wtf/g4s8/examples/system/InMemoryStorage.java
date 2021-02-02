package wtf.g4s8.examples.system;

import java.util.HashMap;
import java.util.Map;

public class InMemoryStorage implements Storage{

    public Integer value = 0;
    private Integer tmp;
    private String lockedBy = "";
    private Map<String, Decision> decisions = new HashMap<>();

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public boolean isLocked() {
        return !lockedBy.isEmpty();
    }

    @Override
    public void lock(String uid) {
        this.lockedBy = uid;
    }

    @Override
    public void saveDecision(String tansactionUID, Decision decision) {
        this.decisions.putIfAbsent(tansactionUID, decision);
    }

    @Override
    public void saveProposedValue(int newValue) {
        this.tmp = newValue;
    }

    @Override
    public Integer proposedValue() {
        return tmp;
    }

    @Override
    public boolean isLockedBy(String uid) {
        return lockedBy.equals(uid);
    }

    @Override
    public void flush() {
        this.tmp = null;
        this.lockedBy = "";
    }

    @Override
    public void updateValue(Integer proposedValue) {
        this.value = proposedValue;
    }
}

package wtf.g4s8.examples.system.storage;

import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.system.Decision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage implements Storage {

    public Integer value = 0;
    private final Map<String, Integer> tmp = new HashMap<>();
    private String lockedBy = "";
    final ConcurrentHashMap<String, List<Acceptor<Decision>>> activeAcceptors = new ConcurrentHashMap<>();

    @Override
    public void saveActiveAcceptors(String tuid, List<Acceptor<Decision>> accs) {
        activeAcceptors.put(tuid, accs);
    }

    @Override
    public ConcurrentHashMap<String, List<Acceptor<Decision>>> activeAcceptors() {
        return this.activeAcceptors;
    }

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public synchronized boolean isLocked() {
        return !lockedBy.isEmpty();
    }

    @Override
    public synchronized void lock(String uid) {
        this.lockedBy = uid;
    }

    @Override
    public void saveProposedValue(String transactionId, int newValue) {
        this.tmp.put(transactionId, newValue);
    }

    @Override
    public Integer proposedValue(String transactionId) {
        return this.tmp.get(transactionId);
    }

    @Override
    public synchronized boolean isLockedBy(String uid) {
        return lockedBy.equals(uid);
    }

    @Override
    public void flush(String transactionId) {
        this.activeAcceptors.remove(transactionId);
        this.tmp.remove(transactionId);
    }

    @Override
    public void updateValue(Integer proposedValue) {
        this.value = proposedValue;
    }

    @Override
    public synchronized void unlock() {
        this.lockedBy = "";
    }

}

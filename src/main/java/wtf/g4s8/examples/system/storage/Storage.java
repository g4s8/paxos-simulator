package wtf.g4s8.examples.system.storage;

import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.system.Decision;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface Storage {

    void saveActiveAcceptors(String tuid, List<Acceptor<Decision>> accs);

    ConcurrentHashMap<String, List<Acceptor<Decision>>> activeAcceptors();

    Integer value();

    boolean isLocked();

    String holder();

    void lock(String uid);

    void saveProposedValue(String transactionId, int newValue);

    Integer proposedValue(String transactionId);

    boolean isLockedBy(String uid);

    void flush(String transactionId);

    void updateValue(Integer proposedValue);

    void unlock();
}

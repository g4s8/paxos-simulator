package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.system.storage.Storage;

import java.util.List;

public interface ResourceManager {

    /**
     * Prepares to commit and locks storage if possible, else aborts transaction.
     * Also notifies other transaction participant about its decision.
     * @param patch - update
     * @param acceptors - participants to notify
     */
    void prepare(Patch patch, List<Acceptor<Decision>> acceptors);

    /**
     * Commits given transaction and releases locks.
     */
    void commit(String transactionId);

    /**
     * Aborts given transaction and releases locks if any.
     */
    void abort(String transactionId);

    Integer id();

    List<Acceptor<Decision>> acceptors(String transactionId);

    Storage storage();
}

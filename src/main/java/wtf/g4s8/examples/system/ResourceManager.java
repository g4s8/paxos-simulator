package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.system.storage.Storage;

import java.util.List;

public interface ResourceManager {

    void update(Patch patch, List<Acceptor<Decision>> acceptors);

    void commit(String transactionId);

    Integer id();

    List<Acceptor<Decision>> acceptors(String transactionId);

    Storage storage();

    void abort(String transactionId);
}

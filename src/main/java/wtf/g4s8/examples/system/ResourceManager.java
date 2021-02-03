package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;

import java.util.List;

public interface ResourceManager {

    void update(Patch patch);

    void commit(String transactionId);

    Integer id();

    List<Acceptor<Decision>> acceptors();

    Storage storage();
}

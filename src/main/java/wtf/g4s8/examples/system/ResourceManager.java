package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;

import java.util.List;

public interface ResourceManager {
    //check if update is doable.
    //Yes -> prepare
    //No -> abort
    void update(Patch patch);

    void commit(Patch patch);

    Integer id();

    List<Acceptor<Decision>> acceptors();

    Storage storage();
}

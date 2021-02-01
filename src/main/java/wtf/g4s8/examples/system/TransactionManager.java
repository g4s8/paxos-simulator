package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.spaxos.AsyncAcceptor;

import java.util.List;
import java.util.Map;

public interface TransactionManager {
    void update(String id, int currentValue, int proposedValue, List<? extends ResourceManager> replicas) throws Exception;

    void commit(String uid, int currentValue, int proposedValue, List<? extends ResourceManager> replicas) throws Exception;

    Decision sync(List<StupidResourceManager<Decision>> replicas);

}

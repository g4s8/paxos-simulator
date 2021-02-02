package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.spaxos.Proposer;

import java.util.List;

import static wtf.g4s8.examples.system.Decision.ABORT;
import static wtf.g4s8.examples.system.Decision.COMMIT;

public class StupidResourceManager implements ResourceManager {
    public final List<Acceptor<Decision>> accs;
    Integer id;
    Proposer<Decision> decisionProposer;
    Storage storage;


    public StupidResourceManager(int id, Proposer<Decision> decisionProposer, List<Acceptor<Decision>> accs) {
        this.decisionProposer = decisionProposer;
        this.id = id;
        storage = new InMemoryStorage();
        this.accs = accs;
    }

    @Override
    public void update(Patch patch) {
        if(!storage.isLocked() && storage.value() == patch.lastKnownValue) {
                storage.lock(patch.uid);
                storage.saveDecision(patch.uid, COMMIT);
                storage.saveProposedValue(patch.newValue);
                decisionProposer.propose(COMMIT);
        } else {
            storage.saveDecision(patch.uid, ABORT);
            decisionProposer.propose(ABORT);
        }
    }

    @Override
    public void commit(Patch patch) {
        if (storage.value() == patch.lastKnownValue && storage.isLockedBy(patch.uid)) {
            storage.updateValue(storage.proposedValue());
            storage.flush();
        }
    }

    @Override
    public Integer id() {
        return this.id;
    }

    @Override
    public List<Acceptor<Decision>> acceptors() {
        return this.accs;
    }

    @Override
    public Storage storage() {
        return this.storage;
    }
}

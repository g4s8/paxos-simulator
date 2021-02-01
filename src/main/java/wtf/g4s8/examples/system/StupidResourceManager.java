package wtf.g4s8.examples.system;

import wtf.g4s8.examples.spaxos.Proposer;

import java.util.concurrent.Future;

import static wtf.g4s8.examples.system.Decision.ABORT;
import static wtf.g4s8.examples.system.Decision.COMMIT;

public class StupidResourceManager implements ResourceManager {
    Integer id;
    Proposer<Decision> decisionProposer;
    InMemoryStorage storage;

    public StupidResourceManager(int id, Proposer<Decision> decisionProposer) {
        this.decisionProposer = decisionProposer;
        this.id = id;
        storage = new InMemoryStorage();
    }

    @Override
    public Future update(Patch patch) {
        if(!storage.isLocked()) {
            if (storage.value() == patch.lastKnownValue) {
                storage.lock(patch.uid);
                storage.saveDecision(COMMIT);
                storage.saveProposedValue(patch.newValue);
                return decisionProposer.propose(COMMIT);
            } else {
                storage.lock(patch.uid);
                storage.saveDecision(ABORT);
                return decisionProposer.propose(ABORT);
            }
        }
        return null;
    }

    @Override
    public void commit(Patch patch) {
        if (storage.value() == patch.lastKnownValue && storage.isLockedBy(patch.uid)) {
            storage.value = storage.proposedValue();
            storage.flush();
        }
    }
}

package wtf.g4s8.examples.system;

import wtf.g4s8.examples.configuration.Config;
import wtf.g4s8.examples.spaxos.Acceptor;
import wtf.g4s8.examples.spaxos.Proposer;
import wtf.g4s8.examples.system.storage.InMemoryStorage;
import wtf.g4s8.examples.system.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static wtf.g4s8.examples.system.Decision.ABORT;
import static wtf.g4s8.examples.system.Decision.PREPARE;

/**
 * SafeResourceManager saves transaction data to stable storage before update
 */
public class StupidResourceManager implements ResourceManager {
    private static final Random RNG = new Random();
    final Integer id;
    final Storage storage;

    final ConcurrentHashMap<String, Proposer<Decision>> activeProps = new ConcurrentHashMap<>();
    private volatile boolean dead;


    public StupidResourceManager(int id) {
        this.id = id;
        storage = new InMemoryStorage();
    }

    @Override
    public void update(Patch patch, List<Acceptor<Decision>> acceptors) {
        if (dead) {
            return;
        }
        Proposer<Decision> decisionProposer = new Proposer<>(id, patch.uid, acceptors);
        activeProps.put(patch.uid, decisionProposer);
        if(!storage.isLocked() && storage.value() == patch.lastKnownValue) {
                storage.lock(patch.uid);
                storage.saveActiveAcceptors(patch.uid, acceptors);
                storage.saveProposedValue(patch.uid, patch.newValue);
                restartIf();
                System.out.printf("[%s] preparing on %d\n", patch.uid, id);
                decisionProposer.propose(PREPARE);
                restartIf();
        } else {
            System.out.printf("[%s] aborted on %d\n", patch.uid, id);
            storage.saveActiveAcceptors(patch.uid, acceptors);
            restartIf();
            decisionProposer.propose(ABORT);
            restartIf();
        }
    }

    @Override
    public void commit(String transactionId) {
        if (dead) {
            return;
        }
        if (storage.isLockedBy(transactionId)) {
            System.out.printf("[%s] committed on %d\n", transactionId, id);
            storage.updateValue(storage.proposedValue(transactionId));
            storage.flush(transactionId);
            storage.unlock();
        }
        if (activeProps.get(transactionId) != null) {
            activeProps.get(transactionId).kill();
            activeProps.remove(transactionId);
        }
    }

    @Override
    public void abort(String transactionId) {
        if (dead) {
            return;
        }
        storage.flush(transactionId);
        if (storage.isLockedBy(transactionId)) {
            System.out.printf("[%s] aborted on %d\n", transactionId, id);
            storage.unlock();
        }
        if (activeProps.get(transactionId) != null) {
            activeProps.get(transactionId).kill();
            activeProps.remove(transactionId);
        }
    }

    @Override
    public Integer id() {
        return this.id;
    }

    @Override
    public List<Acceptor<Decision>> acceptors(String transactionId) {
        return this.storage.activeAcceptors().getOrDefault(transactionId, new ArrayList<>());
    }

    @Override
    public Storage storage() {
        return this.storage;
    }

    private void restartIf() {
        if (!dead && RNG.nextDouble() < Config.rmCrashRate) {
            System.out.printf("RM-%d restarting\n", id);
            shutDown();
            Executors.newSingleThreadScheduledExecutor().schedule(this::startUp, Config.rmRestartTimeOutInSeconds, TimeUnit.SECONDS);

        }
    }

    private void startUp() {
        this.storage.activeAcceptors().forEach((transactionId, accs) -> {
            Proposer<Decision> decisionProposer = new Proposer<>(this.id, transactionId, accs);
            activeProps.put(transactionId, decisionProposer);
            decisionProposer.propose(ABORT);
        });
        this.dead = false;

    }

    private void shutDown() {
        this.dead = true;
        activeProps.values().forEach(Proposer::kill);
        activeProps.clear();
    }
}

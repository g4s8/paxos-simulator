package wtf.g4s8.examples.system;

import java.util.concurrent.Future;

public interface ResourceManager {
    //check if update is doable.
    //Yes -> prepare
    //No -> abort
    Future update(Patch patch);

    void commit(Patch patch);
}

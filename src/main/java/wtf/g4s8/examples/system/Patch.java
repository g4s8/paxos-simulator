package wtf.g4s8.examples.system;

import java.util.List;

public class Patch {
    String uid;
    int lastKnownValue;
    int newValue;
    List<? extends ResourceManager> participants;


    public Patch(String uid, int lastKnownValue, int newValue, List<? extends ResourceManager> participants) {
        this.uid = uid;
        this.lastKnownValue = lastKnownValue;
        this.newValue = newValue;
        this.participants = participants;
    }
}

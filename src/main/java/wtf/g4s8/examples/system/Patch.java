package wtf.g4s8.examples.system;

public class Patch {
    final String uid;
    final int lastKnownValue;
    final int newValue;


    public Patch(String uid, int lastKnownValue, int newValue) {
        this.uid = uid;
        this.lastKnownValue = lastKnownValue;
        this.newValue = newValue;
    }
}

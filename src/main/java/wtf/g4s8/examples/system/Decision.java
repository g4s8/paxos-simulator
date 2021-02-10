package wtf.g4s8.examples.system;

/**
 * Decision enum represents RM decision weather to commit or abort transaction.
 * PREPARE - RM locked values and ready to commit new ones.
 * ABORT - All locks are released, if any. Transaction is aborted.
 * NONE - null object.
 */
public enum Decision {
    NONE,
    PREPARE,
    ABORT;
}

package wtf.g4s8.examples.configuration;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Config {

    public final static Config cfg = Main.initConfig();
    /**
     * Cluster size
     */
    public final int nReplicas;
    /**
     * Amount of independent unsynchronized updaters, that tries to modify a value
     */
    public final int nUpdaters;
    /**
     * Total amount of write operations in test case
     */
    public final int nTransactions;
    /**
     * every call goes async
     */
    public final boolean async;
    /**
     * Restart proposing, if decision wasn't made in given time.
     */
    public final int paxosProposerTimeOutMilliseconds;
    /**
     * If proposer-acceptors & TM-RMs channels are unstable (messages between them could be lost)
     */
    public final boolean withDrops;
    public final double dropRate;

    /**
     * If proposers, acceptors, TMs and RMs need some time to respond.
     */
    public final boolean withTimeout;
    /**
     * Upper bound for timeout.
     */
    public final int timeoutMilliseconds;
    /**
     * How frequent RM goes down.
     */
    public final double rmCrashRate;
    /**
     * How much time RM needs to restart.
     */
    public final long rmRestartTimeOutInSeconds;
    /**
     * How much TM should wait before sync up.
     */
    public final int syncDelayInSeconds;
    /**
     * How many times try to repeat transaction.
     */
    public final int nRetries;
    /**
     * Lower border how much to wait before retry update.
     */
    public final int retryUpdateMinTimeOutInSeconds;
    /**
     * Upper border.
     */
    public final int retryUpdateMaxTimeOutInSeconds;
    /**
     * Whether to show thread name in logs.
     */
    public final boolean traceThreads;

    static class ConfigBuilder {
        @JsonProperty
        private int nReplicas;
        @JsonProperty
        private int nUpdaters;
        @JsonProperty
        private int nTransactions;
        @JsonProperty
        private boolean async;
        @JsonProperty
        private int paxosProposerTimeOutMilliseconds;
        @JsonProperty
        private boolean withDrops;
        @JsonProperty
        private double dropRate;
        @JsonProperty
        private boolean withTimeout;
        @JsonProperty
        private int timeoutMilliseconds;
        @JsonProperty
        private double rmCrashRate;
        @JsonProperty
        private long rmRestartTimeOutInSeconds;
        @JsonProperty
        private int syncDelayInSeconds;
        @JsonProperty
        private int nRetries;
        @JsonProperty
        private int retryUpdateMinTimeOutInSeconds;
        @JsonProperty
        private int retryUpdateMaxTimeOutInSeconds;
        @JsonProperty
        private boolean traceThreads;

        ConfigBuilder() {
        }

        public ConfigBuilder nReplicas(int nReplicas) {
            this.nReplicas = nReplicas;
            return this;
        }

        public ConfigBuilder nUpdaters(int nUpdaters) {
            this.nUpdaters = nUpdaters;
            return this;
        }

        public ConfigBuilder nTransactions(int nTransactions) {
            this.nTransactions = nTransactions;
            return this;
        }

        public ConfigBuilder async(boolean async) {
            this.async = async;
            return this;
        }

        public ConfigBuilder paxosProposerTimeOutMilliseconds(int paxosProposerTimeOutMilliseconds) {
            this.paxosProposerTimeOutMilliseconds = paxosProposerTimeOutMilliseconds;
            return this;
        }

        public ConfigBuilder withDrops(boolean withDrops) {
            this.withDrops = withDrops;
            return this;
        }

        public ConfigBuilder dropRate(double dropRate) {
            this.dropRate = dropRate;
            return this;
        }

        public ConfigBuilder withTimeout(boolean withTimeout) {
            this.withTimeout = withTimeout;
            return this;
        }

        public ConfigBuilder timeoutMilliseconds(int timeoutMilliseconds) {
            this.timeoutMilliseconds = timeoutMilliseconds;
            return this;
        }

        public ConfigBuilder rmCrashRate(double rmCrashRate) {
            this.rmCrashRate = rmCrashRate;
            return this;
        }

        public ConfigBuilder rmRestartTimeOutInSeconds(long rmRestartTimeOutInSeconds) {
            this.rmRestartTimeOutInSeconds = rmRestartTimeOutInSeconds;
            return this;
        }

        public ConfigBuilder syncDelayInSeconds(int syncDelayInSeconds) {
            this.syncDelayInSeconds = syncDelayInSeconds;
            return this;
        }

        public ConfigBuilder nRetries(int nRetries) {
            this.nRetries = nRetries;
            return this;
        }

        public ConfigBuilder retryUpdateMinTimeOutInSeconds(int retryUpdateMinTimeOutInSeconds) {
            this.retryUpdateMinTimeOutInSeconds = retryUpdateMinTimeOutInSeconds;
            return this;
        }

        public ConfigBuilder retryUpdateMaxTimeOutInSeconds(int retryUpdateMaxTimeOutInSeconds) {
            this.retryUpdateMaxTimeOutInSeconds = retryUpdateMaxTimeOutInSeconds;
            return this;
        }

        public ConfigBuilder traceThreads(boolean traceThreads) {
            this.traceThreads = traceThreads;
            return this;
        }

        public Config build() {
            return new Config(nReplicas, nUpdaters, nTransactions, async, paxosProposerTimeOutMilliseconds, withDrops, dropRate, withTimeout, timeoutMilliseconds, rmCrashRate, rmRestartTimeOutInSeconds, syncDelayInSeconds, nRetries, retryUpdateMinTimeOutInSeconds, retryUpdateMaxTimeOutInSeconds, traceThreads);
        }

        public String toString() {
            return "Config.ConfigBuilder(nReplicas=" + this.nReplicas + ", nUpdaters=" + this.nUpdaters + ", nTransactions=" + this.nTransactions + ", async=" + this.async + ", paxosProposerTimeOutMilliseconds=" + this.paxosProposerTimeOutMilliseconds + ", withDrops=" + this.withDrops + ", dropRate=" + this.dropRate + ", withTimeout=" + this.withTimeout + ", timeoutMilliseconds=" + this.timeoutMilliseconds + ", rmCrashRate=" + this.rmCrashRate + ", rmRestartTimeOutInSeconds=" + this.rmRestartTimeOutInSeconds + ", syncDelayInSeconds=" + this.syncDelayInSeconds + ", nRetries=" + this.nRetries + ", retryUpdateMinTimeOutInSeconds=" + this.retryUpdateMinTimeOutInSeconds + ", retryUpdateMaxTimeOutInSeconds=" + this.retryUpdateMaxTimeOutInSeconds + ", traceThreads=" + this.traceThreads + ")";
        }
    }
}

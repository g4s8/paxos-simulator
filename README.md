It's Paxos-simple simulator, the stakeholders here: acceptors and proposers.
The simulator generates `n` proposers and `m` acceptors and asks each proposer to propose different values.


# How to build

You need java >= 11 and Maven >= 3. Run:
```bash
mvn package -Passembly
```
to produce assembly jar at `./target`.

# How to run

Run it with `java -jar ./target/spaxos-with-dependencies.jar <pathToConfigFile>`,
where `ConfigFile` is yaml representation of [Config.java](src/main/java/wtf/g4s8/examples/configuration/Config.java)
If you run it without an argument `java -jar ./target/spaxos-with-dependencies.jar` [default_cfg.yml](src/main/resources/default_cfg.yml) will be used.

It'll print transactions' logs and result table of values of Resource Managers' state machines. 

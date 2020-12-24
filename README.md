It's Paxos-simple simulator, the stakeholders here: acceptors and proposers.
The simulator generates `n` proposers and `m` acceptors and asks each proposer to propose different values.


# How to build

You need java >= 11 and Maven >= 3. Run:
```bash
mvn package -Passembly
```
to produce assembly jar at `./target`.

# How to run

Run it with `java -jar ./target/spaxos-with-dependencies.jar <proposer> <acceptors>`,
where `proposer` is amount of proposer and `acceptors` amount of acceptors, e.g.:
`java -jar ./target/spaxos-with-dependencies.jar 5 20`.
It'll print proposer's logs and result table of values of acceptros. One value should be
counted for at least quorum (`acceptors/2 + 1`) of acceptors.

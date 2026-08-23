# RedisFilterBolt T_BB implementation

## Frozen design

The executable T_BB implements exactly the eleven frames frozen in:

`isw2/testing/specs/redis-filter-bolt/test-frames.csv`

No additional frame was introduced during implementation.

Frozen Category Partition SHA-256:

`DA8E999461C742F231D033FD939E333BB553C70CF5566AE7EE157C9A194B563A`

Frozen frame CSV SHA-256:

`ACEFDB4DE43078D98636EAF81390205E19DCDE3729F002AC248B9E326A2FE5C5`

## Executable suite

Source:

`isw2/testing/src/test/java/it/uniroma2/isw2/storm/testing/redisfilterbolt/bb/RedisFilterBoltBBTest.java`

SHA-256:

`BBB539411F70DDF94FD0EBF1EE0FC697160895C867ECE4BE5601CBB061AADBFD`

JUnit tests:

`11`

Execution result:

`11 passed / 0 failed / 0 errors / 0 skipped`

## Redis isolation

No real Redis server is started.

`JedisCommandsContainer` is represented by a Mockito collaborator.

A test-only subclass of `RedisFilterBolt` overrides the protected
`getInstance()` seam so that `process(Tuple)` receives that controlled
container.

The same subclass exposes installation of the inherited protected
`OutputCollector` field.

These mechanisms are fixture construction only.

T_BB does not verify the internal invocation of `getInstance()` and does
not use Redis-command call paths as the primary functional oracle.

## Functional oracles

For documented successful matches:

- the input tuple is emitted;
- the input tuple is acknowledged;
- it is not failed.

For documented non-matches:

- the input tuple is not emitted;
- the input tuple is acknowledged;
- it is not failed.

The `declareOutputFields` frame verifies the externally visible mapper
declaration delegation.

## Independence from adequacy feedback

Before and during this frozen T_BB implementation:

- JaCoCo was not run;
- PIT was not run;
- native Apache Storm tests were not used.

Coverage and mutation measurements are intentionally deferred until after
this executable baseline is committed and frozen.

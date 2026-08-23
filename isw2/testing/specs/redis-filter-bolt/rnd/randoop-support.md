# RedisFilterBolt Randoop generation support

## Purpose

The random automatic suite must remain independent from the manually designed
T_BB and T_CF suites and from adequacy and mutation feedback.

A target-only Randoop feasibility run showed that RedisFilterBolt could not be
constructed normally because its public constructors require a
`RedisFilterMapper`.

`RedisFilterMapper` is a public production interface. The only concrete
production implementation located in the repository is the private nested
`WhitelistWordFilterMapper` used by the Redis example, and its factory method
is private as well. It is therefore unavailable to Randoop when generation is
restricted to public members.

## Minimal support decision

A single test-only concrete implementation is introduced:

`RedisFilterMapperRandoopSupport`

The support class:

- is not a JUnit test;
- contains no `@Test`;
- contains no assertion;
- contains no expected result for RedisFilterBolt;
- contains no Mockito usage;
- contains no coverage information;
- contains no mutation information;
- does not reuse T_BB or T_CF inputs;
- does not connect to Redis.

It exists only to make the public RedisFilterBolt constructors reachable by an
automatic generator.

The mapper exposes the same Redis data-type descriptor abstraction required by
production and supplies neutral fixed tuple key/value strings. Its output-field
declaration is intentionally a no-op because this support stage concerns object
construction rather than manually specifying RedisFilterBolt behavior.

## Frozen first-stage strategy

Randoop configuration:

- Randoop 4.3.4;
- Java 25;
- random seed 20260820;
- desired random suite size N = 11, matching the frozen T_BB size;
- public members only;
- error-revealing tests disabled;
- generated tests must reference RedisFilterBolt;
- no manual editing of generated tests;
- no Line/Branch feedback during generation;
- no PIT feedback during generation;
- native Apache Storm tests are not used.

The Redis `prepare()` lifecycle method must not be invoked during generation,
because the random suite must not depend on a live Redis service.

## Deliberate limitation

No RedisFilterBolt subclass, fake JedisCommandsContainer, fake collector, or
other process-specific harness is introduced at this stage.

This is intentional.

The mapper-only support is frozen before observing the next random generation.
If it proves insufficient to produce meaningful tests, any additional support
will be treated as a distinct, explicitly documented second-stage decision
rather than being silently tailored to adequacy results.

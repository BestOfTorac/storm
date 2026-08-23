# RedisFilterBolt - Black-box specification basis

## Target

`org.apache.storm.redis.bolt.RedisFilterBolt`

Production source:

`external/storm-redis/src/main/java/org/apache/storm/redis/bolt/RedisFilterBolt.java`

The manual T_BB design is based primarily on the public documentation of the
production API.

Native Apache Storm tests are neither read nor reused for test design.

JaCoCo and PIT results are not available at this point.

## Publicly documented filtering contract

The RedisFilterBolt class documentation states that the bolt queries Redis and
filters out an input tuple when the requested key, field or value does not
exist.

When the requested item exists, the input tuple is forwarded to the default
stream.

The documented supported Redis data types are:

- STRING;
- HASH;
- SET;
- SORTED_SET;
- HYPER_LOG_LOG;
- GEO.

The documentation further specifies the interpretation of existence:

- STRING: existence of the key in the Redis key space;
- HASH: existence of the requested field;
- SET: existence of the requested value;
- SORTED_SET: existence of the requested field/member;
- HYPER_LOG_LOG: existence of the requested value;
- GEO: existence of the requested field/member.

For SET an additional key is documented as necessary.

## Environment choices

The public API provides constructors for:

- a single Redis/JedisPool environment;
- a Redis/JedisCluster environment.

Both are treated as valid environment choices of the same filtering
functionality.

## Output-field declaration

RedisFilterMapper publicly defines the output-field declaration used by the
filter bolt.

`declareOutputFields` is therefore included as a separate externally
observable functionality.

## Limited implementation knowledge

The production implementation had already been inspected during the
class-suitability pre-flight.

That fact is not hidden.

For T_BB, implementation information is not used to create branch-driven
categories or structural-coverage targets.

A limited implementation-derived mapping is permitted only where necessary to
construct a collaborator fixture.

In particular, the public documentation defines GEO semantically as
present/absent but does not define the exact Java return value representing
those two Redis states. The test fixture may therefore encode an absent GEO
result using an empty position list and a present result using at least one
non-null coordinate.

This fixture mapping is classified as:

`INFERRED_FROM_CODE`

It does not introduce a new functional category.

## Deliberately deferred cases

The initial black-box suite does not assign a test frame to:

- unsupported LIST processing;
- exceptions raised by Redis collaborators;
- missing SET additional key and its exact failure mode;
- null or empty tuple keys;
- distinct GEO implementation encodings such as null list, empty list and
  all-null list.

The public documentation does not establish sufficiently precise independent
oracles for those cases, or the distinction is primarily implementation
driven.

They remain eligible for later control-flow-guided or mutation-guided
evolution after T_BB has been frozen and measured.

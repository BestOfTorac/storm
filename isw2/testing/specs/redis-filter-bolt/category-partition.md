# RedisFilterBolt - Category Partition Specification

## Purpose

This document defines the initial manually designed black-box suite T_BB for:

`org.apache.storm.redis.bolt.RedisFilterBolt`

The suite is designed before executing structural coverage or mutation
analysis.

## Method

The design follows Category Partition.

Categories and representative choices are derived from documented,
externally observable functionality.

The objective at this stage is functional representation, not branch or line
coverage.

The resulting test frames deliberately avoid the full Cartesian product.

## F1 - Redis filtering

### Category C1 - Redis environment

Choices:

- E1: single Redis environment through JedisPool;
- E2: Redis Cluster environment through JedisCluster.

Both public constructor forms are represented in T_BB.

### Category C2 - documented Redis data type

Choices:

- D1: STRING;
- D2: HASH;
- D3: SET;
- D4: SORTED_SET;
- D5: HYPER_LOG_LOG;
- D6: GEO.

Constraint:

- HASH, SET, SORTED_SET and GEO use a valid additional structure key in the
  selected T_BB frames.

### Category C3 - semantic existence result

Choices:

- X1: requested item exists;
- X2: requested item does not exist.

The meaning of existence depends on C2 according to the public
RedisFilterBolt documentation.

### Category C4 - externally observable filtering outcome

Constraints:

- [X1] => input tuple is forwarded and acknowledged;
- [X2] => input tuple is not forwarded and is acknowledged.

Normal documented frames do not expect failure reporting.

### Type-specific representative Redis states

STRING:

- present: Redis existence result is true;
- absent: Redis existence result is false.

HASH:

- present: requested hash field exists.

SET:

- absent: requested value is not a member of the documented set key.

SORTED_SET:

- present: requested member has a rank;
- absent: requested member has no rank.

HYPER_LOG_LOG:

- absent boundary: count is 0;
- present boundary: count is 1.

GEO:

- absent semantic state: no usable coordinate is returned;
- present semantic state: at least one usable coordinate is returned.

The exact GEO collaborator representation is fixture information and is
recorded as INFERRED_FROM_CODE, not as a separate black-box category.

## F2 - Output-field declaration

### Category C5 - mapper declaration

Choice:

- M1: mapper provides an output-field declaration.

Expected behavior:

- RedisFilterBolt exposes the mapper-defined output-field declaration through
  its public `declareOutputFields` operation.

## Constraints and exclusions

No T_BB frame is introduced solely for:

- source-code branches;
- exception handlers;
- structural coverage;
- mutation operators;
- unsupported LIST behavior;
- unspecified invalid-input failure modes.

These may be considered only after the T_BB baseline has been frozen.

## Selected representative frames

Exactly eleven representative frames are selected.

They cover:

- both documented Redis environments;
- all six documented supported Redis data types;
- both high-level semantic outcomes, exists and does-not-exist;
- meaningful zero/one HYPER_LOG_LOG boundary values;
- both GEO semantic outcomes;
- public output-field declaration.

Not every combination of environment, data type and existence result is
selected because doing so would duplicate the same externally observable
functional behavior without adding a new category choice.

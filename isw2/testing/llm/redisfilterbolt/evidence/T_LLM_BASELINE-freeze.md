# RedisFilterBolt - T_LLM_BASELINE freeze

## Status

FROZEN

## Freeze timestamp

2026-08-24T13:36:02+02:00

## Target

org.apache.storm.redis.bolt.RedisFilterBolt

## Test suite

T_LLM_BASELINE

## Generation

Provider:

Microsoft 365 Copilot

Access mode:

WEB

Valid generation:

P1 attempt 02

No LLM repair was required.

## Cardinality

11 ordinary JUnit Jupiter tests

## Initial execution

Tests run: 11

Failures: 0

Errors: 0

Skipped: 0

BUILD SUCCESS

## Repeatability

5 / 5 successful executions

Each execution:

- 11 tests;
- 0 failures;
- 0 errors;
- 0 skipped.

## Frozen source

Execution source:

isw2/testing/src/test/java/it/uniroma2/isw2/storm/testing/redisfilterbolt/llm/RedisFilterBoltLLMTest.java

Archival baseline copy:

isw2/testing/llm/redisfilterbolt/baseline/RedisFilterBoltLLMTest.java

## SHA-256

B247D548F81DAA437A281A22F865C56810C4B0575368267F520FC26D64D17358

The execution source and archival baseline copy are byte-identical at freeze time.

## Experimental rule

From this freeze onward, T_LLM_BASELINE must not be modified using JaCoCo, PIT, previous-suite adequacy evidence, or other adequacy feedback.

Any later LLM refinement must be stored as a distinct derived suite and must not overwrite T_LLM_BASELINE.

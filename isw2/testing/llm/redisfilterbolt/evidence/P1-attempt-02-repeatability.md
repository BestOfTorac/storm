# RedisFilterBolt T_LLM - Repeatability evidence

## Candidate

P1 attempt 02

## Timestamp

2026-08-24T13:33:04+02:00

## Test class

it.uniroma2.isw2.storm.testing.redisfilterbolt.llm.RedisFilterBoltLLMTest

## Cardinality

11 ordinary JUnit Jupiter tests

## Repeatability protocol

The same unmodified LLM-generated test source was executed five consecutive times.

Each execution ran only RedisFilterBoltLLMTest through Maven Surefire.

## Results

Run 1: PASS - 11 tests, 0 failures, 0 errors, 0 skipped

Run 2: PASS - 11 tests, 0 failures, 0 errors, 0 skipped

Run 3: PASS - 11 tests, 0 failures, 0 errors, 0 skipped

Run 4: PASS - 11 tests, 0 failures, 0 errors, 0 skipped

Run 5: PASS - 11 tests, 0 failures, 0 errors, 0 skipped

## Repeatability

5 / 5 successful runs

## Source integrity

SHA-256 before repeatability:

B247D548F81DAA437A281A22F865C56810C4B0575368267F520FC26D64D17358

SHA-256 after repeatability:

B247D548F81DAA437A281A22F865C56810C4B0575368267F520FC26D64D17358

Source unchanged:

True

## Interpretation

The T_LLM candidate is deterministic under the five-run execution gate.

No LLM repair was required.

The suite is ready to be frozen as T_LLM_BASELINE before structural coverage and mutation testing.

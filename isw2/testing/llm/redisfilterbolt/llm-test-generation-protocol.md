# RedisFilterBolt - LLM test generation protocol

## Status

This document preregisters the LLM test-generation experiment for:

`org.apache.storm.redis.bolt.RedisFilterBolt`

It is created before the first LLM interaction used to generate the RedisFilterBolt experimental test suite.

Preregistration timestamp:

`2026-08-24T12:56:57+02:00`

Repository baseline commit:

`89ad95215fd3f9ebeca6597742f6e5d0f1c989c0`

Branch:

`isw2-project`

---

## 1. Objective

The experiment evaluates an independently generated LLM test suite for the Apache Storm 3.0.0 production class:

`org.apache.storm.redis.bolt.RedisFilterBolt`

The baseline LLM suite is named:

`T_LLM`

The experiment compares LLM-based test generation with the other RedisFilterBolt testing approaches while preventing their adequacy results from guiding baseline LLM generation.

---

## 2. Test cardinality

The preregistered target is:

`N = 11`

The generated baseline must contain exactly 11 ordinary JUnit Jupiter test methods.

Parameterized and dynamic tests are intentionally excluded so that these values correspond directly:

- 11 planned logical cases;
- 11 implemented test methods;
- 11 executed tests;
- 11 tests reported by the build and CI.

The value 11 is fixed before LLM generation.

It matches the independently frozen RedisFilterBolt black-box suite (`T_BB`) and the controlled-size random suite (`T_RND`), enabling a direct same-cardinality comparison among manual black-box, random, and LLM-based generation.

The control-flow and EvoSuite suites are evaluated separately and are not artificially resized to 11.

---

## 3. Technical environment

Production project:

`Apache Storm 3.0.0`

Target Java version:

`Java 25`

Test module:

`isw2/testing`

Test framework:

`JUnit Jupiter`

Mocking framework:

`Mockito`, only when justified by the behavior under test.

Expected package:

`it.uniroma2.isw2.storm.testing.redisfilterbolt.llm`

Expected main generated test class:

`RedisFilterBoltLLMTest`

LLM provider:

`Microsoft 365 Copilot`

Access mode:

`WEB`

The exact model/mode label actually exposed by the Microsoft 365 Copilot interface must be recorded immediately before the first experimental prompt.

If the underlying model is not exposed, record:

`NOT EXPOSED`

IntelliJ IDEA remains the development environment used to inspect, compile, run, and maintain the project and generated tests.

---

## 4. Context-isolation policy

A fresh Microsoft 365 Copilot conversation must be opened for this experiment.

The LLM may intentionally receive initially only:

1. `external/storm-redis/src/main/java/org/apache/storm/redis/bolt/RedisFilterBolt.java`;
2. `isw2/testing/pom.xml`.

Additional Apache Storm production classes may be supplied only when required to resolve production signatures, types, collaborators, or behavior needed for compilation or valid test design.

Every additional production artifact supplied must be recorded in the context manifest.

Previously created RedisFilterBolt experimental tests must not intentionally be provided as generation context.

Excluded suites and evidence include:

- `redisfilterbolt/bb`;
- `redisfilterbolt/cf`;
- `redisfilterbolt/rnd`;
- `redisfilterbolt/es`;
- native Apache Storm tests;
- `isw2/testing/results/redis-filter-bolt`;
- JaCoCo reports;
- PIT reports;
- mutation survivors;
- uncovered-line or uncovered-branch information;
- adequacy comparisons;
- descriptions of specific gaps found by previous suites.

---

## 5. Effective baseline prompt strategy

The baseline generation uses one structured zero-shot prompt:

`P1-effective-analysis-and-generation`

P1 asks Microsoft 365 Copilot to:

1. analyze the supplied production class independently;
2. identify meaningful observable responsibilities, inputs, collaborators, boundary/error situations, and testability constraints;
3. design exactly 11 functionally distinct test cases;
4. implement those 11 cases as exactly 11 ordinary JUnit Jupiter test methods.

The prompt must not enumerate the desired Redis data-type branches or reveal cases discovered by previous manual, random, EvoSuite, JaCoCo, or PIT work.

The purpose is to measure what the LLM derives from production information rather than its ability to translate a human-authored test plan.

The exact submitted P1 text is preserved under:

`isw2/testing/llm/redisfilterbolt/prompts/P1-effective-analysis-and-generation.txt`

---

## 6. Baseline generation constraints

The generated suite must:

- contain exactly 11 ordinary `@Test` methods;
- use Java 25;
- use JUnit Jupiter;
- use Mockito only when useful for clean isolation;
- avoid a real Redis server;
- avoid external network/service dependencies;
- avoid sleep-based synchronization;
- remain deterministic;
- avoid direct testing of private methods through reflection;
- avoid production-code modifications;
- avoid native Apache Storm tests as reference material;
- use clear observable oracles;
- avoid duplicate or near-duplicate scenarios;
- avoid tests whose only purpose is to exercise implementation details.

The LLM may create helper methods or nested support code when useful, but those helpers do not count as tests.

---

## 7. Technical repair policy

Repair interactions are permitted only when the generated baseline:

- does not compile;
- cannot execute;
- contains invalid production API usage;
- fails because of an LLM-generated technical mistake;
- is nondeterministic.

Repair prompts may receive only the minimum information needed to repair the technical defect, such as:

- compiler diagnostics;
- runtime exceptions/errors;
- failing assertion information;
- relevant production API signatures or production classes.

Repair prompts must not receive:

- JaCoCo feedback;
- PIT feedback;
- uncovered lines or branches;
- surviving mutants;
- Mutation Score;
- Test Strength;
- results from BB, CF, RND, or ES.

Repairs should preserve the functional intent and cardinality of the baseline whenever possible.

Any replacement of a generated logical case must be explicitly documented.

Silent manual semantic repair is not allowed. Purely mechanical integration changes, if unavoidable, must be recorded.

---

## 8. Baseline validity criteria

Before adequacy measurement, `T_LLM` must satisfy:

- exactly 11 logical test cases;
- exactly 11 ordinary executable JUnit tests;
- successful compilation on Java 25;
- exactly 11 executed tests;
- 0 failures;
- 0 errors;
- 0 skipped or ignored tests;
- no native Apache Storm test execution;
- no unintended external service dependency;
- deterministic execution.

The valid suite must then be executed five consecutive times.

Required repeatability:

`5 / 5 successful runs`

Failures must be investigated before baseline freeze.

---

## 9. Freeze rule

JaCoCo and PIT must not be executed on `T_LLM` until:

1. generation is complete;
2. any required technical repairs are complete;
3. all 11 tests pass;
4. repeatability succeeds 5/5.

Before adequacy measurement, the final baseline source must be fingerprinted with SHA-256 and committed.

The freeze establishes:

`T_LLM_BASELINE`

After freeze, JaCoCo or PIT feedback must not be used to modify `T_LLM_BASELINE`.

Any later adequacy-guided evolution must be stored as a separate derived suite.

---

## 10. Structural adequacy measurement

Only after baseline freeze, JaCoCo will measure `T_LLM` independently.

Primary structural metrics:

1. Line Coverage;
2. Branch Coverage.

The measurement must target `RedisFilterBolt` under the same methodology used for the other RedisFilterBolt suites.

A denominator mismatch with the established RedisFilterBolt measurements must be investigated before cross-suite comparison.

---

## 11. Mutation testing

Only after baseline freeze, PIT will evaluate `T_LLM`.

Planned mutation configuration:

- PIT `1.25.8`;
- mutators: `DEFAULTS`;
- target: `org.apache.storm.redis.bolt.RedisFilterBolt`;
- Java 25;
- native Apache Storm tests excluded.

For project-level comparability, the produced mutation population must be compared with the established RedisFilterBolt reference population:

`12 mutants`

Exact mutant-population identity is a methodological control adopted in this project.

Evidence must preserve at least:

- generated;
- killed;
- survived;
- no coverage;
- timed out / infrastructure statuses when relevant;
- Mutation Score;
- Test Strength.

---

## 12. Optional mutation-guided LLM refinement

Any adequacy-guided LLM refinement is strictly post-baseline.

If PIT reveals meaningful surviving or no-coverage mutants, a separate mutation-guided interaction may be performed only after `T_LLM_BASELINE` has been frozen and measured.

The derived suite must not overwrite the baseline.

It must be named and documented separately, for example:

`T_LLM_MT_REFINED`

Where possible, cardinality remains `N = 11`: a refined case should replace or strengthen an existing case rather than simply increasing suite size.

The refinement must remain functionally defensible and must not create tests whose only rationale is killing a particular mutant.

---

## 13. Feedback-control rule

The following are ex-post evaluation evidence and must not guide baseline `T_LLM` generation:

- T_BB adequacy results;
- T_CF adequacy results;
- mutation-baseline results;
- T_RND adequacy results;
- T_ES adequacy results;
- future T_LLM JaCoCo results;
- future T_LLM PIT results.

The purpose is to preserve `T_LLM` as an independently generated LLM baseline.

---

## 14. Evidence to preserve

The experiment should preserve:

- this preregistration protocol;
- exact P1 prompt;
- complete P1 response;
- context manifest;
- provider and access mode;
- exact model/mode label exposed by the UI;
- interaction timestamp;
- repository commit;
- generated Java source;
- every repair prompt and response, if any;
- compilation diagnostics;
- initial execution evidence;
- five-run repeatability evidence;
- SHA-256 fingerprint;
- freeze commit;
- CI evidence;
- JaCoCo evidence;
- PIT evidence;
- cross-suite comparison evidence.

---

## 15. Interpretation boundaries

Higher structural coverage must not automatically be interpreted as stronger fault-detection capability.

Mutation adequacy is evaluated separately after freeze.

A low coverage or mutation score does not invalidate the LLM experiment; it is an observed result of the independently generated baseline.

The experiment evaluates one generation under a controlled prompt/context setup. LLM generation is nondeterministic, so the result must not be presented as a universal performance estimate for Microsoft 365 Copilot or for LLM-based test generation in general.

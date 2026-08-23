# UIHelpers - LLM test generation protocol

## Status

This document preregisters the LLM test-generation experiment for:

`org.apache.storm.daemon.ui.UIHelpers`

It is created before the first LLM interaction used to generate the
experimental test suite.

Preregistration timestamp:

`2026-08-23T17:14:10+02:00`

Repository baseline commit:

`e2c1bba35e5ab8529a6f3b415cfe35714c81d65b`

Branch:

`isw2-project`

---

## 1. Objective

The experiment evaluates an independent test suite generated with GitHub
Copilot for the Apache Storm 3.0.0 production class:

`org.apache.storm.daemon.ui.UIHelpers`

The baseline LLM suite is named:

`T_LLM`

The experiment is intended to compare LLM-based test generation with the
other UIHelpers testing approaches without using their adequacy results to
guide baseline LLM generation.

---

## 2. Test cardinality

The preregistered target is:

`N = 35`

The logical test plan must contain exactly:

`T01 ... T35`

The implementation target is also exactly 35 ordinary JUnit test methods.

Parameterized and dynamic tests are intentionally avoided in the baseline
suite so that these values correspond directly:

- 35 planned logical cases;
- 35 implemented test methods;
- 35 executed tests;
- 35 tests reported by the build and CI.

The value 35 is fixed before LLM generation.

It is consistent with the initial manual UIHelpers suite and the Randoop
random suite, which both use 35 tests.

The EvoSuite experiment is treated separately because the generator produced
43 raw methods and compatibility validation produced a final target of 37
valid methods. EvoSuite is not artificially reduced to 35.

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

`it.uniroma2.isw2.storm.testing.uihelpers.llm`

GitHub Copilot is used from IntelliJ IDEA.

Before the first experimental prompt, the following metadata must be
recorded:

- IntelliJ IDEA version;
- GitHub Copilot plugin version;
- Copilot model selected;
- date/time;
- repository commit;
- exact prompt text.

---

## 4. Context-isolation policy

A new Copilot conversation must be opened for this experiment.

The LLM may intentionally receive:

1. `UIHelpers.java`;
2. `isw2/testing/pom.xml`;
3. additional Apache Storm production classes only when required to
   understand signatures, types, collaborators, or production behavior.

Previously created UIHelpers tests must not intentionally be provided as
generation context.

Excluded experimental suites include:

- `uihelpers/bb`;
- `uihelpers/cf`;
- `uihelpers/mt`;
- `uihelpers/rnd`;
- `uihelpers/es`;
- `uihelpers/integration`.

Experimental results under:

`isw2/testing/results/uihelpers`

must not intentionally be supplied before baseline freeze.

In particular, the LLM must not be given:

- JaCoCo results;
- line-coverage results of other suites;
- branch-coverage results of other suites;
- PIT reports;
- Mutation Score values;
- Test Strength values;
- surviving-mutant lists;
- no-coverage-mutant lists;
- cross-suite adequacy comparisons.

Native Apache Storm tests must not be used as a generation reference.

---

## 5. Prompt sequence

Multiple prompt styles are preregistered.

### P1 - Functional analysis

Purpose:

Allow Copilot to independently analyze the production class before designing
tests.

Copilot should identify:

- public responsibilities;
- public methods/functions suitable for testing;
- meaningful classes of input;
- collaborators and dependencies;
- important observable behavior;
- testability risks.

P1 must not request Java test code.

P1 must not yet request the final set of 35 cases.

---

### P2 - Test planning

Purpose:

Generate exactly 35 candidate test cases based on the preceding functional
analysis.

Required identifiers:

`T01 ... T35`

For every case Copilot must specify:

1. test ID;
2. public method/functionality;
3. scenario/input;
4. expected result/oracle;
5. collaborators that may require mocking;
6. short functional justification.

P2 must not generate Java implementation code.

---

### P3 - Plan critique and refinement

Purpose:

Use a different prompt style to have the LLM critically review its own test
plan.

The review must consider:

- duplicates;
- near-duplicates;
- weak or ambiguous oracles;
- invented or nonexistent APIs;
- nondeterministic behavior;
- unnecessary external dependencies;
- fragile timing assumptions;
- excessive implementation coupling;
- redundant scenarios;
- low functional significance.

The refined plan must still contain exactly:

`T01 ... T35`

After P3, the logical test plan is frozen.

---

### P4 - Code generation

Purpose:

Implement the frozen 35-case plan.

The generated suite must contain exactly 35 ordinary JUnit test methods.

Constraints:

- Java 25;
- JUnit Jupiter;
- Mockito only when useful;
- deterministic execution;
- no external network dependency;
- no sleep-based synchronization;
- no direct testing of private methods through reflection;
- no production-code modifications;
- no native Apache Storm tests;
- no coverage-guided additions;
- no mutation-guided additions.

P4 must implement the frozen T01-T35 plan rather than create a different
test plan.

---

### P5 - Repair prompts

P5 is optional.

It is used only if the generated suite fails to compile, execute, or remain
deterministic.

Repair prompts may receive:

- compiler diagnostics;
- runtime exception/error output;
- failing assertion information;
- production API/signature incompatibility information.

Repair prompts must not receive:

- JaCoCo feedback;
- PIT feedback;
- uncovered lines or branches;
- surviving mutants;
- Mutation Score;
- Test Strength;
- comparisons with other testing approaches.

Repairs should preserve the logical intent of T01-T35 whenever possible.

If a case is technically invalid and must be replaced, the replacement and
its justification must be explicitly recorded.

---

## 6. Baseline validity criteria

Before adequacy measurement, T_LLM must satisfy:

- exactly 35 logical test cases;
- exactly 35 ordinary executable tests;
- successful compilation on Java 25;
- exactly 35 executed tests;
- 0 failures;
- 0 errors;
- 0 skipped or ignored tests;
- no native Apache Storm test execution;
- no unintended external network/service dependency;
- deterministic execution.

The valid suite must then be executed five consecutive times.

Required repeatability:

`5 / 5 successful runs`

Failures must be investigated before baseline freeze.

---

## 7. Freeze rule

JaCoCo and PIT must not be executed on T_LLM until:

1. generation is complete;
2. required technical repairs are complete;
3. all 35 tests pass;
4. repeatability succeeds 5/5.

Before adequacy measurement, the final baseline source must be fingerprinted
with SHA-256 and committed.

The freeze establishes:

`T_LLM_BASELINE`

After freeze, JaCoCo or PIT feedback must not be used to modify the baseline
T_LLM suite.

Any later adequacy-guided evolution must be stored as a separate derived
suite rather than overwriting T_LLM_BASELINE.

---

## 8. CI policy

After T_LLM_BASELINE is frozen, the suite will be integrated into GitHub
Actions.

The intended CI gate is:

`35 tests, 0 failures, 0 errors, 0 skipped`

Existing UIHelpers suites remain independent.

JaCoCo and PIT are measurement activities and are not required on every CI
push.

---

## 9. Structural adequacy measurement

Only after baseline freeze, JaCoCo will measure T_LLM independently.

The two primary structural adequacy metrics are:

1. Line Coverage;
2. Branch Coverage.

For comparability with the existing UIHelpers measurements, the expected
structural denominators are:

- 1122 lines;
- 242 branches.

A denominator mismatch must be investigated before cross-suite comparison.

Method and instruction coverage may also be preserved as supporting
measurements.

---

## 10. Mutation testing

Only after baseline freeze, PIT will evaluate T_LLM.

Planned mutation configuration:

- PIT 1.25.8;
- `DEFAULTS` mutator set;
- target:
  `org.apache.storm.daemon.ui.UIHelpers`;
- Java 25;
- native Apache Storm tests excluded.

For project-level experimental comparability, the produced mutation
population will be compared with the established reference population:

`338 mutants`

Exact mutant-population identity is a methodological control adopted in this
project and is not stated as an independent professor requirement.

The mutation evidence will include:

- generated;
- killed;
- survived;
- no coverage;
- Mutation Score;
- Test Strength;
- relevant infrastructure-error statuses.

---

## 11. Feedback-control rule

The following data are ex-post evaluation evidence and must not guide
baseline T_LLM generation:

- T_BB adequacy results;
- T_CF adequacy results;
- T_MT mutation results;
- T_RND adequacy results;
- T_ES adequacy results;
- future T_LLM JaCoCo results;
- future T_LLM PIT results.

The purpose is to preserve T_LLM as an independently generated LLM baseline,
not a manually adequacy-tuned suite.

---

## 12. Evidence to preserve

The experiment should preserve, where practical:

- this preregistration protocol;
- P1 exact prompt;
- P1 complete response;
- P2 exact prompt;
- P2 complete response;
- P3 exact prompt;
- P3 complete response;
- P4 exact prompt;
- P4 generated code/response;
- every P5 repair prompt;
- every P5 repair response;
- Copilot model/plugin metadata;
- generated Java source;
- compilation diagnostics;
- initial execution result;
- five-run repeatability evidence;
- SHA-256 fingerprints;
- freeze commit;
- CI evidence;
- JaCoCo evidence;
- PIT evidence;
- cross-suite comparisons.

---

## 13. Interpretation boundaries

Higher structural coverage must not automatically be interpreted as stronger
fault-detection capability.

Mutation adequacy is evaluated separately after freeze.

Likewise, a low adequacy value does not invalidate the LLM experiment.

The objective is to measure and compare independently produced testing
approaches rather than tune all approaches toward the same score.

---

## 14. Planned sequence

The preregistered sequence is:

1. commit this protocol;
2. open a fresh Copilot conversation;
3. record Copilot environment/model metadata;
4. execute P1;
5. preserve P1 prompt and complete response;
6. execute P2;
7. preserve P2 prompt and complete response;
8. execute P3;
9. freeze the 35-case logical plan;
10. execute P4;
11. use P5 only if technically necessary;
12. validate exactly 35 executable tests;
13. execute five-run repeatability;
14. fingerprint the generated baseline;
15. commit/freeze T_LLM_BASELINE;
16. integrate T_LLM into CI;
17. execute JaCoCo;
18. execute PIT;
19. compare T_LLM with the other UIHelpers suites;
20. preserve evidence for the final report.

No adequacy measurement after item 16 may influence the frozen baseline suite.

---

## 15. Preregistration statement

At the time this protocol is committed:

- no Copilot prompt for the UIHelpers T_LLM experiment has yet been executed;
- no LLM-generated UIHelpers baseline test has been accepted;
- T_LLM cardinality is fixed at 35;
- context-isolation rules are fixed;
- prompt sequence P1-P5 is fixed;
- validity criteria are fixed;
- five-run repeatability is fixed;
- the no-adequacy-feedback-before-freeze rule is fixed.

Any protocol change made after the first LLM interaction must be recorded as
an explicit amendment rather than silently rewriting this preregistration.

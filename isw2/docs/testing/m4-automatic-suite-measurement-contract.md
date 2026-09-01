# M4 Automatic Suite Measurement Contract

## 1. Purpose

This document freezes the measurement protocol used to compare the automatically
generated Randoop (RND), EvoSuite (ES), and LLM suites associated with the C0-C4
versions of the two target classes:

- org.apache.storm.daemon.ui.UIHelpers
- org.apache.storm.redis.bolt.RedisFilterBolt

The protocol is frozen before the final JaCoCo, PIT and Sonar measurements are
observed. Measurement results must not be used to regenerate, repair, prune or
otherwise modify a frozen generated suite.

---

## 2. Experimental units

The comparison considers:

- Classes: UIHelpers, RedisFilterBolt
- Variants: C0, C1, C2, C3, C4
- Techniques: RND, ES, LLM

C0 is the original class and provides the technique-specific baseline where a
corresponding frozen automatic suite exists.

C1-C4 are the refactored variants produced under the experimental information
conditions defined by the M4 protocol.

A Class x Variant x Technique tuple is one experimental unit.

---

## 3. Frozen-suite integrity

Before every measurement the canonical generated source files must pass a
SHA-256 integrity gate.

No generated test may be edited after freeze.

Any technically necessary staging operation must satisfy:

    canonical SHA256 == staged SHA256

If this equality does not hold, the measurement is invalid.

---

## 4. Validity dimensions

Compilation validity and execution validity are reported separately.

Possible execution classifications include:

- PASS_NATIVE
- FAIL_NATIVE_TEST
- BLOCKED_NATIVE_FRAMEWORK
- PASS_COMPATIBILITY
- PARTIAL_COMPATIBILITY_FRAMEWORK
- FAIL_COMPATIBILITY_TEST
- NOT_APPLICABLE

A framework/tool incompatibility must not be reported as a failed test oracle.

A value that cannot be measured under the frozen protocol is reported as N/A,
not as zero.

---

## 5. Measurement environments

### Native environment

The preferred dynamic measurement environment is the project-native environment:

- Apache Storm 3.0.0
- Java 25

RND and LLM suites are measured natively when valid.

### EvoSuite compatibility environment

EvoSuite 1.2.0 has documented compatibility limitations with the Java 25
bytecode used by the native project.

Where native EvoSuite execution is blocked by the EvoSuite runtime/framework,
the already frozen suite may additionally be evaluated in the documented
generation-compatible environment.

For RedisFilterBolt this environment is:

- Java 11
- Apache Storm 2.7.1 compatibility dependencies
- EvoSuite 1.2.0
- audited Java-11-safe dependency classpath

A compatibility-environment result does not replace or conceal the native
portability result. Both are retained.

Metrics produced in different execution environments must carry the environment
label and must not be silently treated as identical measurements.

---

## 6. Dynamic coverage metrics

JaCoCo measurements record, for the target class only:

- LINE_MISSED
- LINE_COVERED
- LINE_TOTAL
- LINE_COVERAGE_PERCENT

- BRANCH_MISSED
- BRANCH_COVERED
- BRANCH_TOTAL
- BRANCH_COVERAGE_PERCENT

- METHOD_MISSED
- METHOD_COVERED
- METHOD_TOTAL
- METHOD_COVERAGE_PERCENT

Definitions:

    LINE_TOTAL = LINE_MISSED + LINE_COVERED

    BRANCH_TOTAL = BRANCH_MISSED + BRANCH_COVERED

    METHOD_TOTAL = METHOD_MISSED + METHOD_COVERED

Coverage percentages use the corresponding COVERED / TOTAL ratio.

Coverage is always scoped to the target production class, not to the whole
Storm module.

A measurement obtained from a suite with runtime failures must be explicitly
marked PARTIAL and must not be presented as equivalent to a complete passing
suite.

---

## 7. Mutation metrics

PIT measurements record:

- TOTAL_MUTANTS
- KILLED
- SURVIVED
- NO_COVERAGE
- TIMED_OUT
- OTHER_STATUS
- MUTATION_SCORE_PERCENT
- TEST_STRENGTH_PERCENT

Definitions:

    MUTATION_SCORE =
        KILLED / TOTAL_MUTANTS

    TEST_STRENGTH =
        KILLED / (KILLED + SURVIVED)

when the respective denominator is non-zero.

The raw mutant population may change between C0-C4 because the production
source changes. Therefore comparisons across variants emphasize Mutation Score
and Test Strength rather than absolute KILLED counts alone.

The complete status counts are still retained as evidence.

PIT requires a valid passing baseline for the measured suite/environment.

If no fully passing frozen suite exists in a usable measurement environment,
mutation metrics are N/A with a documented blocker.

Generated tests must not be repaired or pruned merely to allow PIT to run.

---

## 8. Structural metrics

The frozen test sources are measured independently from runtime validity.

For every experimental unit record:

- JAVA_FILES
- TEST_METHODS
- LOGIC_LOC
- SUPPORT_LOC
- TOTAL_LOC
- ASSERTION_LIKE
- ASSERTIONS_PER_TEST
- DESCRIPTIVE_TEST_NAMES
- OPAQUE_TEST_NAMES
- DESCRIPTIVE_NAME_PERCENT

LOC counts non-empty, non-comment Java source lines according to the same
counter for every technique.

LOGIC and SUPPORT are roles, not quality judgments.

Typical role assignment:

- Randoop RegressionTest / generated test body files -> LOGIC
- Randoop helper/support files -> SUPPORT
- EvoSuite *_ESTest.java -> LOGIC
- EvoSuite *_ESTest_scaffolding.java -> SUPPORT
- LLM test source -> LOGIC
- separately generated helper files, if any -> SUPPORT

If helper/fixture code is nested inside a single LLM test file and cannot be
separated reproducibly at file level, that file remains LOGIC and the limitation
is documented.

---

## 9. Assertion-like operations

ASSERTION_LIKE is a structural indicator of explicit test oracles.

The common counting protocol includes calls matching the supported testing
APIs, including:

- JUnit assert*
- assertThrows
- assertDoesNotThrow
- fail
- Hamcrest assertThat
- Mockito verify

Generator-internal instrumentation or plain production-method calls are not
counted as assertions solely because they may throw exceptions.

The exact same counting rules are applied to all suites.

ASSERTIONS_PER_TEST is:

    ASSERTION_LIKE / TEST_METHODS

when TEST_METHODS > 0.

This metric is interpreted cautiously because generated tests can encode
implicit exception-based behavior not represented by an explicit assertion.

---

## 10. Naming clarity

Naming is classified using a pre-defined syntactic rule.

A generated test name is OPAQUE when it matches generator-style patterns such
as:

- test0
- test1
- test001
- test002
- regressionTest0
- regressionTest1

or equivalent purely numeric/generated naming without behavioral semantics.

A name is DESCRIPTIVE when it contains meaningful behavioral/domain wording
beyond a generator sequence identifier.

The rule is applied mechanically first. Ambiguous cases are recorded rather
than manually relabeled to favor a technique.

DESCRIPTIVE_NAME_PERCENT is:

    DESCRIPTIVE_TEST_NAMES / TEST_METHODS

---

## 11. Sonar / maintainability evidence

Sonar analysis is performed on exact staged copies of the frozen sources.

Every Sonar unit must pass:

### Staging hash gate

    canonical SHA256 == staged SHA256

### Component gate

    expected test source components == analyzed test source components

The following evidence is retained when available:

- Class
- Variant
- Technique
- Role
- CanonicalPath
- CanonicalSHA256
- SonarPath
- ComponentKey
- IssueKey
- Rule
- Type
- IssueStatus
- LegacySeverity
- MaintainabilitySeverity
- Impacts
- CleanCodeAttribute
- CleanCodeAttributeCategory
- Line
- Message

Aggregate outputs include:

- OPEN_CODE_SMELLS
- LOGIC_CODE_SMELLS
- SUPPORT_CODE_SMELLS
- DISTINCT_RULES
- rule-category summary
- maintainability-severity summary
- Clean Code attribute/category summary

The issue-level evidence is the authoritative source; aggregate counts are
derived from it.

---

## 12. Comparison rules

Two complementary comparisons are produced.

### Vertical comparison

For each technique:

    C0 -> C1 -> C2 -> C3 -> C4

This assesses how the refactored production variant relates to the quality and
effectiveness of automatically generated tests.

### Horizontal comparison

For each variant:

    RND vs ES vs LLM

This compares generators on the same production variant.

No single metric determines an overall winner.

Coverage, mutation effectiveness, structural complexity, clarity,
maintainability and portability are reported as separate dimensions.

---

## 13. Delta rules

For numeric metric M:

    DELTA(Ci) = M(Ci) - M(C0)

A delta is computed only when:

- C0 and Ci both have valid values;
- the metric definition is identical;
- the execution environment/protocol is comparable.

Otherwise the delta is N/A with a reason.

---

## 14. Handling framework failures

Framework/tool failures are preserved as experimental evidence.

They are not converted into assertion failures.

Examples include:

- EvoSuite ASM unsupported class-file version
- EvoSuite sandbox/scaffolding initialization failures
- generated-framework portability incompatibilities

No repair is performed after suite freeze to remove these outcomes.

---

## 15. Current RedisFilterBolt validation facts before measurement

RND:
- C1-C4 compile and execute natively
- 44/44 tests PASS

LLM:
- C1-C4 compile and execute natively
- 44/44 tests PASS
- an initial summary-parser false negative was resolved by read-only inspection
  of the original execution logs

EvoSuite native:
- C1-C4 frozen suites compile against the native variant
- execution is blocked before test bodies by EvoSuite 1.2.0 / ASM incompatibility
  with Java 25 class-file major version 69

EvoSuite compatibility:
- C1: 8/8 PASS
- C2: 6/9 successful with 3 framework/scaffolding failure entries and
  0 assertion/oracle failures
- C3: 8/8 PASS
- C4: 9/9 PASS

The C2 compatibility failures are retained; the suite is not repaired.

---

## 16. Anti-feedback rule

JaCoCo, PIT, Sonar and structural-quality results obtained after this protocol
freeze must not be used to regenerate, modify, prune or repair any already
frozen C1-C4 automatic suite.

The measurements are observational outcomes of the experiment.
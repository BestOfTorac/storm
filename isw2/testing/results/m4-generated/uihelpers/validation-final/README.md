# UIHelpers C1-C4 automatic-suite validation - final consolidated record

Repository HEAD: 6fb84f954982999eb1f11393c58ef0a5028db284

## Experimental policy

All automatic suites are retained exactly as generated.

- no generated test repaired;
- no generated test rewritten;
- no failing test removed;
- no suite regenerated using validation feedback;
- no coverage feedback used;
- no PIT/mutation feedback used.

## Randoop

C1, C2, C3 and C4 each contain 35 frozen tests.

All four suites compiled and executed successfully against their corresponding
native Java 25 variants:

- C1: 35/35 PASS
- C2: 35/35 PASS
- C3: 35/35 PASS
- C4: 35/35 PASS

## LLM

All four suites contain exactly 35 frozen tests.

Native validation:

- C1: production PASS; test compilation FAIL.
  Generated generic type mismatch:
  Map<String,Map<String,Object>> cannot be converted to
  Map<String,Map<String,Long>>.

- C2: compilation PASS; 34/35 tests PASS.
  One generated oracle expected %20 for a URL space while the implementation
  produced +.

- C3: compilation PASS; 32/35 tests PASS.
  Three generated oracle mismatches:
  one sanitizeStreamName expectation and two URL-space encoding expectations.

- C4: production PASS; test compilation FAIL.
  Generated generic type mismatch:
  Map<String,Object> cannot be converted to Map<String,Double>.

No LLM failure was repaired.

## EvoSuite

Generation protocol:

- EvoSuite 1.2.0;
- Java 11;
- Storm 2.7.1 compatibility surrogate;
- five compatibility transformations;
- 159 Java-11-safe dependency entries;
- Clojure 1.10.3;
- BRANCH criterion;
- seed 20260820;
- search budget 15 seconds.

Frozen raw-suite sizes:

- C1: 43
- C2: 41
- C3: 48
- C4: 45

### Generation-environment execution

The frozen raw suites compile successfully in the reconstructed generation
environment, but not all generated tests pass:

- C1: 41/43 PASS
- C2: 35/41 PASS
- C3: 40/48 PASS
- C4: 38/45 PASS

Total: 154/177 passing test executions and 23 reported failure entries.

The 23 failure entries are classified as:

- 7 generated expectations for ServiceConfigurationError that were not met;
- 3 Clojure initialization failures during EvoSuite class reset/scaffolding;
- 8 EvoSuite sandbox setup failures;
- 5 other reset/security/runtime scaffolding failures.

Some failure entries refer to the same generated test method during different
setup/teardown/reset phases. Therefore 23 is a failure-entry count and must not
be described as 23 distinct generated test methods.

### Native Storm 3.0.0 portability

The frozen EvoSuite raw sources are not directly compilable against the native
Storm 3.0.0 / Java 25 variants.

After correcting the validation harness to include Clojure, all C1-C4 native
compilations fail on the Jetty API boundary, e.g. types from:

org.eclipse.jetty.ee10.servlet.*

versus the older:

org.eclipse.jetty.servlet.*

used by the Java-11 / Storm-2.7.1 surrogate.

This is recorded separately from generation-environment execution quality.

## Superseded validation attempts

The earlier EvoSuite result reporting:

package clojure.lang does not exist

was caused by an incomplete validation harness classpath and is NOT treated as
an experimental failure of the generated suites.

That provisional result is superseded by:

1. corrected native portability validation, which exposes the Jetty API
   incompatibility; and
2. generation-environment validation, in which all four raw suites compile
   successfully and are then executed without repairs.

Previous logs are retained as audit evidence and are not deleted.

## Final policy

Failures remain part of the observed raw quality of the respective automatic
test-generation technique. No corrective transformation is applied before the
later coverage, mutation and structural-quality comparisons.
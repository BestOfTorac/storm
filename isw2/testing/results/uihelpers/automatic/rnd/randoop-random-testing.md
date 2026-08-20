# UIHelpers - T_RND Random Test Generation

## Objective

Generate and evaluate an automatic random test suite for:

`org.apache.storm.daemon.ui.UIHelpers`

The suite is identified as:

`T_RND`

## Experimental budget

The project specification refers to a budget of N manually defined and
automatically generated tests.

For this experiment N was operationalized as:

`N = 35`

This is a methodological choice.

The value was selected before observing automatic-test adequacy because
the initial manually designed T_BB suite also contains 35 tests.

This provides a same-size comparison between the initial manual suite
and the automatic random suite.

## Generator

- generator: Randoop
- version: 4.3.4
- generation approach: feedback-directed random generation
- target class: `UIHelpers`
- seed: `20260820`
- generation JVM: Java 25
- error-revealing tests included: no
- Apache Storm native tests used: no
- structural coverage used as generation feedback: no
- mutation information used as generation feedback: no
- manual test selection: no
- manual editing of generated tests: no

The generated suite was frozen before adequacy measurements.

## Java compatibility issue

The first Randoop attempt used Java 21.

Generation failed while loading `UIHelpers.class` because the exact
Storm production artifact had been compiled as class-file version 69,
which requires Java 25.

The target production bytecode was not changed or recompiled to an older
Java version.

Instead, Randoop was executed with Java 25 so that the same production
artifact used by the other experiments remained under test.

## Output-limit calibration

The experimental target was exactly 35 generated tests.

Randoop's output limit is a maximum budget and did not initially produce
35 final test methods.

Observed deterministic calibration with the same seed and unchanged
generation configuration:

| output-limit | generated test methods |
|---:|---:|
| 35 | 33 |
| 36 | 34 |
| 37 | 35 |

The first configuration that produced exactly N=35 was therefore:

`output-limit = 37`

Calibration used only the number of generated tests.

No line coverage, branch coverage or mutation result was inspected or
used to select the final output.

## Frozen suite

The final generated suite contains:

`35 tests`

and executes successfully:

`35 / 35 PASS`

The original generated Java source is stored under:

`isw2/testing/generated-tests/uihelpers/rnd/`

The sources are intentionally kept outside the normal Maven
`src/test/java` tree so they remain a separate experimental suite and
are not mixed automatically with T_BB, T_CF or T_MT.

SHA-256 hashes are stored in:

`frozen-suite-sha256.csv`

and were checked before and after adequacy measurements.

## Structural adequacy

Structural coverage was measured ex-post using JaCoCo 0.8.15 against
the exact `UIHelpers.class` extracted from the Apache Storm 3.0.0
`storm-webapp` artifact.

Only UIHelpers was analyzed.

Results:

| Metric | Covered | Total | Coverage |
|---|---:|---:|---:|
| Line | 115 | 1122 | 10.25% |
| Branch | 30 | 242 | 12.40% |

These results did not influence test generation or selection.

For comparison, the same-size manually designed T_BB suite contains 35
tests and previously obtained:

- line coverage: 39.13%
- branch coverage: 35.12%

Therefore, at the same test-count budget, the Randoop suite reaches
substantially less of UIHelpers than the initial manually designed
Category Partition suite.

This is an experimental result, not a reason to regenerate T_RND.

## Mutation adequacy

Mutation testing was performed ex-post using:

- PIT 1.25.8
- mutators: DEFAULTS
- threads: 1
- target: UIHelpers only
- Apache Storm native tests: excluded

The mutation population was checked against the previously frozen
UIHelpers mutation population.

Both contain exactly:

`338 mutants`

with exact mutation-population identity.

T_RND results:

| Outcome | Value |
|---|---:|
| Generated mutants | 338 |
| Killed | 32 |
| Survived | 39 |
| No Coverage | 267 |
| Covered mutants | 71 |
| Mutation Score | 9.47% |
| Test Strength | 45.07% |
| No-Coverage fraction | 78.99% |

The high number of no-coverage mutants is consistent with the limited
structural reach of the random suite.

Among mutants that are actually reached by T_RND, 32 out of 71 are
killed.

## PIT execution note

PIT's console summary reported one test examined because the 35 Randoop
test methods are contained in one generated test-bearing JUnit 4 class.

A separate JUnit execution before PIT verified:

`35 / 35 PASS`

PIT also emitted a warning that JUnit 5 libraries were present on the
broader dependency classpath without the JUnit 5 PIT plugin.

This warning does not change the T_RND experiment: the selected Randoop
tests are JUnit 4 tests and were discovered and executed successfully.

## JaCoCo and PIT line-coverage values

JaCoCo is the standardized tool used in this project for structural
line and branch coverage.

JaCoCo reports:

`115 / 1122 lines = 10.25%`

PIT independently reported its own internal coverage information during
mutation analysis.

That PIT-internal figure is retained in the raw PIT evidence but is not
used as a replacement for the JaCoCo structural-coverage metric.

## Tooling issues encountered

During the measurement workflow two PowerShell compatibility issues were
found and corrected:

1. `System.IO.Path.GetRelativePath` was unavailable in the active
   PowerShell/.NET runtime. A compatible path-handling implementation was
   used instead.

2. PowerShell interpreted the colon following `$jacocoVersion` while
   constructing the Maven artifact coordinate. The variable was changed
   to `${jacocoVersion}` to preserve the complete JaCoCo coordinate.

Neither issue changed the generated test suite or its results.

The frozen SHA-256 hashes were verified after the corrections.

## Interpretation

T_RND demonstrates that the number of tests alone is not sufficient to
describe test quality.

With the same 35-test budget as T_BB, Randoop produced a valid and
reproducible suite, but the suite reaches a substantially smaller part
of the large UIHelpers class.

Its mutation results similarly show that most generated mutants are
outside the code reached by the random suite.

The suite is deliberately not regenerated after observing these
results. Doing so would allow adequacy measurements to influence the
random-generation experiment.

## Reproducibility artifacts

This directory stores:

- generation configuration and metadata;
- output-limit calibration history;
- frozen SHA-256 source hashes;
- JaCoCo CSV/XML and structural summary;
- PIT CSV/XML and mutation summary;
- remaining surviving mutants;
- this methodological document.

The Randoop binary itself is not versioned because its exact released
version and configuration are recorded and it can be obtained again
independently.

# UIHelpers - Mutation Testing Evolution

## Scope

Class under test:

`org.apache.storm.daemon.ui.UIHelpers`

The mutation-testing phase starts from the frozen control-flow suite T_CF.

The production class is taken from the exact Apache Storm 3.0.0
`storm-webapp` artifact.

Apache Storm native test JARs are excluded from the experiment.

## Toolchain

- PIT: 1.25.8
- PIT JUnit 5 plugin: 1.2.3
- JUnit Platform Launcher: 6.1.1
- Mutator set: DEFAULTS
- Execution threads: 1
- Java: project JDK 25 environment
- Target class: UIHelpers only

The same set of 338 mutations is used for every comparison.

## Preflight

Before the full experiment, PIT was executed with a one-mutant class limit.

The smoke mutant was generated in UIHelpers and was killed by the
control-flow-guided test suite.

This confirmed:

- PIT mutation instrumentation works;
- JUnit 5 tests are discovered correctly;
- UIHelpers is the mutable production class;
- Apache Storm native tests are not used.

## Infrastructure issues and resolutions

### Missing JUnit Platform Launcher

The first direct PIT CLI attempt failed with:

`NoClassDefFoundError: org/junit/platform/launcher/core/LauncherFactory`

Cause:

`pitest-junit5-plugin` did not place `junit-platform-launcher` on the
direct CLI tool classpath.

Resolution:

`org.junit.platform:junit-platform-launcher:6.1.1` was explicitly added
to the temporary PIT tool environment.

No production POM modification was required.

### Locale-sensitive percentage assertion

The first mutation-guided version of MT04 expected:

`25.000`

The Italian runtime locale formatted the value as:

`25,000`

The assertion was changed only to normalize the decimal separator before
comparison.

This preserves the semantic oracle and remains able to detect arithmetic
mutants.

### Mutation identity comparison

The first comparison script reported duplicate mutation identities.

Cause:

PIT stores mutation indexes and blocks under nested XML elements:

`indexes/index`

and

`blocks/block`

Resolution:

the comparison key was corrected to include the nested values and all
relevant mutation metadata.

The corrected comparison verified that T_CF, T_MT Batch 1 and T_MT
Batch 2 contain exactly the same 338 mutation points.

## T_CF mutation baseline

Input suite:

- T_BB: 35 tests
- additional CF tests: 10
- total T_CF: 45 tests

| Outcome | Value |
|---|---:|
| Generated | 338 |
| Killed | 115 |
| Survived | 61 |
| No Coverage | 162 |
| Mutation Score | 34.02% |
| Test Strength | 65.34% |

Covered mutants:

`115 + 61 = 176`

Test Strength:

`115 / 176 = 65.34%`

No-coverage proportion:

`162 / 338 = 47.93%`

PIT reports structural line coverage of:

`492 / 1122`

which is consistent with the frozen T_CF JaCoCo measurement.

## T_MT Batch 1

Four mutation-guided tests were added.

Main targets:

- JSONP boundary and serialization
- logviewer HTTPS and port-zero boundary
- Jetty HTTP connector configuration
- cluster CPU and memory percentage calculations

Suite size:

`45 + 4 = 49 tests`

Results:

| Outcome | T_CF | Batch 1 | Delta |
|---|---:|---:|---:|
| Killed | 115 | 131 | +16 |
| Survived | 61 | 49 | -12 |
| No Coverage | 162 | 158 | -4 |
| Mutation Score | 34.02% | 38.76% | +4.74 pp |
| Test Strength | 65.34% | 72.78% | +7.44 pp |

Exact status transitions included:

- SURVIVED -> KILLED: 15
- NO_COVERAGE -> KILLED: 1
- NO_COVERAGE -> SURVIVED: 3

## T_MT Batch 2

Four additional mutation-guided tests were added.

Main targets:

- SSL factory configuration
- CORS filter parameters
- supervisor arithmetic and generic resources
- Jetty/SSL port boundaries

Suite size:

`49 + 4 = 53 tests`

Results:

| Outcome | Batch 1 | Batch 2 | Delta |
|---|---:|---:|---:|
| Killed | 131 | 155 | +24 |
| Survived | 49 | 25 | -24 |
| No Coverage | 158 | 158 | 0 |
| Mutation Score | 38.76% | 45.86% | +7.10 pp |
| Test Strength | 72.78% | 86.11% | +13.33 pp |

Every one of the 24 additional kills in Batch 2 was a previously
surviving mutant.

Important examples include:

- multiple SSL configuration setter mutations in `mkSslConnector`;
- all four CORS initialization-parameter mutations;
- arithmetic mutations in `getPrettifiedSupervisorMap`;
- generic-resource mutations in `prettifyGenericResources`;
- Jetty and SSL boundary-condition mutations.

## Final T_MT

T_MT is defined as:

`T_MT = T_CF + mutation-guided tests`

Final composition:

- T_BB: 35
- CF additions: 10
- MT additions: 8
- total T_MT: 53 tests

All 53 tests pass.

Final mutation results:

| Outcome | T_CF | Final T_MT | Delta |
|---|---:|---:|---:|
| Generated | 338 | 338 | 0 |
| Killed | 115 | 155 | +40 |
| Survived | 61 | 25 | -36 |
| No Coverage | 162 | 158 | -4 |
| Mutation Score | 34.02% | 45.86% | +11.84 pp |
| Test Strength | 65.34% | 86.11% | +20.77 pp |

A total of eight mutation-guided tests therefore increased the number of
killed mutants by 40.

## Interpretation

The structural line coverage reported by PIT remains approximately:

`492 / 1122 = 44%`

while Test Strength grows from:

`65.34%`

to:

`86.11%`

This is an important result.

The mutation-guided evolution improves the ability of the suite to
distinguish incorrect behavior even when structural line coverage does
not increase significantly.

Mutation adequacy therefore captures information that cannot be inferred
from line coverage alone.

## Remaining survivors

Twenty-five mutants remain alive after T_MT.

They are intentionally preserved in:

`remaining-survivors.csv`

The largest remaining clusters include:

- `getClusterSummary`
- `mkSslConnector`
- `configFilters`
- `jettyCreateServer`

Smaller surviving clusters are also present in topology, supervisor,
Nimbus, generic-resource, uptime and JSON-related functionality.

The goal of this phase is not to force a 100% mutation score.

The final suite is stopped after two mutation-guided batches because the
additional tests produce a substantial and clearly measurable adequacy
improvement while remaining small, maintainable and explainable.

## Reproducibility

The repository stores:

- raw final PIT XML;
- raw final PIT CSV;
- remaining survivors;
- status transitions from T_CF to final T_MT;
- status transitions from Batch 1 to Batch 2;
- summary metrics;
- mutation-guided JUnit tests.

These artifacts are sufficient to reconstruct the mutation-testing
analysis for the final project report.

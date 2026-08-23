# RedisFilterBolt - T_ES mutation testing

## Scope

Target:

`org.apache.storm.redis.bolt.RedisFilterBolt`

Frozen suite:

`T_ES`

Suite size:

**9 EvoSuite-generated tests**

PIT measurement was performed only after the suite freeze and after the
official JaCoCo measurement.

## Configuration

Final valid PIT configuration:

- PIT: `1.25.8`
- mutators: `DEFAULTS`
- threads: `1`
- target class: `RedisFilterBolt`
- generated tests: `9`
- adapter: dedicated JUnit 4 suite-level adapter
- JUnit 5 PIT plugin: absent in the final valid run
- CLASSLIMIT: none
- Java argfile: not used
- Storm target: `3.0.0`
- Java: `25`

The adapter executes all nine frozen EvoSuite methods through the previously
validated Java 25 compatibility runner.

No generated test body or assertion was modified for mutation testing.

## Population comparability

The final run generated exactly **12 mutants**.

The mutant identities are exactly equal to both:

- the final manual-suite baseline;
- the T_RND mutation population.

Population differences:

`0`

Therefore the raw mutation results are directly comparable across these
suites.

## Official T_ES mutation result

| Metric | Result |
|---|---:|
| Total mutants | 12 |
| Killed | 2 |
| Survived | 1 |
| No coverage | 9 |
| Timed out | 0 |
| Mutation Score | 16.6667% |
| Test Strength | 66.6667% |

Mutation Score:

`2 / 12 = 16.6667%`

Test Strength:

`2 / (2 + 1) = 66.6667%`

## Interpretation

The main weakness of T_ES is reachability rather than oracle effectiveness.

Nine of the twelve mutants are classified as `NO_COVERAGE`. All nine belong
to the main `process()` logic.

The two killed mutants are constructor mutants at line 61. The suite therefore
detects the mutations affecting constructor validation.

The only covered survivor occurs at line 146 in `declareOutputFields()`. PIT
removes the call to `RedisFilterMapper.declareOutputFields`, but the generated
suite does not detect the behavioral difference.

Thus, among the three mutants actually exercised by the suite, two are killed,
which produces a Test Strength of 66.6667%. However, the large number of
unreached mutants keeps the overall Mutation Score low.

## Comparison

| Suite | Tests | Killed | Survived | No coverage | Mutation Score | Test Strength |
|---|---:|---:|---:|---:|---:|---:|
| T_MT | 15 | 11 | 1 | 0 | 91.6667% | 91.6667% |
| T_RND | 11 | 0 | 4 | 8 | 0.0000% | 0.0000% |
| T_ES | 9 | 2 | 1 | 9 | 16.6667% | 66.6667% |

For RedisFilterBolt no additional manual mutation-driven test was retained, so
the final manual T_MT suite contains the same 15 tests as T_CF.

T_ES performs better than T_RND in mutation effectiveness, but remains far
below the final manual suite.

## Relation with JaCoCo

Official structural adequacy remains the previously frozen JaCoCo result:

- Line Coverage: `16/45 = 35.5556%`
- Branch Coverage: `4/21 = 19.0476%`

PIT internally reported line coverage `17/45`. This value is retained only as
a PIT diagnostic and does not replace the official JaCoCo measurement because
the two tools use different coverage instrumentation/execution mechanisms.

## Execution notes

The first attempted PIT launch did not execute PIT because a Java argument file
contained a quoted classpath split across physical lines. Java interpreted the
classpath as the main class.

A later mixed-tooling direct invocation launched PIT but produced zero mutation
units, so no mutation result from that invocation was accepted.

A JUnit4-only historical PIT smoke with `CLASSLIMIT=1` then successfully
generated and executed one RedisFilterBolt mutant, proving the compatibility
path.

The final measurement removed CLASSLIMIT and generated the complete exact
12-mutant population. Only this final run is considered the official T_ES PIT
measurement.

## Artifacts

- `mutations.xml`: raw PIT XML report
- `mutations.csv`: raw PIT CSV report
- `tes-mutation-summary.csv`: T_ES aggregate metrics
- `tes-mutation-comparison.csv`: manual/RND/ES comparison
- `tes-mutant-transitions.csv`: exact per-mutant statuses across suites
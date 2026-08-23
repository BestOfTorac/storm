# RedisFilterBolt T_RND — mutation testing

## Scope

This measurement evaluates the frozen Randoop suite T_RND for:

`org.apache.storm.redis.bolt.RedisFilterBolt`

The suite contains 11 generated tests and was frozen before both coverage and
mutation measurements.

No T_RND test was changed in response to adequacy feedback.

## Configuration

- PIT: 1.25.8
- PIT JUnit 5 plugin: 1.2.3
- JUnit Platform Launcher: 6.1.1
- JUnit Vintage: 6.1.1
- mutators: `DEFAULTS`
- threads: 1
- target class: `RedisFilterBolt`
- target test class:
  `RedisFilterBoltRandomRegressionTest0`
- mutable production bytecode: exact `storm-redis:3.0.0` target class

The mutant population is exactly identical to the manual-suite baseline:
**12/12 mutants**, with identical class, method, line, mutator, description and
multiplicity.

## Execution result

Before PIT, the frozen T_RND suite passed 11/11 tests.

PIT discovered 11 tests and executed all 11 during discovery.

Mutation results:

| Status | Count |
|---|---:|
| KILLED | 0 |
| SURVIVED | 4 |
| NO_COVERAGE | 8 |
| TIMED_OUT | 0 |
| Total | 12 |

Raw Mutation Score:

`0 / 12 = 0.0000%`

Test Strength, calculated over mutants actually reached by the suite:

`0 / 4 = 0.0000%`

Therefore all four mutants reached by T_RND survived.

## Relationship with structural coverage

The official coverage measurement is the previously recorded JaCoCo result:

- Line Coverage: `22/45 = 48.8889%`
- Branch Coverage: `1/21 = 4.7619%`
- Method Coverage: `4/4 = 100.0000%`

PIT also printed its own internal line-coverage diagnostic of `23/45`.
This value is not used as the official adequacy metric. JaCoCo remains the
project-wide source for Line and Branch Coverage so that all suites are
compared using the same measurement tool.

The combination of 100% Method Coverage and only 4.7619% Branch Coverage is
consistent with the mutation result: the generated suite reaches the public
surface but exercises very little of the internal decision space.

## Surviving mutants

Four mutants were reached but not detected:

1. constructor, line 61 — removal of a conditional;
2. `declareOutputFields`, line 146 — removal of the mapper declaration call;
3. `process`, line 136 — removal of `collector.reportError`;
4. `process`, line 137 — removal of `collector.fail`.

The random regression suite therefore executes these areas without an oracle
capable of distinguishing the mutated behavior.

In particular, the test-only process support intentionally contains no Mockito
`verify` calls and no manually designed assertions. Adding such oracle logic
after observing PIT would contaminate the independence of T_RND, so no change
is made.

## No-coverage mutants

Eight of the twelve mutants received `NO_COVERAGE`.

This is consistent with the very low Branch Coverage measured for T_RND and
shows that random method invocation alone did not explore most of the target's
conditional behavior.

## Comparison with the manual suite

The frozen manual BB+CF suite uses the exact same 12-mutant population.

| Suite | Tests | Killed | Survived | No coverage | Mutation Score | Test Strength |
|---|---:|---:|---:|---:|---:|---:|
| T_CF manual | 15 | 11 | 1 | 0 | 91.6667% | 91.6667% |
| T_RND | 11 | 0 | 4 | 8 | 0.0000% | 0.0000% |

For the manual suite, the single survivor had previously been classified as
equivalent in the intended operational domain. Excluding that equivalent
mutant gives an adjusted manual Mutation Score of 100%.

For T_RND, excluding the same equivalent mutant from the global mutation
population would not change the interpretation: zero mutants are killed.

## PIT execution issue and solution

Two preliminary direct-CLI attempts produced no mutation feedback because PIT
stopped during pre-scan with zero mutation units.

The final successful configuration established that:

- `--mutableCodePaths` explicitly identifies the directory containing the
  copied production bytecode;
- PIT CLI `--classPath` receives individual classpath entries separated by
  commas;
- `--includeLaunchClasspath true` is enabled;
- JUnit Platform Launcher is explicitly available;
- JUnit Vintage provides execution of the Randoop-generated JUnit 4 tests.

With this configuration PIT created one mutation unit, discovered all 11 tests,
and generated the expected 12-mutant population.

The failed setup attempts did not generate mutation results and therefore did
not provide feedback capable of changing the frozen T_RND suite.

## Freeze policy

T_RND is an independently generated automatic suite.

Its low coverage, zero Mutation Score and zero Test Strength are experimental
results, not inputs for subsequent improvement of the random suite.

No mutation-guided T_RND additions are made.

## Raw artifact identity

Tracked raw `mutations.xml` SHA-256:

`E6C0520ED4CDFDA4F74078E30EB9FCBF5D25321B6F82CE71AA31A6FFB81F4B38`

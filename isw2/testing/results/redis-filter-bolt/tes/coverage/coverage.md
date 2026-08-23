# RedisFilterBolt - T_ES JaCoCo coverage

## Scope

Target class:

`org.apache.storm.redis.bolt.RedisFilterBolt`

Suite:

`T_ES`

Frozen suite size:

**9 tests**

Freeze commit:

`bfbd665ca0e7b6f5a7a8cb26bbbab58a222fa1b4`

## Measurement protocol

The measurement was performed only after the EvoSuite suite had been
officially frozen.

Environment:

- Apache Storm: `3.0.0`
- Java: `25`
- JaCoCo: `0.8.15`
- EvoSuite: `1.2.0`
- generated tests: `9`
- excluded tests: `0`
- native Storm tests: not used

The Java 25 EvoSuite compatibility layer is the same one validated before
freeze:

- `RuntimeSettings.useSeparateClassLoader=false`
- SecurityManager initialization/reset disabled in the compatibility scaffold
- `--add-opens=java.desktop/java.awt=ALL-UNNAMED`
- `--add-opens=java.base/java.net=ALL-UNNAMED`

All 9 tests passed while the JaCoCo agent was active.

No coverage result was used to alter, remove, add, or regenerate a test.

## Official coverage

| Metric | Covered | Total | Coverage |
|---|---:|---:|---:|
| Line | 16 | 45 | 35.5556% |
| Branch | 4 | 21 | 19.0476% |
| Method | 3 | 4 | 75.0000% |
| Instruction | 53 | 170 | 31.1765% |

The two official adequacy metrics selected for the experiment are Line
Coverage and Branch Coverage. Method and instruction coverage are retained as
secondary diagnostic information.

## Denominator comparability

The JaCoCo denominators are exactly the same as those used for the previous
RedisFilterBolt suites:

- lines: `45`
- branches: `21`
- methods: `4`
- instructions: `170`

This makes the cross-suite comparison structurally valid.

## Cross-suite comparison

| Suite | Tests | Line coverage | Branch coverage |
|---|---:|---:|---:|
| T_BB | 11 | 40/45 = 88.8889% | 16/21 = 76.1905% |
| T_CF | 15 | 45/45 = 100.0000% | 21/21 = 100.0000% |
| T_RND | 11 | 22/45 = 48.8889% | 1/21 = 4.7619% |
| T_ES | 9 | 16/45 = 35.5556% | 4/21 = 19.0476% |

Compared with T_RND, T_ES has:

- line coverage: `-13.3333` percentage points;
- branch coverage: `+14.2857` percentage points.

Compared with T_BB, T_ES has:

- line coverage: `-53.3333` percentage points;
- branch coverage: `-57.1429` percentage points.

## Interpretation

The frozen EvoSuite suite exercises fewer production lines than the random
suite, but reaches more branches.

A substantial part of the generated suite is concentrated on exceptional or
boundary executions, including null-driven execution paths, constructor
validation and output-field declaration. Consequently, some distinct control
decisions are exercised without traversing a large fraction of the normal
Redis filtering logic.

The generation-time EvoSuite BRANCH statistic is not used as the official
coverage result. The official value is the ex-post JaCoCo measurement obtained
on the actual Storm 3.0.0 / Java 25 target.

## Artifacts

- `jacoco.csv`: raw JaCoCo CSV report
- `jacoco.xml`: raw JaCoCo XML report
- `tes-coverage-summary.csv`: exact T_ES metrics
- `tes-coverage-comparison.csv`: BB/CF/RND/ES comparison
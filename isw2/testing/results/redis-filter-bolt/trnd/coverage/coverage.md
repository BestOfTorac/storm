# RedisFilterBolt T_RND — coverage measurement

## Freeze boundary

The random suite was frozen before any adequacy measurement in commit:

`b4961fb053e5907ca1578fd1484cba8f872fb184`

The frozen suite contains 11 Randoop-generated tests.

Generated-source SHA-256:

- wrapper: `ACF89B83E1952214ED98BAB2277EAB5E2EDE865FEF79F9C6E6B5A48DDAED18D0`
- test body: `29EE392097D0C0428CBD2FDF6435940878639CF529746DF7148754DDD5B5B75D`

No generated source was modified after the freeze.

## Measurement configuration

- target: `org.apache.storm.redis.bolt.RedisFilterBolt`
- Apache Storm production artifact: `storm-redis:3.0.0`
- JaCoCo: 0.8.15
- test execution: Maven Surefire
- tests executed: 11
- failures: 0
- errors: 0
- skipped: 0

The exact production bytecode used for the measurement is the same bytecode
used for the manual-suite measurements.

## T_RND coverage

| Metric | Covered | Total | Coverage |
|---|---:|---:|---:|
| Line | 22 | 45 | 48.8889% |
| Branch | 1 | 21 | 4.7619% |
| Method | 4 | 4 | 100.0000% |
| Instruction | 65 | 170 | 38.2353% |

The two official adequacy metrics are Line Coverage and Branch Coverage.

## Denominator comparability

The denominators are identical to those used for T_BB and T_CF:

- 45 lines;
- 21 branches;
- 4 methods;
- 170 instructions.

Therefore the results are directly comparable.

## Comparison with manual suites

| Suite | Tests | Line Coverage | Branch Coverage |
|---|---:|---:|---:|
| T_BB | 11 | 88.8889% | 76.1905% |
| T_CF | 15 | 100.0000% | 100.0000% |
| T_RND | 11 | 48.8889% | 4.7619% |

Relative to T_BB, T_RND covers:

- 18 fewer lines;
- 15 fewer branches;
- 40.0000 percentage points less Line Coverage;
- 71.4286 percentage points less Branch Coverage.

Relative to the evolved T_CF manual suite, T_RND covers:

- 23 fewer lines;
- 20 fewer branches;
- 51.1111 percentage points less Line Coverage;
- 95.2381 percentage points less Branch Coverage.

T_RND nevertheless reaches all 4 measured methods. Its low Branch Coverage
shows that reaching the public method surface does not imply substantial
exploration of the decisions inside `RedisFilterBolt`.

This result is observational only: the frozen T_RND suite is not modified in
response to the coverage measurement.

## Raw artifact identity

JaCoCo execution data SHA-256:

`E8B8CB388F3B38CD1767018EC8EE9C6E3736118DC76BC82E2A38305ED15B296A`

Tracked JaCoCo CSV SHA-256:

`7968E5B0126B3B3B2EC2117A3A9B8C19479AA6EE9813AE980A0E83E62D2715E6`

Tracked JaCoCo XML SHA-256:

`3DBC03A3B4E240DFF0EE6569F5580ED730EF9FEB96033CDEC12585D4D5F8839F`

Mutation testing is intentionally performed only after this coverage result is
recorded.

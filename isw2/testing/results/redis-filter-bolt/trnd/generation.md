# RedisFilterBolt T_RND — Randoop random suite freeze

## Scope

Target:

`org.apache.storm.redis.bolt.RedisFilterBolt`

The T_RND suite was generated independently from T_BB and T_CF.

No native Apache Storm tests, JaCoCo feedback, or PIT feedback were supplied
during generation or calibration.

## Generator configuration

- generator: Randoop 4.3.4
- Java: 25
- seed: 20260820
- target size: N = 11
- target-size basis: frozen T_BB size
- final output-limit: 17
- time limit per generation run: 30 seconds
- error-revealing tests: disabled
- public members only: enabled
- `prepare()`: omitted
- `cleanup()`: omitted
- live Redis: not used

## Testability support

A target-only feasibility probe showed that the public API did not provide a
constructible concrete `RedisFilterMapper`.

A first support stage therefore introduced only a test-only mapper.

With mapper-only support, Randoop could construct the target, but every
generated `process()` invocation was an exception regression because the
Storm collector/runtime infrastructure was absent.

A second support stage was therefore frozen before final generation. It
provides a process factory backed by:

- one mocked `JedisCommandsContainer`;
- one mocked `OutputCollector`;
- an override of the protected `getInstance()` seam.

Mockito is used only for infrastructure isolation.

The support contains no assertions, no `when(...).thenReturn(...)` stubbing,
no `verify(...)` calls, and no real Redis access.

## Deterministic size calibration

With the seed fixed, only Randoop's `output-limit` was varied.

| output-limit | generated tests |
|---:|---:|
| 8 | 6 |
| 9 | 7 |
| 10 | 7 |
| 11 | 7 |
| 12 | 8 |
| 13 | 8 |
| 14 | 9 |
| 15 | 9 |
| 16 | 10 |
| 17 | 11 |

The first exact-N candidate was therefore `output-limit = 17`.

The selection criterion was generated-test count plus the previously defined
generator-feasibility requirement that normal `process()` execution be
possible.

No Line/Branch Coverage or mutation result was used.

## Frozen candidate

The selected candidate contains exactly **11 tests**.

Structural signals observed before adequacy measurement:

- process-factory calls: 4
- `process()` calls: 1
- normal `process()` tests: 1
- expected-exception `process()` tests: 0
- `declareOutputFields()` calls: 1
- `prepare()` calls: 0
- `cleanup()` calls: 0

The two generated Java files were copied byte-for-byte. No generated line was
edited.

SHA-256:

- `RedisFilterBoltRandomRegressionTest.java`:
  `ACF89B83E1952214ED98BAB2277EAB5E2EDE865FEF79F9C6E6B5A48DDAED18D0`
- `RedisFilterBoltRandomRegressionTest0.java`:
  `29EE392097D0C0428CBD2FDF6435940878639CF529746DF7148754DDD5B5B75D`

## JUnit / Maven integration

Randoop 4.3.4 generated JUnit 4 tests.

The first Maven integration check returned `BUILD SUCCESS` but reported
`Tests run: 0`. A direct JUnit 4 execution of the same generated suite passed
11/11.

The dependency audit showed:

- `junit:junit:4.13.2` present;
- `org.junit.jupiter:junit-jupiter:6.1.1` present;
- `junit-jupiter-engine:6.1.1` resolved transitively;
- Surefire auto-selected the JUnit Platform provider;
- `junit-vintage-engine` was absent.

Therefore the testing POM was extended with the test-scoped
`org.junit.vintage:junit-vintage-engine` using the existing
`${junit.version}` property.

After this infrastructure change:

- RedisFilterBolt T_RND: **11/11 through Maven Surefire**;
- UIHelpers T_RND regression gate: **35/35 through Maven Surefire**.

The JUnit Vintage change is committed separately from the RedisFilterBolt
random-suite freeze.

## Freeze boundary

At the T_RND freeze boundary:

- generated tests: 11;
- generated-source edits: 0;
- manual test reuse: no;
- native test reuse: no;
- coverage feedback used: no;
- mutation feedback used: no.

Line/Branch Coverage and mutation testing are intentionally performed only
after this freeze.

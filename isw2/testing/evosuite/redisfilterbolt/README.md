# RedisFilterBolt - EvoSuite test suite (T_ES)

## Scope

Class under test:

`org.apache.storm.redis.bolt.RedisFilterBolt`

The suite in this directory is the frozen EvoSuite automatic-test suite used
for the ISW2 testing experiment.

No native Apache Storm test was used as generation input or as part of the
final T_ES execution.

## Generation configuration

EvoSuite generation was performed independently from the manual, random,
coverage and mutation results.

Configuration:

- EvoSuite: `1.2.0`
- generation target: `RedisFilterBolt`
- generation project version: Apache Storm `2.7.1`
- generation Java runtime: Java `11`
- criterion: `BRANCH`
- random seed: `20260820`
- search budget: `15 s`
- `new_statistics=false`

Storm 2.7.1 was used only as the EvoSuite generation environment because
EvoSuite 1.2.0 cannot instrument the Java 25 bytecode used by Storm 3.0.0.
A source/API compatibility audit established that the RedisFilterBolt source
and public API used for generation are compatible with the actual 3.0.0
target.

The natural EvoSuite result contains **9 generated test methods**. The suite
was not resized to match T_BB.

## Source-origin freeze

Raw EvoSuite output is retained unchanged under:

`isw2/testing/evosuite/redisfilterbolt/raw/`

Raw source SHA-256 values:

- `RedisFilterBolt_ESTest.java`: `B60503F59A7583C3AA88F1C068588B597E5D020982A986DBC9F224C8631841A8`
- `RedisFilterBolt_ESTest_scaffolding.java`: `E49E0BCEE64589CBD4A28B843988FD2AE33C055B97446A66EDE3B68267D71B7B`

The frozen executable test source is byte-identical to the raw generated test.

## Java 25 portability

The raw generated sources compile successfully against the actual
Apache Storm 3.0.0 dependency graph with Java 25.

The original EvoSuite runner cannot execute directly on that target because
EvoSuite 1.2.0 contains legacy runtime mechanisms incompatible with Java 25.

The compatibility investigation isolated the following issues sequentially:

1. `EvoClassLoader` / bundled ASM cannot parse class-file major version 69.
2. EvoSuite reflective access requires `java.desktop/java.awt` to be opened.
3. Java 25 no longer supports installation of the legacy SecurityManager.
4. EvoSuite reflective support requires `java.base/java.net` to be opened.

The frozen Java 25 compatibility layer therefore applies only these runtime
adaptations:

- bypass the EvoSuite separate instrumenting classloader by setting
  `RuntimeSettings.useSeparateClassLoader=false`;
- disable only
  `Sandbox.initializeSecurityManagerForSUT()` and
  `Sandbox.resetDefaultSecurityManager()` in the compatibility scaffold;
- retain the remaining EvoSuite setup/teardown lifecycle;
- execute with
  `--add-opens=java.desktop/java.awt=ALL-UNNAMED`;
- execute with
  `--add-opens=java.base/java.net=ALL-UNNAMED`.

Generated test bodies and assertions are unchanged.

Compatibility-scaffold SHA-256:

`08810525F68C1B3C4E0B4286E989262F35AFE59E9909B82CE4FBFADFDF316C2A`

Compatibility-launcher SHA-256:

`2932EC3B6637EC729BD7375AFC28385D64184F5A2B9BFAAD9574401C117CD9E1`

## Validity and exclusions

Generated methods: **9**

Excluded methods: **0**

Frozen T_ES target: **9**

No test was removed based on JaCoCo coverage, PIT mutation results, or
comparison with another suite.

`evidence/excluded-tests.csv` therefore contains only its header.

## Repeatability

Before freeze, the raw suite passed 5/5 executions in its native generation
environment.

The final Java 25 compatibility configuration was then executed five
consecutive times against the actual Storm 3.0.0 target:

- runs: 5
- passed runs: 5
- tests per run: 9
- failures per run: 0
- timeouts: 0

Each run emitted three non-blocking EvoSuite `ClassResetter`
`ClassNotFoundException` warnings for optional/reset instrumentation paths.
They do not represent missing runtime dependencies because each complete run
finished with:

`COMPAT_RESULT tests=9 failures=0 ignored=0 successful=true`

The final repeatability evidence is stored in:

`evidence/repeatability.csv`

## Maven integration

The EvoSuite source tree is excluded from normal Maven test compilation and
the generated `RedisFilterBolt_ESTest` is excluded from ordinary Surefire
discovery.

This is intentional: the generated suite must be compiled and executed through
its dedicated Java 25 compatibility runtime instead of the original
`@RunWith(EvoRunner.class)` path.

The normal project test lifecycle therefore remains isolated from the EvoSuite
compatibility execution.

## Adequacy status at freeze

At this freeze point:

- JaCoCo measurement: **not yet performed**
- PIT mutation testing: **not yet performed**
- adequacy-driven edits: **none**

Coverage and mutation measurements are strictly ex-post with respect to this
frozen suite.

## Frozen files

Raw generator output:

- `raw/RedisFilterBolt_ESTest.java`
- `raw/RedisFilterBolt_ESTest_scaffolding.java`

Executable frozen suite:

- `src/test/java/it/uniroma2/isw2/storm/testing/redisfilterbolt/es/generated/RedisFilterBolt_ESTest.java`
- `src/test/java/it/uniroma2/isw2/storm/testing/redisfilterbolt/es/generated/RedisFilterBolt_ESTest_scaffolding.java`
- `src/test/java/it/uniroma2/isw2/storm/testing/redisfilterbolt/es/runtime/RedisFilterBoltJava25Launcher.java`

Evidence:

- `evidence/excluded-tests.csv`
- `evidence/repeatability.csv`
- `evidence/fingerprints.sha256`
## Ex-post JaCoCo result

After the suite freeze, JaCoCo 0.8.15 was executed against the actual
Storm 3.0.0 / Java 25 target.

All 9 frozen tests passed.

Official adequacy results:

- Line Coverage: `16/45 = 35.5556%`
- Branch Coverage: `4/21 = 19.0476%`

Secondary diagnostics:

- Method Coverage: `3/4 = 75.0000%`
- Instruction Coverage: `53/170 = 31.1765%`

The denominators exactly match the RedisFilterBolt BB, CF and RND
measurements.

No test-source change followed from this measurement.

Permanent measurement artifacts are stored under:

`isw2/testing/results/redis-filter-bolt/tes/coverage/`
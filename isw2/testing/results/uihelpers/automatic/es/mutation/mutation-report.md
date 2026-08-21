# UIHelpers - EvoSuite mutation testing

## Scope

Class under test:

`org.apache.storm.daemon.ui.UIHelpers`

Automatic coverage-guided generator:

`EvoSuite 1.2.0`

The raw EvoSuite generation produced 43 test methods.

Six methods were excluded before adequacy measurement for previously
documented validity/compatibility reasons:

- `test02`: failure already present in the native Storm 2.7.1 generation environment;
- `test09`: failure already present in the native Storm 2.7.1 generation environment;
- `test22`: failure already present in the native Storm 2.7.1 generation environment;
- `test08`: behavioral drift between Storm 2.7.1 and Storm 3.0.0;
- `test10`: dependency on EvoSuite VNET instrumentation;
- `test38`: dependency on EvoSuite VFS instrumentation.

The final target suite is therefore:

`T_ES_TARGET = 37`

The selection was frozen before JaCoCo and PIT measurement. No generated
test was added, removed, or modified according to coverage or mutation
outcomes.

## PIT protocol

Mutation testing tool:

`PIT 1.25.8`

Mutator set:

`DEFAULTS`

Threads:

`1`

Only production bytecode for:

`org.apache.storm.daemon.ui.UIHelpers`

was mutated.

The exact Storm 3.0.0 `UIHelpers.class` bytecode was used. Its SHA-256 is:

`08C2E2460DBFF35E909285DCC11923A81AD2EF09A04CCB441DB2AD289927F79E`

The PIT client classpath and mutable-code setup reproduce the same protocol
used for the previous UIHelpers mutation experiments.

The generated mutation population contains exactly 338 mutants and matches
the populations used for T_CF, T_MT, and T_RND with zero differences.

Maintaining the same mutation population is an experimental comparability
control adopted for this project; it is not a separate requirement stated by
the professor.

## Java 25 compatibility adapter

EvoSuite 1.2.0 cannot execute its normal EvoRunner path directly on the
Storm 3.0.0 Java 25 bytecode because of its older embedded ASM/runtime
assumptions.

A previously validated JUnit 4 compatibility adapter is therefore used by PIT.

PIT sees one external JUnit test:

`runFrozenTargetSuite`

That adapter invokes the frozen `CompatibleRunner`, which executes exactly
the 37 selected EvoSuite test methods.

Before mutation analysis this execution was independently validated as:

`37 tests, 0 failures, 0 ignored`

During the PIT smoke run the same adapter again reported:

`PIT_ADAPTER_RESULT tests=37 failures=0 ignored=0 successful=true`

This adapter does not change the generated tests or their assertions.

### Granularity limitation

Because PIT sees the compatibility adapter as one external JUnit test, PIT can
measure whether the complete EvoSuite suite kills a mutant but cannot reliably
attribute the kill to an individual generated `testXX` method.

The mutation metrics are therefore interpreted at suite level.

## Mutation results

| Outcome | Mutants |
|---|---:|
| Generated | 338 |
| Killed | 24 |
| Survived | 80 |
| No Coverage | 234 |
| Covered mutants | 104 |
| Timed Out | 0 |
| Run Error | 0 |
| Memory Error | 0 |

### Mutation Score

`24 / 338 = 7.10%`

### Test Strength

Mutants reached by T_ES are:

`24 + 80 = 104`

Therefore:

`24 / 104 = 23.08%`

### No-coverage proportion

`234 / 338 = 69.23%`

## Structural coverage

The primary structural-coverage measurement for T_ES remains the previously
recorded JaCoCo result:

- line coverage: `256 / 1122 = 22.8164%`;
- branch coverage: `51 / 242 = 21.0744%`.

PIT reported `281 / 1122` lines during its own dependency/coverage analysis.

That PIT value is not substituted for the JaCoCo metric because PIT and
JaCoCo use different instrumentation and measurement mechanisms. JaCoCo is
kept as the structural adequacy metric, while PIT supplies mutation adequacy.

## Cross-suite comparison

| Suite | Tests | Killed | Survived | No Coverage | Mutation Score | Test Strength |
|---|---:|---:|---:|---:|---:|---:|
| T_RND | 35 | 32 | 39 | 267 | 9.47% | 45.07% |
| T_ES | 37 | 24 | 80 | 234 | 7.10% | 23.08% |
| T_CF | 45 | 115 | 61 | 162 | 34.02% | 65.34% |
| T_MT | 53 | 155 | 25 | 158 | 45.86% | 86.11% |

A useful observation is the comparison between T_ES and T_RND.

T_RND reaches:

`32 + 39 = 71 mutants`

T_ES reaches:

`24 + 80 = 104 mutants`

Therefore T_ES reaches 33 more mutants than T_RND, but kills 8 fewer.

This indicates that, for UIHelpers under this experiment, additional
reachability does not automatically correspond to stronger fault detection.
The EvoSuite suite exercises more mutated behavior than the random suite, but
many of those executions do not contain or trigger sufficiently discriminating
oracles to distinguish the mutant from the original behavior.

This is also reflected by Test Strength:

- T_RND: `45.07%`;
- T_ES: `23.08%`.

## Reproducibility and feedback control

The full PIT run was performed only after the EvoSuite suite had been frozen.

Mutation feedback was therefore ex-post only: PIT results were not used to
modify, regenerate, select, or tune the 37 measured tests.

The verified run completed with:

- 338 generated mutants;
- zero population differences against T_CF;
- zero timed-out mutants;
- zero run-error mutants;
- zero memory-error mutants;
- unchanged frozen EvoSuite source fingerprints;
- clean Git working tree.

Measurement commit before recording these results:

`f91f13b8816a1149d8b63b907fce07781d70fba9`
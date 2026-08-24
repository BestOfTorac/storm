# RedisFilterBolt - Final Testing Report

## Status

FINAL - TESTING PHASE CLOSED

Generated: 2026-08-24T14:24:28+02:00

## Target

Production class:

org.apache.storm.redis.bolt.RedisFilterBolt

Project baseline:

Apache Storm 3.0.0

## Experimental protocol

The RedisFilterBolt experiment evaluates five testing approaches:

1. T_BB - manual black-box testing;
2. T_CF - manual control-flow-guided testing;
3. T_RND - random automatic test generation;
4. T_ES - EvoSuite automatic test generation;
5. T_LLM - LLM-based test generation.

Storm native tests were not used as experimental test inputs.

The experimental suites were designed or generated from production behavior and code under the project protocol.

Each automatic suite was evaluated only after its candidate had been stabilized or frozen.

Coverage and mutation feedback were not used retrospectively to alter the frozen baseline suites.

## Common measurement infrastructure

Structural coverage:

- JaCoCo 0.8.15;
- target denominator: 45 executable lines;
- target denominator: 21 branches.

Mutation testing:

- PIT 1.25.8;
- mutators: DEFAULTS;
- target: RedisFilterBolt;
- reference population: 12 mutants.

For T_BB+T_CF, T_RND, T_ES and T_LLM, mutant identity was verified using:

- mutated class;
- mutated method;
- source line;
- mutator;
- mutation description;
- multiplicity.

Final population identity result: PASS.

## T_BB - Manual black-box testing

Method:

Category Partition based on externally observable responsibilities and documented behavior.

The suite was intentionally kept small and explainable, with explicit and defensible oracles.

Tests: 11

Coverage:

- lines: 40 / 45 = 88.8889%
- branches: 16 / 21 = 76.1905%

Mutation testing:

No independent PIT measurement was performed on T_BB alone.

Mutation results from the cumulative T_BB+T_CF suite must not be attributed retroactively to T_BB.

## T_CF - Control-flow-guided additions

T_CF was developed after structural inspection of the target.

Four additional tests were introduced on top of the 11-test black-box suite.

Cumulative cardinality: 15

Coverage:

- lines: 45 / 45 = 100.0000%
- branches: 21 / 21 = 100.0000%

Mutation testing:

- mutants: 12
- killed: 11
- survived: 1
- no coverage: 0
- raw mutation score: 91.6667%
- raw test strength: 91.6667%

One remaining survivor was analyzed separately and classified as equivalent.

The raw mutation score remains the canonical cross-suite comparison value.

## T_RND - Random automatic generation

Generator:

Randoop.

The final suite was calibrated to 11 tests to permit a direct cardinality-controlled comparison with T_BB and T_LLM.

Tests: 11

Coverage:

- lines: 22 / 45 = 48.8889%
- branches: 1 / 21 = 4.7619%

Mutation testing:

- mutants: 12
- killed: 0
- survived: 4
- no coverage: 8
- mutation score: 0.0000%
- test strength: 0.0000%

Interpretation:

The random suite executes successfully but explores the target weakly, especially at branch and mutation level.

## T_ES - EvoSuite

The EvoSuite-generated candidate was stabilized and frozen before adequacy measurement.

Tests: 9

Coverage:

- lines: 16 / 45 = 35.5556%
- branches: 4 / 21 = 19.0476%

Mutation testing:

- mutants: 12
- killed: 2
- survived: 1
- no coverage: 9
- mutation score: 16.6667%
- test strength: 66.6667%

Interpretation:

The principal limitation is mutant reachability. The mutants reached by the suite are tested more strongly than the raw mutation score alone suggests.

## T_LLM - LLM-generated testing

Provider:

Microsoft 365 Copilot via Web.

Generation protocol:

The LLM received only controlled production context and the testing POM.

No BB, CF, RND, ES, JaCoCo or mutation feedback was supplied before baseline freeze.

### Generation attempts

Attempt 01 was rejected because the assistant independently used external Storm documentation, violating the controlled-context protocol.

Attempt 02 used the controlled context and produced the accepted suite.

Accepted tests: 11

No LLM repair was required.

The accepted source passed five consecutive executions with:

- 11 tests;
- 0 failures;
- 0 errors;
- 0 skipped.

Frozen SHA-256:

B247D548F81DAA437A281A22F865C56810C4B0575368267F520FC26D64D17358

Coverage:

- lines: 38 / 45 = 84.4444%
- branches: 17 / 21 = 80.9524%

Mutation testing:

- mutants: 12
- killed: 9
- survived: 3
- no coverage: 0
- mutation score: 75.0000%
- test strength: 75.0000%

All 12 reference mutants were reached.

The three survivors are therefore oracle/discrimination gaps rather than reachability gaps.

No mutation-guided refinement was applied.

### T_LLM measurement infrastructure notes

Several execution-harness problems occurred during measurement and were preserved as evidence.

JaCoCo:

- an initial PowerShell harness treated native stderr output as a terminating error;
- Mockito agent warnings were not test failures;
- JaCoCo itself executed successfully;
- the CSV parser was corrected to use the actual dotted package representation.

PIT:

- the initial Maven PIT plugin run skipped the module because the experimental module contains test code but no module-local production source;
- a recovery attempt was rejected because it accidentally selected an existing T_ES report;
- the final solution used direct PIT CLI execution;
- RedisFilterBolt bytecode from storm-redis 3.0.0 was supplied through explicit mutableCodePaths;
- the project test classpath was supplied explicitly;
- pitest-junit5-plugin was placed on the PIT tool classpath;
- JUnit Platform Launcher 6.1.1 was supplied explicitly;
- Mockito 5.23.0 was supplied as a Java agent to the PIT minion.

The final PIT run produced the expected 12-mutant population and passed exact population identity validation.

No production or test source was changed to obtain the successful measurement.

## Final cross-suite results

| Suite | Tests | Line coverage | Branch coverage | Mutation score | Test strength |
|---|---:|---:|---:|---:|---:|
| T_BB | 11 | 88.8889% | 76.1905% | N/A | N/A |
| T_BB+T_CF | 15 | 100.0000% | 100.0000% | 91.6667% | 91.6667% |
| T_RND | 11 | 48.8889% | 4.7619% | 0.0000% | 0.0000% |
| T_ES | 9 | 35.5556% | 19.0476% | 16.6667% | 66.6667% |
| T_LLM | 11 | 84.4444% | 80.9524% | 75.0000% | 75.0000% |

## Controlled N=11 comparison

T_BB, T_RND and T_LLM each contain exactly 11 tests.

| Suite | Line coverage | Branch coverage | Mutation score |
|---|---:|---:|---:|
| T_BB | 88.8889% | 76.1905% | N/A |
| T_RND | 48.8889% | 4.7619% | 0.0000% |
| T_LLM | 84.4444% | 80.9524% | 75.0000% |

T_BB covers more lines than T_LLM.

T_LLM covers more branches than T_BB.

T_LLM substantially outperforms T_RND in both structural and mutation adequacy.

No mutation comparison between T_BB and T_LLM is claimed because T_BB was not measured independently with PIT.

## Conclusions

1. The cumulative manual T_BB+T_CF suite provides the strongest overall adequacy on RedisFilterBolt.

2. Manual black-box testing alone already achieves high structural coverage with a small, explainable suite.

3. Control-flow-guided additions close the remaining structural gaps and substantially improve mutation adequacy.

4. Random generation produces executable tests but performs poorly on this target.

5. EvoSuite shows better oracle effectiveness on the mutants it reaches, but its principal limitation is reachability.

6. The LLM baseline performs strongly without receiving adequacy feedback before freeze.

7. T_LLM reaches all 12 reference mutants and kills nine, leaving three oracle-level survivors.

8. Structural coverage alone is insufficient to characterize test quality; mutation testing reveals complementary information.

9. These observations describe this controlled experiment on RedisFilterBolt and are not universal claims about the testing techniques.

## Canonical evidence map

Black-box evidence:

isw2/testing/results/redis-filter-bolt/tbb/

Control-flow evidence:

isw2/testing/results/redis-filter-bolt/tcf/

Manual mutation baseline:

isw2/testing/results/redis-filter-bolt/mutation/baseline/

Random generation:

isw2/testing/results/redis-filter-bolt/trnd/

EvoSuite:

isw2/testing/results/redis-filter-bolt/tes/

LLM:

isw2/testing/results/redis-filter-bolt/tllm/

Cross-suite comparison:

isw2/testing/results/redis-filter-bolt/comparison/

## T_LLM and final comparison checkpoints

T_LLM freeze:

1be792bbef02651d1ff033f3f1e2aa8e949cd021

T_LLM JaCoCo:

8e9470b63167c8ea67b62f60608355d957566acf

T_LLM PIT:

8e63c5d9adb5d31c81f00b70733b23f64f835e44

Cross-suite comparison:

d9b013e0afc5bf34576001d30dc3a17d9b360061

## Closure

RedisFilterBolt experimental testing is complete.

No additional BB, CF, RND, ES or LLM tests are required for the frozen experiment.

The target is ready to be treated as closed in the final De Angelis project documentation.

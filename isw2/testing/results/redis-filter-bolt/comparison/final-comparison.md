# RedisFilterBolt - Final test-suite comparison

## Status

FINAL CROSS-SUITE COMPARISON

## Target

org.apache.storm.redis.bolt.RedisFilterBolt

## Structural coverage

All JaCoCo measurements use the same target denominators:

- 45 executable lines;
- 21 branches.

| Suite | Tests | Line coverage | Branch coverage |
|---|---:|---:|---:|
| T_BB | 11 | 40 / 45 = 88.8889% | 16 / 21 = 76.1905% |
| T_BB+T_CF | 15 | 45 / 45 = 100.0000% | 21 / 21 = 100.0000% |
| T_RND | 11 | 22 / 45 = 48.8889% | 1 / 21 = 4.7619% |
| T_ES | 9 | 16 / 45 = 35.5556% | 4 / 21 = 19.0476% |
| T_LLM | 11 | 38 / 45 = 84.4444% | 17 / 21 = 80.9524% |

## Mutation adequacy

Comparable PIT measurements use:

- PIT 1.25.8;
- DEFAULTS mutators;
- target RedisFilterBolt;
- the same 12-mutant population.

Exact population identity across T_BB+T_CF, T_RND, T_ES and T_LLM: PASS.

| Suite | Tests | Killed | Survived | No Coverage | Mutation Score | Test Strength |
|---|---:|---:|---:|---:|---:|---:|
| T_BB | 11 | N/A | N/A | N/A | N/A | N/A |
| T_BB+T_CF | 15 | 11 | 1 | 0 | 91.6667% | 91.6667% |
| T_RND | 11 | 0 | 4 | 8 | 0.0000% | 0.0000% |
| T_ES | 9 | 2 | 1 | 9 | 16.6667% | 66.6667% |
| T_LLM | 11 | 9 | 3 | 0 | 75.0000% | 75.0000% |

T_BB has no independent PIT measurement. Mutation results must therefore not be retroactively attributed to the original black-box suite.

## Same-cardinality comparison

T_BB, T_RND and T_LLM each contain 11 tests.

| Suite | Line Coverage | Branch Coverage | Mutation Score |
|---|---:|---:|---:|
| T_BB | 88.8889% | 76.1905% | N/A |
| T_RND | 48.8889% | 4.7619% | 0.0000% |
| T_LLM | 84.4444% | 80.9524% | 75.0000% |

T_BB covers more executable lines than T_LLM: 88.8889% versus 84.4444%.

T_LLM covers more branches than T_BB: 80.9524% versus 76.1905%.

T_LLM substantially outperforms T_RND at equal cardinality.

No independent mutation comparison between T_BB and T_LLM is claimed because PIT was not executed on T_BB alone.

## Interpretation

### T_BB

The independently designed black-box suite achieves high structural adequacy without coverage-guided design.

- line coverage: 88.8889%;
- branch coverage: 76.1905%.

### T_BB+T_CF

The four control-flow-guided additions increase the cumulative suite to 15 tests and close all structural gaps.

- line coverage: 100.0000%;
- branch coverage: 100.0000%;
- raw mutation score: 91.6667%.

### T_RND

The Randoop suite is executable and cardinality-controlled but explores RedisFilterBolt weakly.

- line coverage: 48.8889%;
- branch coverage: 4.7619%;
- mutation score: 0.0000%.

### T_ES

The frozen EvoSuite suite contains 9 tests.

- line coverage: 35.5556%;
- branch coverage: 19.0476%;
- mutation score: 16.6667%;
- test strength: 66.6667%.

Its principal limitation on this target is mutation reachability.

### T_LLM

The independently generated LLM baseline contains 11 tests and was frozen before adequacy feedback.

- line coverage: 84.4444%;
- branch coverage: 80.9524%;
- mutation score: 75.0000%;
- test strength: 75.0000%.

All 12 reference mutants are reached. Nine are killed and three survive.

The remaining mutation gaps are therefore oracle/discrimination gaps rather than reachability gaps.

No mutation-guided refinement is applied to T_LLM_BASELINE.

## Main conclusions

1. T_BB+T_CF provides the strongest overall adequacy.
2. T_LLM performs strongly despite receiving no coverage or mutation feedback before freeze.
3. At equal cardinality, T_LLM substantially outperforms T_RND.
4. T_BB and T_LLM show complementary structural profiles.
5. EvoSuite is mainly limited by reachability on this target.
6. Structural coverage and mutation testing reveal different properties and must be interpreted together.
7. These results describe this controlled experiment on RedisFilterBolt and are not claimed as universal conclusions about the techniques.

## Canonical evidence

Full comparison:

isw2/testing/results/redis-filter-bolt/comparison/final-suite-comparison.csv

Same-cardinality comparison:

isw2/testing/results/redis-filter-bolt/comparison/same-cardinality-comparison.csv

# M4 - RedisFilterBolt final C0-C4 analysis

Target: `org.apache.storm.redis.bolt.RedisFilterBolt`

The analysis compares the original class C0 with the four variants generated according to the Milestone 4 experimental matrix.

## Generation matrix

| Variant | Starting source | Tests/context supplied to Copilot |
| --- | --- | --- |
| C0 | Original | N/A |
| C1 | C0 original | None |
| C2 | C0 original | T_BB: 11 tests |
| C3 | C0 original | T_BB: 11 + T_CF: 4 |
| C4 | C0 original | T_BB: 11 + T_CF: 4 + T_MT context |

For RedisFilterBolt, mutation testing did not produce an additional derived executable T_MT suite. Therefore C4 received the T_MT experimental status as context (`NO_DERIVED_SUITE`), but no mutation-derived Java test was invented.

T_RND, T_ES and T_LLM were never supplied during generation.

## Source identity and effective refactoring

- C1 is byte-for-byte identical to C0 and is therefore classified as a no-op, despite the refactoring claimed in the Copilot response.
- C2 contains a real source-level refactoring.
- C3 contains a real source-level refactoring.
- C4 contains a real source-level refactoring.
- Claims that the GEO list was changed from a raw List to `List<GeoCoordinate>` are not counted as refactoring because C0 already used `List<GeoCoordinate>`.

## Compilation and code smells

| Variant | Compiles | NSMELLS | Delta vs C0 | Old smells | New smells |
| --- | --- | ---: | ---: | --- | --- |
| C0 | Baseline | 0 | 0 | None | None |
| C1 | PASS | 0 | 0 | None | None |
| C2 | PASS | 0 | 0 | None | None |
| C3 | PASS | 0 | 0 | None | None |
| C4 | PASS | 0 | 0 | None | None |

C0 contains no SonarCloud code smells. Consequently none of the variants can remove an existing smell, and none of C1-C4 introduced a new smell.

## LOC

LOC was measured consistently for C0-C4 with the project `JavaLocCounter.count(String)` implementation.

| Variant | LOC | Delta vs C0 |
| --- | ---: | ---: |
| C0 | 82 | 0 |
| C1 | 82 | 0 |
| C2 | 85 | +3 |
| C3 | 91 | +9 |
| C4 | 94 | +12 |

## Bugginess-correlation analysis

The correlation signs are taken from the same M1 dataset used for the project analysis.

Among the features that can be meaningfully re-measured on synthetic source variants, `LOC` and `NSMELLS` are positively correlated with bugginess.

The measured correlations are positive for LOC (approximately +0.279) and NSMELLS (approximately +0.168).

The negatively correlated features identified in the dataset are historical/process features such as age, entropy, change-set and developer-distribution measures. A source-only synthetic Cx variant does not create a new project history, so those features are not synthetically redefined and remain unchanged relative to C0.

### Professor page-5 questions

| Variant | 1. Compiles? | 2. Smells? | 3. Positive-correlated feature higher than C0? | 4. Negative-correlated feature higher than C0? |
| --- | --- | --- | --- | --- |
| C1 | YES | None; no old/new smells | NO | NO |
| C2 | YES | None; no old/new smells | YES - LOC 85 > 82 | NO |
| C3 | YES | None; no old/new smells | YES - LOC 91 > 82 | NO |
| C4 | YES | None; no old/new smells | YES - LOC 94 > 82 | NO |

C1 does not increase either LOC or NSMELLS because its source is exactly equal to C0.

C2-C4 increase LOC while NSMELLS remains zero.

## Post-hoc experimental validation

| Variant | T_BB | T_CF | T_MT | T_RND | T_ES | T_LLM | Executable total |
| --- | --- | --- | --- | --- | --- | --- | --- |
| C1 | 11/11 PASS | 4/4 PASS | N/A | 11/11 PASS | 9/9 PASS | 11/11 PASS | 46/46 PASS |
| C2 | 11/11 PASS | 4/4 PASS | N/A | 11/11 PASS | 9/9 PASS | 11/11 PASS | 46/46 PASS |
| C3 | 11/11 PASS | 4/4 PASS | N/A | 11/11 PASS | 9/9 PASS | 11/11 PASS | 46/46 PASS |
| C4 | 11/11 PASS | 4/4 PASS | N/A | 11/11 PASS | 9/9 PASS | 11/11 PASS | 46/46 PASS |

`T_MT` is N/A because no mutation-derived executable test suite exists for RedisFilterBolt.

Native Apache Storm tests were not used as experimental post-hoc suites.

## Overall interpretation

C1 demonstrates that supplying no tests does not guarantee that the LLM will perform an effective refactoring: the returned source is identical to C0.

C2, C3 and C4 all perform real structural refactoring while preserving all executable experimental tests and introducing no SonarCloud code smells.

However, all three increase LOC, which is a positively bugginess-correlated feature in the project dataset.

Therefore the refactorings preserve the tested observable behaviour and do not worsen NSMELLS, but they do not improve every bugginess-correlated feature: LOC increases progressively from C2 to C4.
# M4 - UIHelpers C4

## Identity

- Target: org.apache.storm.daemon.ui.UIHelpers
- Variant: C4
- C4 SHA-256: 2EB794AB204C9555703E8D2D73AF787AC5001737B69C8268BDF64A60BA9D8B79
- C0 Nsmells: 77

## Generation input

C4 was generated from C0 with Microsoft 365 Copilot using:
- C0 source;
- C0 SonarCloud diagnostic;
- T_BB: 35 tests;
- T_CF: 10 tests;
- T_MT: 8 tests.

T_RND, T_ES and T_LLM were not supplied during generation.

## Compilation and SonarCloud

- Compile: PASS
- C4 Nsmells: 15
- Delta vs C0: -62
- NCLOC: 1685
- Complexity: 236
- Sonar analysis ID: 5c14e2d6-ff89-406b-a248-18e9636de2f4

## Post-hoc compatibility

| Suite | Supplied to Copilot | Result |
| --- | --- | --- |
| T_BB | Yes | 35/35 PASS |
| T_CF | Yes | 10/10 PASS |
| T_MT | Yes | 7/8 FAIL |
| T_RND | No | 35/35 PASS |
| T_ES | No | 37/37 PASS |
| T_LLM | No | 35/35 PASS |

Overall: 159/160 post-hoc tests passed.

Tests supplied to Copilot: 52/53 passed.
Tests not supplied to Copilot: 107/107 passed.

## Observed regression

The only failing test is mt06CorsConfigurationParametersAreObservable.
The expected CORS filter parameter value is '*' but the lookup returns null.
This is recorded as an observable CORS configuration regression.
C4 was not modified after observing the failure.

## Evidence

- UIHelpers-C4.java
- c4-summary.csv
- c4-test-results.csv
- c4-smell-rules.csv
- raw/sonar-c4-result.txt
- raw/tbb-c4.log
- raw/tcf-c4.log
- raw/tmt-c4.log
- raw/trnd-c4.log
- raw/tes-c4.log
- raw/tllm-p2-c4.log

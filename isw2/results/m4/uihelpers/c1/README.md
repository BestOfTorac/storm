# M4 - UIHelpers C1

## Identity

- Target: org.apache.storm.daemon.ui.UIHelpers
- Variant: C1
- C1 source SHA-256: 19EBC88BD9ECF655F575793D11CD67067E0634BFA4C2F7395FE849E8A3649B80
- Tests supplied to Copilot during generation: none

## Compilation

- C1 compilation/package with Storm 3.0.0: PASS

## SonarCloud

| Metric | C0 | C1 | Delta |
| --- | ---: | ---: | ---: |
| Nsmells | 77 | 79 | +2 |

The only rule whose count increased is java:S5411, from 2 to 4.
No Sonar rule count decreased in C1.

## Post-hoc test compatibility

| Suite | Passed | Executed | Result |
| --- | ---: | ---: | --- |
| T_BB | 35 | 35 | PASS |
| T_CF | 10 | 10 | PASS |
| T_MT | 7 | 8 | FAIL |
| T_RND | 35 | 35 | PASS |
| T_ES | 37 | 37 | PASS |
| T_LLM | 35 | 35 | PASS |

## T_MT failure

- Test: UIHelpersMutationTest.mt06CorsConfigurationParametersAreObservable
- Failure type: AssertionFailedError
- Observation: expected "*" but obtained null.
- Interpretation: C1 changes an observable aspect of the CORS filter parameter configuration.

No manual correction was applied to C1 after observing the failure.

## Interpretation

C1 compiles successfully.
It passes T_BB, T_CF, T_RND, T_ES and T_LLM completely.
It fails one of the eight mutation-guided tests.
Its Nsmells value increases from 77 to 79.
Therefore C1 does not improve the SonarCloud smell criterion and does not preserve every behavior observed by the complete set of post-hoc test suites.

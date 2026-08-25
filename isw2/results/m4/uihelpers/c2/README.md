# UIHelpers C2 - M4 evidence

## Variant

Class: org.apache.storm.daemon.ui.UIHelpers

Variant: C2

Frozen source SHA256: CC70E7D66B23D0666A3CBC1BBD94D97C58689E695E84E9DDA387AB174E86F91B

C2 was generated from the original C0. The Copilot input contained the C0 source, the C0 SonarCloud diagnostic with 77 smells, and the 35 black-box tests. No CF, MT, RND, ES, or LLM tests were supplied during C2 generation.

## Compilation

C2 compiles with the Apache Storm 3.0.0 system: PASS.

## SonarCloud

C0 Nsmells: 77

C1 Nsmells: 79

C2 Nsmells: 12

Delta C2 vs C0: -65

Remaining C2 smells:

- S1172: 7
- S107: 3
- S5411: 2

The official SonarCloud code_smells metric and the open CODE_SMELL issue count both report 12.

## Post-hoc test compatibility

| Suite | Result |
| --- | --- |
| T_BB | 35/35 PASS |
| T_CF | 10/10 PASS |
| T_MT | 7/8 FAIL |
| T_RND | 35/35 PASS |
| T_ES | FAIL before execution |
| T_LLM | 35/35 PASS |

T_MT fails only mt06CorsConfigurationParametersAreObservable: the queried CORS configuration parameter returns null instead of the expected value *.

T_ES cannot be executed because the frozen suite does not compile against C2. EvoSuite test28 invokes new UIHelpers(), while C2 changes the UIHelpers constructor accessibility to private. After restoring all required EvoSuite and Clojure dependencies, this was the only javac error, so it is classified as a C2 compatibility failure rather than an infrastructure failure.

No modification was made to C2 in response to these post-hoc results.

## Files

- UIHelpers-C2.java: frozen C2 source
- c2-summary.csv: variant-level result
- c2-smell-rules.csv: final SonarCloud smell distribution
- c2-test-results.csv: suite-by-suite compatibility results
- raw/sonar-c2-result.txt: final verified SonarCloud result
- raw/tbb-c2.log: black-box execution
- raw/tcf-c2.log: control-flow execution
- raw/tmt-c2.log: mutation-guided execution
- raw/trnd-c2.log: Randoop execution
- raw/tes-c2-javac.log: definitive EvoSuite compilation failure
- raw/tllm-p2-c2.log: frozen LLM P2 execution

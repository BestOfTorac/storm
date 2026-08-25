# UIHelpers C3 - M4 evidence

## Variant

Class: org.apache.storm.daemon.ui.UIHelpers

Variant: C3

Frozen source SHA256: 6EC2905E393E8C7D3058C5CB938DB926AB514D2D8CDC2C1C40D57103CD08A8CB

C3 was generated independently from the original C0.

The Copilot input contained:

- the original C0 UIHelpers source;
- the C0 SonarCloud diagnostic with 77 code smells;
- the 35 black-box tests T_BB;
- the 10 control-flow tests T_CF.

T_MT, T_RND, T_ES, and T_LLM were not supplied during generation.

## Compilation

C3 compiles with Apache Storm 3.0.0: PASS.

## SonarCloud

Analysis ID: a6a49165-de04-4fe4-9ced-323de4c2da61

C0 Nsmells: 77

C3 Nsmells: 15

Delta C3 vs C0: -62

Outcome: IMPROVED

C3 ncloc: 1689

C3 complexity: 236

Remaining C3 smells:

- S1172: 7
- S107: 3
- S5411: 2
- S1118: 1
- S1192: 1
- S2864: 1

The official SonarCloud code_smells metric, the CODE_SMELL issue count, and the rule-count sum all report 15.

## Post-hoc test compatibility

| Suite | Supplied during C3 generation | Result |
| --- | --- | --- |
| T_BB | Yes | 35/35 PASS |
| T_CF | Yes | 10/10 PASS |
| T_MT | No | 8/8 PASS |
| T_RND | No | 35/35 PASS |
| T_ES | No | 37/37 PASS |
| T_LLM | No | 35/35 PASS |

All 45 tests supplied during C3 generation pass.

All four additional post-hoc suites also pass completely.

The frozen EvoSuite suite compiled successfully against C3 before all 37 selected tests were executed successfully.

No modification was made to C3 in response to the post-hoc results.

## Files

- UIHelpers-C3.java: frozen C3 source
- c3-summary.csv: variant-level result
- c3-smell-rules.csv: final SonarCloud smell distribution
- c3-test-results.csv: suite-by-suite compatibility results
- raw/sonar-c3-result.txt: verified SonarCloud result
- raw/tbb-c3.log: black-box execution
- raw/tcf-c3.log: control-flow execution
- raw/tmt-c3.log: mutation-guided execution
- raw/trnd-c3.log: Randoop execution
- raw/tes-c3.log: frozen EvoSuite execution
- raw/tllm-p2-c3.log: frozen LLM P2 execution

# UIHelpers T_LLM - P2 repeatability

Recorded:

`2026-08-23T18:21:48+02:00`

Frozen repository commit:

`9de067548af80dc4018da76ffb390c85beef70d3`

## Suite identity

Suite:

`T_LLM P2`

P1 raw SHA-256:

`8099DE656838FD97BB09175F4377D079B7BB06112917380DDB8B64D264278881`

P2 frozen SHA-256:

`4767540BADE73F4F391872AE8218F2005001166495B8FEA1A4E315431453CE94`

Executable SHA-256:

`4767540BADE73F4F391872AE8218F2005001166495B8FEA1A4E315431453CE94`

P2 and executable source were byte-identical throughout the repeatability
gate.

## Repeatability protocol

The frozen P2 suite was executed five consecutive times using:

- Java: Microsoft OpenJDK 25.0.4
- Maven: 3.9.16
- Maven Surefire: 3.5.6
- test class: `org.apache.storm.daemon.ui.UIHelpersTest`
- expected tests per run: 35

No source modification was performed between runs.

## Results

| Run | Tests | Failures | Errors | Skipped | Time (s) | Result |
|---:|---:|---:|---:|---:|---:|:---|
| 1 | 35 | 0 | 0 | 0 | 0.482 | PASS |
| 2 | 35 | 0 | 0 | 0 | 0.457 | PASS |
| 3 | 35 | 0 | 0 | 0 | 0.447 | PASS |
| 4 | 35 | 0 | 0 | 0 | 0.433 | PASS |
| 5 | 35 | 0 | 0 | 0 | 0.459 | PASS |

Successful runs:

`5 / 5`

Total individual test-method executions:

`175`

Failures:

`0`

Errors:

`0`

Skipped:

`0`

## Interpretation

The refined Microsoft 365 Copilot suite showed no observed flaky behavior
across the five-run repeatability gate.

This gate establishes execution stability only. It does not imply structural
or fault-detection adequacy.

At this point:

- no manual repair of P2 has been performed;
- no JaCoCo result has been used to modify P2;
- no PIT result has been used to modify P2;
- P2 remains frozen before adequacy measurement.

The next experimental stage is independent structural adequacy measurement
using Line Coverage and Branch Coverage.

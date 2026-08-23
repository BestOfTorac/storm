# UIHelpers T_LLM P2 - Structural adequacy

Recorded:

`2026-08-23T18:27:18+02:00`

Frozen repository commit:

`108ab42b074f482f5bff145d4f896df005e3c040`

## Suite identity

Suite:

`T_LLM P2`

Tests:

`35`

Frozen P2 SHA-256:

`4767540BADE73F4F391872AE8218F2005001166495B8FEA1A4E315431453CE94`

Executable SHA-256:

`4767540BADE73F4F391872AE8218F2005001166495B8FEA1A4E315431453CE94`

The suite was not modified before or during adequacy measurement.

## Production target

Class:

`org.apache.storm.daemon.ui.UIHelpers`

Measured production bytecode SHA-256:

`08C2E2460DBFF35E909285DCC11923A81AD2EF09A04CCB441DB2AD289927F79E`

This bytecode identity matches the production class used for the previous
UIHelpers coverage experiments.

## Tool

JaCoCo:

`0.8.15`

The instrumented regression execution completed with:

- tests: 35
- failures: 0
- errors: 0
- skipped: 0

## Structural adequacy results

| Metric | Covered | Total | Percentage |
|---|---:|---:|---:|
| Line | 80 | 1122 | 7.1301% |
| Branch | 37 | 242 | 15.2893% |
| Method | 17 | 122 | 13.9344% |
| Instruction | 471 | 5802 | 8.1179% |

The two preregistered structural adequacy metrics for the experiment are
Line Coverage and Branch Coverage.

Method and Instruction coverage are retained as supplementary diagnostic
information.

## Interpretation

The refined LLM suite is execution-stable and contains clear deterministic
tests, but reaches only a small portion of the large `UIHelpers` class.

The result is consistent with qualitative inspection of the generated
suite: many tests exercise a limited set of helper families, especially
duration formatting, JSON/JSONP processing, and logviewer configuration.

The Branch Coverage percentage is higher than the Line Coverage percentage
because several selected helper methods contain multiple conditional paths.

Coverage measurement was performed only after the P2 suite had been frozen
and passed the five-run repeatability gate.

No coverage result was supplied to Microsoft 365 Copilot before the suite
freeze.

No adequacy-guided modification of P2 has been performed.

## Raw measurement fingerprints

JaCoCo CSV SHA-256:

`DDF11F297C5EB44A28FCB1A3A82AF1BA1A1BFC87488954E701C4FB4F7C3A4C63`

JaCoCo XML SHA-256:

`B6991FF5AF7A44B97C109A9239DE78C422285CA2EDAF4BF76830D5F25BAA9DD8`

JaCoCo execution-data SHA-256:

`EB05553E07DB47F4F858E0FA0A34C32338E2791CE556842E2B29DD6F636E2EFC`

At the time this evidence was recorded, PIT mutation testing had not yet
been executed on T_LLM P2.

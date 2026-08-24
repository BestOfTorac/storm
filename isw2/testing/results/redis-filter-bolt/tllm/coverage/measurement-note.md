# RedisFilterBolt T_LLM - JaCoCo measurement note

## Timestamp

2026-08-24T13:46:19+02:00

## Baseline

Freeze commit:

1be792bbef02651d1ff033f3f1e2aa8e949cd021

Test SHA-256:

B247D548F81DAA437A281A22F865C56810C4B0575368267F520FC26D64D17358

## Execution

Tests run: 11
Failures: 0
Errors: 0
Skipped: 0
Build: SUCCESS

## Harness correction

The first JaCoCo orchestration command contained two PowerShell-side issues.

1. The Maven dependency:get coordinate used a variable immediately followed by a colon. PowerShell therefore did not interpolate the JaCoCo version correctly. The required JaCoCo 0.8.15 artifacts were already present in the local Maven repository and were subsequently used successfully.

2. The initial CSV parser searched the JaCoCo PACKAGE field using a slash-separated package name. The generated CSV represents the package as:

org.apache.storm.redis.bolt

The JaCoCo execution and report generation themselves completed successfully. No test source or production source was modified.

## Corrected results

Line coverage:

38 / 45 = 84.4444 %

Branch coverage:

17 / 21 = 80.9524 %

Method coverage:

3 / 4 = 75 %

Instruction coverage:

141 / 170 = 82.9412 %

## Comparability

Line denominator: 45
Branch denominator: 21

Expected reference denominators:

45 lines
21 branches

Comparability gate: PASS

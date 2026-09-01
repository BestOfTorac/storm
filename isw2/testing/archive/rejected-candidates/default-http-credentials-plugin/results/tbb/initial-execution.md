# DefaultHttpCredentialsPlugin - T_BB initial execution

Recorded:

`2026-08-23T19:32:16+02:00`

Repository baseline before T_BB freeze:

`7d51d2f0683e15372690f549bb8303b4137ffb5e`

## Target

Class:

`org.apache.storm.security.auth.DefaultHttpCredentialsPlugin`

Production source SHA-256:

`2AC890C2F52A856E07E2F83C245DA8A732D69ACED9B5FE938FCB1E1C77D5AE38`

## Category Partition

Category Partition SHA-256:

`1D1C550EB7A297D534343DE1DF84C380371724C00DA9B407EB9AE1442E880486`

Selected-frame inventory SHA-256:

`0A533C61C3651C06C32DE7DAE336B40E190832A6CB777F002449DBCC5555BBF1`

Exactly ten black-box frames were selected before any structural coverage or
mutation measurement for this class.

No project-native Apache Storm tests were inspected or reused.

## T_BB identity

Test class:

`it.uniroma2.isw2.storm.testing.defaulthttpcredentialsplugin.bb.DefaultHttpCredentialsPluginBBTest`

Tests:

`10`

T_BB SHA-256:

`19CCFFEE51A8E6F00078B1A5EB0B0F5174E5B0DD8C4BFDCBC5C1C3F4E09117AF`

All ten frozen Category Partition frames have a corresponding test.

## Test technology

- Java: Microsoft OpenJDK 25.0.4
- Maven: 3.9.16
- JUnit Jupiter: 6.1.1
- Mockito Core: 5.23.0
- Mockito usage: HttpServletRequest input representation only
- Mockito interaction verification: none
- network access: none
- server startup: none
- sleep: none

## Initial execution

Surefire result:

- tests: 10
- failures: 0
- errors: 0
- skipped: 0
- execution time reported by Surefire: 0.909 s

Result:

`10 / 10 PASS`

## Mockito / Java 25 runtime warning

During execution Mockito emitted its self-attachment compatibility warning and
the JVM reported dynamic Byte Buddy agent loading.

This warning did not indicate a test failure:

- Maven process exit code: 0
- Surefire failures: 0
- Surefire errors: 0

The experimental test suite and the shared Maven configuration were therefore
not modified merely to suppress the warning.

The warning is retained as an environment/reproducibility note because future
JDK releases may disallow dynamic agent loading by default.

## Adequacy chronology

At T_BB freeze time:

- JaCoCo measurement for this class: NOT RUN
- PIT measurement for this class: NOT RUN
- control-flow-guided additions: NOT DESIGNED
- mutation-guided additions: NOT DESIGNED

Therefore the initial black-box baseline was frozen without adequacy feedback.

The next step is to measure the two selected structural adequacy metrics:

- Line Coverage
- Branch Coverage

Only after those measurements will the separate T_CF evolution be designed.

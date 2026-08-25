# M4 RedisFilterBolt - C1

Target: org.apache.storm.redis.bolt.RedisFilterBolt

## Generation

- Input source: C0.
- C0 SonarCloud diagnostic: NSMELLS = 0.
- Tests supplied to Copilot: none.
- Copilot described generic typing of the GEO result as its improvement.
- That claimed improvement was already present in C0.
- Effective C0-to-C1 source difference: none.
- C1 is byte-identical to C0.

## Validation

- Compilation: PASS.
- C0 NSMELLS: 0.
- C1 NSMELLS: 0.
- New Sonar code smells: none.

## Post-hoc experimental suites

| Suite | Result |
| --- | --- |
| T_BB | 11/11 PASS |
| T_CF | 4/4 PASS |
| T_MT | N/A - no derived T_MT suite |
| T_RND | 11/11 PASS |
| T_ES | 9/9 PASS |
| T_LLM | 11/11 PASS |

Executable experimental tests: 46/46 PASS.

Native Apache Storm tests were not executed.

T_ES used the dedicated Java 25 compatibility launcher.
Launcher result: COMPAT_RESULT tests=9 failures=0 ignored=0 successful=true.

## Identity

C0 SHA-256: F60776FC3FC9E884843794506D05CB514B5CD2EA8B5CD3519DDA8C2CA69A1E7B
C1 SHA-256: F60776FC3FC9E884843794506D05CB514B5CD2EA8B5CD3519DDA8C2CA69A1E7B

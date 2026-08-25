# M4 RedisFilterBolt - C2

Target: org.apache.storm.redis.bolt.RedisFilterBolt

## Generation matrix

- Starting source: C0 original.
- Sonar diagnostic supplied: yes.
- T_BB supplied to Copilot: yes, 11 tests.
- T_CF supplied: no.
- T_MT supplied: no.
- T_RND supplied: no.
- T_ES supplied: no.
- T_LLM supplied: no.

## Effective refactoring

- C2 contains a real source-level refactoring relative to C0.
- Redis command selection was extracted from process into isPresent.
- GEO-result handling was extracted into containsGeoPosition.
- The mutable found variable was removed in favor of direct returns.
- Documentation and formatting were also revised.

Copilot additionally claimed that it eliminated a raw List type.
That claim is not counted as an effective refactoring because C0 already used List<GeoCoordinate>.

## Compilation and SonarCloud

- Compilation: PASS.
- C0 NSMELLS: 0.
- C2 NSMELLS: 0.
- Delta NSMELLS: 0.
- New code smells: none.

## Validation

| Suite | Result | Supplied to Copilot |
| --- | --- | --- |
| T_BB | 11/11 PASS | YES |
| T_CF | 4/4 PASS | NO |
| T_MT | N/A - no derived suite | NO |
| T_RND | 11/11 PASS | NO |
| T_ES | 9/9 PASS | NO |
| T_LLM | 11/11 PASS | NO |

Executable experimental tests: 46/46 PASS.

Native Apache Storm tests were not executed.

The transient RND and ES classpath errors encountered during manual execution were infrastructure issues.
After supplying the required runtime dependencies, both frozen suites passed completely without modifying C2 or the tests.

## Identity

C2 SHA-256: 4D6E40479F3DC4C4FED5F037269619F2A1545F5E882BBB69248671E2081FA0E0

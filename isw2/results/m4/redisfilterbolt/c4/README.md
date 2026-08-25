# M4 RedisFilterBolt - C4

Target: org.apache.storm.redis.bolt.RedisFilterBolt

## Generation matrix

- Starting source: C0 original.
- Sonar diagnostic supplied: yes.
- T_BB supplied to Copilot: yes, 11 executable tests.
- T_CF supplied to Copilot: yes, 4 executable tests.
- T_MT supplied as experimental context: yes.
- T_MT derived executable tests: 0.
- T_MT status: NO_DERIVED_SUITE.
- T_RND supplied: no.
- T_ES supplied: no.
- T_LLM supplied: no.

BB, CF and T_MT context were combined into one attachment because of the Copilot three-file attachment limit.
No mutation-testing test was invented or synthesized.

## Effective refactoring

- C4 contains a real source-level refactoring relative to C0.
- Pool-specific validation was extracted into validatePoolConfiguration.
- Redis command dispatch was extracted into isPresent.
- GEO handling was extracted into hasGeoPosition.
- The unnecessary jedisCommand null initialization was removed.
- The validation message was extracted into MISSING_ADDITIONAL_KEY_MESSAGE.

Copilot also claimed that the GEO List raw type was replaced by List<GeoCoordinate>.
That claim is not counted as an effective refactoring because C0 already used List<GeoCoordinate>.

## Compilation and SonarCloud

- Compilation: PASS.
- C0 NSMELLS: 0.
- C4 NSMELLS: 0.
- Delta NSMELLS: 0.
- New code smells: none.

## Validation

| Suite | Result | Supplied to Copilot |
| --- | --- | --- |
| T_BB | 11/11 PASS | YES |
| T_CF | 4/4 PASS | YES |
| T_MT | N/A - no derived suite | CONTEXT ONLY |
| T_RND | 11/11 PASS | NO |
| T_ES | 9/9 PASS | NO |
| T_LLM | 11/11 PASS | NO |

Executable experimental tests: 46/46 PASS.

Native Apache Storm tests were not executed.

## Identity

C4 SHA-256: DC66EEBB9065E0C101FD9C833DDC0412B20D4409435217CCE0549D07DF0EB0FE

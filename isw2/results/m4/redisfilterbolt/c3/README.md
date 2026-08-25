# M4 RedisFilterBolt - C3

Target: org.apache.storm.redis.bolt.RedisFilterBolt

## Generation matrix

- Starting source: C0 original.
- Sonar diagnostic supplied: yes.
- T_BB supplied to Copilot: yes, 11 tests.
- T_CF supplied to Copilot: yes, 4 tests.
- Total tests supplied during generation: 15.
- T_MT supplied: no.
- T_RND supplied: no.
- T_ES supplied: no.
- T_LLM supplied: no.

BB and CF were combined into one attachment only because of the Copilot three-file attachment limit.
They remained two distinct test suites.

## Effective refactoring

- C3 contains a real source-level refactoring relative to C0.
- Pool-specific validation was extracted into validatePoolConfiguration.
- Redis command dispatch was extracted into matchesRedisEntry.
- GEO handling was extracted into hasGeoPosition.
- The mutable found variable and unnecessary jedisCommand null initialization were removed.
- process preserves the original placement of getKeyFromTuple outside the try/catch.

Copilot also claimed that the GEO List raw type was replaced by List<GeoCoordinate>.
That claim is not counted as an effective refactoring because C0 already used List<GeoCoordinate>.

## Compilation and SonarCloud

- Compilation: PASS.
- C0 NSMELLS: 0.
- C3 NSMELLS: 0.
- Delta NSMELLS: 0.
- New code smells: none.

## Validation

| Suite | Result | Supplied to Copilot |
| --- | --- | --- |
| T_BB | 11/11 PASS | YES |
| T_CF | 4/4 PASS | YES |
| T_MT | N/A - no derived suite | NO |
| T_RND | 11/11 PASS | NO |
| T_ES | 9/9 PASS | NO |
| T_LLM | 11/11 PASS | NO |

Executable experimental tests: 46/46 PASS.

Native Apache Storm tests were not executed.

## Identity

C3 SHA-256: 155641571859135A46DA7F02E557D2A228B5AE24F3F6DD6FF9BC4E6930B0CD27

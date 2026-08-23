# RedisFilterBolt T_BB coverage baseline

## Measurement point

This measurement was performed only after:

1. the black-box Category Partition was frozen;
2. the eleven black-box frames were frozen;
3. the executable T_BB suite was frozen;
4. the suite passed 11/11 tests.

No T_BB test was modified using coverage feedback.

Target:

`org.apache.storm.redis.bolt.RedisFilterBolt`

Test suite:

`isw2/testing/src/test/java/it/uniroma2/isw2/storm/testing/redisfilterbolt/bb/RedisFilterBoltBBTest.java`

Test source SHA-256:

`BBB539411F70DDF94FD0EBF1EE0FC697160895C867ECE4BE5601CBB061AADBFD`

## Tool

JaCoCo:

`0.8.15`

The report was generated against exactly one production class from the
Apache Storm 3.0.0 `storm-redis` artifact.

## Baseline

| Metric | Covered | Total | Coverage |
| --- | ---: | ---: | ---: |
| Line | 40 | 45 | 88.8889% |
| Branch | 16 | 21 | 76.1905% |
| Method | 4 | 4 | 100% |
| Instruction | 145 | 170 | 85.2941% |

The two official adequacy metrics for the experiment remain:

- Line Coverage;
- Branch Coverage.

Unlike the rejected `DefaultHttpCredentialsPlugin` candidate, the
RedisFilterBolt black-box baseline does not saturate either official metric.

Therefore both metrics can legitimately be increased by the subsequent
manual control-flow-guided evolution.

## Structural adequacy gaps

The baseline contains five missed executable lines and five missed branches.

The exact missed or partially covered source lines are recorded in:

`isw2/testing/results/redis-filter-bolt/tbb/coverage-gap.csv`

The relevant control-flow areas are:

- the JedisPool constructor condition checking SET without an additional key;
- the unvisited default arm of the Redis data-type switch;
- the null short-circuit outcome of the GEO position test;
- the exception-reporting/failure path.

These gaps are inspected only now, after T_BB was frozen.

They may be used to design the subsequent T_CF additions.

## Measurement artifacts

Temporary JaCoCo files remain under the ignored Maven target directory.

Their identities for this baseline are:

- `jacoco.exec`:
  `BB2809D3F84F6557B0B187B2955C2FA0A6CDA047B2E62E7966D64809E723C27D`
- `jacoco.csv`:
  `D9D764541E7D0C6342E54F230A5DE3E92DADD131795D978E05634CC5E523D59E`
- `jacoco.xml`:
  `3BE4E9D3951BC7E1ED6EFF5DEE640281CFFBE5A21EB90D6F89DBC2AE5DAEDA36`

PIT was not run during this phase.

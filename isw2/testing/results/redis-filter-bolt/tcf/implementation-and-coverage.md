# RedisFilterBolt T_CF implementation and adequacy evolution

## Starting baseline

Frozen T_BB:

- 11 tests;
- 11/11 passed;
- Line Coverage: 40/45 = 88.8889%;
- Branch Coverage: 16/21 = 76.1905%.

The T_BB suite remained unchanged throughout T_CF.

## Frozen T_CF design

The coverage-guided design added exactly four tests:

- RFB-CF-01: SET with a valid additional key in the JedisPool constructor;
- RFB-CF-02: SET without an additional key;
- RFB-CF-03: GEO lookup returning null;
- RFB-CF-04: unsupported LIST processing and the process error path.

The executable implementation is:

`isw2/testing/src/test/java/it/uniroma2/isw2/storm/testing/redisfilterbolt/cf/RedisFilterBoltCFTest.java`

SHA-256:

`12CE1CEC5BFB643D3E85AA4DCE77C03A8E96902A8DBE8B12814EFF1B0E9BDF00`

## Execution

Combined population:

- T_BB: 11 tests;
- T_CF additions: 4 tests;
- total: 15 tests.

Result:

`15 passed / 0 failures / 0 errors / 0 skipped`

## Adequacy evolution

| Metric | T_BB | BB + CF | Delta |
| --- | ---: | ---: | ---: |
| Line Coverage | 40/45 = 88.8889% | 45/45 = 100.0000% | +5 lines, +11.1111 pp |
| Branch Coverage | 16/21 = 76.1905% | 21/21 = 100.0000% | +5 branches, +23.8095 pp |

Both official adequacy metrics increased.

No executable or partial Line/Branch gap remains in RedisFilterBolt under
JaCoCo 0.8.15.

Method Coverage is also 4/4 = 100.0000%.

Instruction Coverage is 170/170 = 100.0000%.

## Interpretation

The T_CF additions were not chosen from the original functional
specification.

They were designed only after T_BB had been frozen and measured, using the
recorded control-flow gaps.

The four additions map directly to the previously uncovered outcomes and no
extra tests were added after observing that full Line and Branch Coverage had
been achieved.

## Locale note

An intermediate PowerShell display incorrectly rendered the stored T_BB
percentage strings because a comma decimal separator was interpreted as a
thousands separator.

This affected display only.

All official values are recomputed here directly from the integer JaCoCo
counters:

- Line: 40/45 -> 45/45;
- Branch: 16/21 -> 21/21.

## Mutation-testing independence

PIT has not yet been run.

Therefore no mutation information influenced either the frozen T_BB suite or
the T_CF additions.

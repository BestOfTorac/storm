# RedisFilterBolt - T_CF control-flow-guided additions

## Starting point

T_CF is designed only after the independent black-box T_BB baseline was
frozen, executed and measured.

Frozen T_BB:

- tests: 11;
- Line Coverage: 40/45 = 88.8889%;
- Branch Coverage: 16/21 = 76.1905%.

The official adequacy metrics are Line Coverage and Branch Coverage.

T_CF therefore adds tests specifically to exercise control-flow outcomes that
were not reached by T_BB.

The eleven frozen T_BB tests are not modified or removed.

## Observed T_BB gaps

JaCoCo identified missed or partially covered control flow at:

- line 61: compound SET/additional-key constructor condition;
- line 63: exception for SET without an additional key;
- line 94: one unvisited data-type switch outcome;
- line 117: the `geopos == null` short-circuit outcome;
- line 127: unsupported data-type exception;
- lines 135-137: process exception reporting and tuple failure.

## Selected T_CF additions

Exactly four new tests are selected.

### RFB-CF-01 - SET with valid additional key in JedisPool constructor

Purpose:

Exercise the compound constructor condition with:

- `dataType == SET` = true;
- `additionalKey == null` = false.

Expected behavior:

Construction succeeds.

This complements the T_BB pool-constructor cases where the first condition
was false.

### RFB-CF-02 - SET without additional key in JedisPool constructor

Purpose:

Exercise the compound constructor condition with:

- `dataType == SET` = true;
- `additionalKey == null` = true.

Expected behavior:

Construction throws `IllegalArgumentException`.

This reaches the previously uncovered executable line 63.

### RFB-CF-03 - GEO lookup returns null

Purpose:

Exercise the first operand of:

`geopos == null || geopos.isEmpty()`

as true.

Expected behavior:

The tuple is filtered rather than emitted and is acknowledged.

This is distinct from the frozen T_BB empty-list GEO fixture, which evaluated
the first operand as false and the second operand as true.

### RFB-CF-04 - unsupported LIST processing

Purpose:

Exercise the previously unvisited default outcome of the Redis-data-type
switch.

`LIST` exists in the RedisDataType enum but is not among the six data types
documented as supported by RedisFilterBolt.

Expected externally observable process outcome:

- the processing exception is reported;
- the input tuple is failed;
- the input tuple is not acknowledged;
- the input tuple is not emitted.

This test reaches the default exception and the process exception handler.

## Minimality

Four tests are retained because the uncovered compound condition at line 61
requires two different new outcomes:

1. SET with a non-null additional key;
2. SET with a null additional key.

The GEO null short-circuit and the switch default/exception path require one
additional test each.

Therefore these four additions map directly to distinct uncovered
control-flow outcomes.

No additional test is introduced solely to inflate the suite size.

## Independence

T_CF is explicitly coverage-guided and therefore may use the recorded T_BB
JaCoCo gap.

PIT has still not been run and provides no information to this design.

The production class and the frozen T_BB suite remain unchanged.

# RedisFilterBolt mutation baseline

## Measurement point

Mutation testing was performed only after the manual black-box and
control-flow-guided suites had been frozen.

Manual suite:

- T_BB: 11 tests;
- T_CF: 4 additions;
- total: 15 tests.

All 15 tests passed before mutation testing.

The frozen adequacy values were:

- Line Coverage: 45/45 = 100.0000%;
- Branch Coverage: 21/21 = 100.0000%.

No PIT information influenced T_BB or T_CF.

## PIT configuration

The valid baseline used:

- PIT 1.25.8;
- pitest-junit5-plugin 1.2.3;
- JUnit Platform Launcher 6.1.1;
- mutator group: DEFAULTS;
- threads: 1;
- target class: `org.apache.storm.redis.bolt.RedisFilterBolt`;
- test population: frozen T_BB + T_CF only.

PIT was launched through its direct command-line entry point because
RedisFilterBolt belongs to the external `storm-redis` Maven artifact rather
than to the native production output of the isolated testing module.

An earlier Maven-plugin attempt was discarded because PIT explicitly skipped
the testing module as having no native production code.

## Raw result

| Measure | Result |
| --- | ---: |
| Mutants | 12 |
| Killed | 11 |
| Survived | 1 |
| No coverage | 0 |
| Timed out | 0 |
| Raw Mutation Score | 11/12 = 91.6667% |
| Raw Test Strength | 11/12 = 91.6667% |

The raw PIT values are retained and must not be replaced by the adjusted
values below.

## Surviving mutant

The only survivor occurs in `process` at source line 117:

`if (geopos == null || geopos.isEmpty())`

PIT reports two
`RemoveConditionalMutator_EQUAL_ELSE`
mutations at this source line.

One is killed and one survives.

The production bytecode contains the two relevant conditional jumps:

- `ifnull 203`, associated with the null guard;
- `ifeq 208`, associated with the empty-list shortcut.

For the EQUAL_ELSE mutation, removal forces the existing branch target.

The first mutation can change a valid non-empty GEO result into the
`found=false` path and is therefore observable. The frozen GEO-present
black-box test kills it.

The surviving mutation corresponds to bypassing the `isEmpty()` shortcut and
continuing to:

`geopos.stream().anyMatch(Objects::nonNull)`

For the intended Redis/Jedis operational domain this is behaviorally
equivalent:

- null list: the preceding null guard still handles the input;
- empty list: `anyMatch` on the empty stream is false;
- non-empty all-null list: `anyMatch` is false;
- non-empty list containing a coordinate: `anyMatch` is true.

Therefore removal of the empty-list shortcut does not alter the externally
observable filtering decision in the intended operational domain.

The survivor is classified as:

`EQUIVALENT_IN_INTENDED_OPERATIONAL_DOMAIN`

## Adjusted interpretation

After manually excluding the one equivalent survivor:

- non-equivalent mutant population: 11;
- killed non-equivalent mutants: 11;
- remaining killable survivors: 0;
- adjusted Mutation Score: 11/11 = 100.0000%;
- adjusted Test Strength: 11/11 = 100.0000%.

These adjusted values are secondary analytical values.

The raw PIT Mutation Score and Test Strength remain 91.6667%.

## Mutation-guided test decision

No T_MT test is added.

Adding a test against an intentionally inconsistent or pathological `List`
implementation solely to distinguish `isEmpty()` from empty-stream
`anyMatch()` would test an artificial collaborator behavior rather than a
meaningful RedisFilterBolt requirement.

There are no remaining non-equivalent survivors to target.

Therefore the final manual suite remains:

- T_BB: 11;
- T_CF: 4;
- mutation-guided additions: 0;
- total: 15 tests.

This result also demonstrates that 100% Line and 100% Branch Coverage do not
by themselves imply a raw PIT Mutation Score of 100%.

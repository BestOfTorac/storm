# UIHelpers - T_CF Mutation Testing Baseline

## Scope

Class under test:

`org.apache.storm.daemon.ui.UIHelpers`

Input test suite:

`T_CF`

- T_BB tests: 35
- additional control-flow-guided tests: 10
- total T_CF tests: 45

Mutation testing tool:

`PIT 1.25.8`

Mutator set:

`DEFAULTS`

Only UIHelpers production bytecode is mutated.
Apache Storm native tests are not part of the experiment.

## Mutation baseline

| Outcome | Mutants |
|---|---:|
| Generated | 338 |
| Killed | 115 |
| Survived | 61 |
| No Coverage | 162 |
| Timed Out | 0 |
| Run Error | 0 |
| Memory Error | 0 |

### Mutation score

Mutation Score is computed over all generated mutants:

`115 / 338 = 34.02%`

### Test strength

Mutants actually reached by the T_CF suite are:

`115 + 61 = 176`

Test Strength is therefore:

`115 / 176 = 65.34%`

### No-coverage proportion

`162 / 338 = 47.93%`

The distinction between mutation score and test strength is important because
a substantial fraction of the mutations lies in code that is not reached by
T_CF.

## Relation with structural coverage

The mutation run reports line coverage of:

`492 / 1122`

This is consistent with the frozen T_CF JaCoCo result:

`43.85%`

## Evolution protocol

T_CF is frozen before mutation-guided test evolution.

The next suite, T_MT, will be obtained by adding a small number of tests
designed after inspecting PIT surviving mutants.

Priority is given to mutants that:

1. occur in already covered code;
2. represent observable behavioral changes;
3. can be killed with simple, maintainable assertions;
4. allow a single test to distinguish multiple surviving mutants.

No-coverage mutants are analyzed separately and are not automatically treated
as test weaknesses requiring new tests.

The goal of T_MT is to increase mutation adequacy while keeping the evolution
traceable and explainable.

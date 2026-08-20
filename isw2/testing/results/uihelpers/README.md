# UIHelpers Testing Evidence Index

Class:

`org.apache.storm.daemon.ui.UIHelpers`

This directory is the evidence index for the complete testing experiment.

## Completed stages

### T_BB - Category Partition / Black Box

- tests: 35
- line coverage: 39.13%
- branch coverage: 35.12%

Evidence:

`coverage/tbb/`

### T_CF - Control-Flow-Guided Evolution

- added tests: 10
- total tests: 45
- line coverage: 43.85%
- branch coverage: 50.41%

Coverage improvement from T_BB:

- line coverage: +4.72 pp
- branch coverage: +15.29 pp

Evidence:

`coverage/tcf/`

### T_CF - Mutation Baseline

- generated mutants: 338
- killed: 115
- survived: 61
- no coverage: 162
- mutation score: 34.02%
- test strength: 65.34%

Evidence:

`mutation/tcf/`

### T_MT - Mutation-Guided Evolution

- added mutation-guided tests: 8
- total tests: 53
- generated mutants: 338
- killed: 155
- survived: 25
- no coverage: 158
- mutation score: 45.86%
- test strength: 86.11%

Mutation improvement from T_CF:

- additional killed mutants: +40
- surviving mutants: 61 -> 25
- mutation score: +11.84 pp
- test strength: +20.77 pp

Evidence:

`mutation/tmt/`

### Reliability

Operational profile:

`uniform over the final 53 T_MT tests`

Observed outcomes:

- passed: 53
- failures: 0
- errors: 0
- skipped: 0

Estimate:

- reliability: 1.000000 (100%)
- estimated failure probability: 0.000000

This value is explicitly interpreted as an empirical estimate relative
to the finite uniform test profile, not as universal production
reliability.

Evidence:

`reliability/`

### T_RND - Randoop Random Generation

Generation:

- generator: Randoop 4.3.4
- seed: 20260820
- target test budget: 35
- final output-limit: 37
- generated tests: 35
- passing tests: 35/35
- manual selection/editing: none
- coverage/mutation feedback during generation: none

Structural adequacy:

- line coverage: 115/1122 = 10.25%
- branch coverage: 30/242 = 12.40%

Mutation adequacy:

- mutation population: 338
- killed: 32
- survived: 39
- no coverage: 267
- mutation score: 9.47%
- test strength: 45.07%

The suite was frozen before adequacy measurement and its SHA-256 hashes
remained unchanged.

Generated tests:

`../../generated-tests/uihelpers/rnd/`

Evidence:

`automatic/rnd/`

## Documentation policy

For each experimental phase the repository preserves:

- methodology;
- tool and dependency versions;
- test-suite composition;
- design rationale;
- relevant technical problems;
- adopted solutions;
- raw machine-readable evidence;
- final metrics;
- delta from the previous phase.

This evidence is maintained incrementally so that the final project
report can be built from verified repository artifacts rather than from
memory.

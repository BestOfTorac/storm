# RedisFilterBolt - Reliability Estimation

## Target

`org.apache.storm.redis.bolt.RedisFilterBolt`

## Final manually evolved executable test suite

The reliability experiment uses the frozen final manually evolved
executable suite:

- T_BB: 11 tests
- T_CF: 4 tests
- mutation-guided additional executable tests: 0
- total: 15 tests

For RedisFilterBolt the mutation-analysis phase did not require the
addition of a further executable T_MT test. Therefore the final manual
test set used for reliability estimation is T_BB + T_CF.

## Operational profile

The operational profile is uniform over the final test set.

For N = 15:

p_i = 1 / N = 1 / 15

Therefore:

p_i = 0.066666666667

for every final test case.

## Reliability estimator

For each test:

success_i = 1 when the test passes

success_i = 0 otherwise

The empirical estimate is:

R_hat = SUM(p_i * success_i)

Under the uniform profile:

R_hat = passed_tests / total_tests

The estimated failure probability is:

Q_hat = 1 - R_hat

## Execution

Timestamp: 2026-09-01T17:00:59+02:00

Repository commit:

`611a496af8edc4a0d62191ff8a0f5ed70a789f9d`

Observed results:

- total tests: 15
- T_BB tests: 11
- T_CF tests: 4
- passed: 15
- failures: 0
- errors: 0
- skipped: 0

## Result

R_hat = 15 / 15

R_hat = 1.000000

Q_hat = 0.000000

## Interpretation

The estimate is relative to the explicitly defined finite operational
profile consisting of uniform probability over the 15 final manual
test cases.

It must not be interpreted as a claim that RedisFilterBolt is perfectly
reliable for every possible production execution.

Reliability, structural coverage and mutation adequacy remain distinct
experimental observations.

## Reproducibility

Evidence produced:

- `operational-profile.csv`
- `reliability-summary.csv`
- `reliability.md`

No production source and no frozen test source was edited, regenerated
or repaired during this experiment.

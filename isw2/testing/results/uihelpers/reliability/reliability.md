# UIHelpers - Reliability Estimation

## Objective

Estimate the reliability of:

`org.apache.storm.daemon.ui.UIHelpers`

using the final manually evolved test suite.

The final suite is:

`T_MT = T_BB + T_CF additions + T_MT additions`

Composition:

- T_BB: 35 tests
- control-flow-guided additions: 10 tests
- mutation-guided additions: 8 tests
- final T_MT: 53 tests

## Requirement interpretation

The Software Testing project specification requires the reliability of
each selected class to be estimated assuming an operational profile with
uniform usage probability over the final set of tests obtained after
test-suite evolution.

The specification defines the uniform-profile assumption but does not
prescribe a more specific numerical estimator in the project slide.

For this experiment an explicit empirical estimator is therefore used.

## Operational profile

Let the final test set be:

T = {t1, ..., tN}

with:

N = 53

Under a uniform operational profile every test has probability:

p_i = 1 / N = 1 / 53

Therefore:

p_i = 0.018867924528 approximately.

The complete finite profile is stored in:

`operational-profile.csv`

with one row for every test case.

## Reliability estimator

For each test:

success_i = 1 if the test passes
success_i = 0 otherwise

The empirical reliability estimate is:

R_hat = SUM(p_i * success_i)

Because the operational profile is uniform this is equivalent to:

R_hat = number_of_passed_tests / number_of_tests

The estimated failure probability is:

Q_hat = 1 - R_hat

## Experimental execution

The complete frozen T_MT suite was executed again specifically for the
reliability experiment.

Observed outcomes:

- total tests: 53
- passed: 53
- failures: 0
- errors: 0
- skipped: 0

## Result

R_hat = 53 / 53

R_hat = 1.000000

Estimated reliability under the stated finite uniform operational
profile:

`100%`

Estimated failure probability:

Q_hat = 0 / 53 = 0.000000

## Interpretation and limitation

The value 1.000000 must not be interpreted as a claim that UIHelpers is
perfectly reliable for every possible production execution.

It is an empirical reliability estimate relative to the explicitly
defined operational profile used in this experiment: uniform
probability over the 53 final T_MT test cases.

The result states that no failure was observed for any operation
represented by this finite test profile.

The previous adequacy analyses remain necessary because successful
execution alone does not describe the strength of the test suite.

In particular, before reliability estimation the same final suite was
also assessed through:

- line coverage;
- branch coverage;
- mutation testing.

Mutation-guided evolution increased test strength from 65.34% to 86.11%,
while all final tests continued to pass on the original class.

This distinction is important: reliability describes observed failure
behavior under the selected operational profile, while structural and
mutation adequacy describe different properties of test quality.

## Reproducibility

The reliability evidence consists of:

- `operational-profile.csv`
  - every final test;
  - its observed outcome;
  - its uniform operational probability;
  - its contribution to the reliability estimate;

- `reliability-summary.csv`
  - suite-level counts;
  - probability per test;
  - final reliability;
  - estimated failure probability;

- this methodological document.

These artifacts are intended to support the final project report.

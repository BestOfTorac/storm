# UIHelpers - T_BB Coverage Baseline

## Scope

Class under test:

`org.apache.storm.daemon.ui.UIHelpers`

Suite:

`T_BB`

The suite contains the manually defined strict black-box tests produced from
the Category Partition activity.

Only the isolated ISW2 testing harness is executed. Apache Storm native tests
are not part of this measurement.

## Test execution

- Test classes: 10
- Tests: 35
- Failures: 0
- Errors: 0
- Skipped: 0

## Adequacy metrics

The two adequacy metrics selected for manual test-suite evolution are:

1. Line Coverage
2. Branch Coverage

JaCoCo version: `0.8.15`.

### T_BB baseline

| Metric | Covered | Missed | Total | Coverage |
|---|---:|---:|---:|---:|
| Line | 439 | 683 | 1122 | 39.13% |
| Branch | 85 | 157 | 242 | 35.12% |

Additional JaCoCo counters:

- Instructions covered: 2193
- Instructions missed: 3609
- Methods covered: 43
- Methods missed: 79
- Complexity covered: 70
- Complexity missed: 173

## Evolution protocol

This baseline is frozen before coverage-guided manual test evolution.

The existing `T_BB` tests remain unchanged when constructing the next suite.
Coverage-guided tests are added to obtain `T_CF`.

The objective of `T_CF` is to increase both selected adequacy metrics compared
with this baseline:

- Line Coverage > 39.13%
- Branch Coverage > 35.12%

The exact JaCoCo CSV and XML reports used for this baseline are stored in this
directory.

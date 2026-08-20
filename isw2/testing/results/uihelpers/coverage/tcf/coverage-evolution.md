# UIHelpers - Control-Flow Coverage Evolution

## Scope

Class under test:

`org.apache.storm.daemon.ui.UIHelpers`

Selected adequacy metrics:

1. Line Coverage
2. Branch Coverage

JaCoCo version: `0.8.15`.

## T_BB baseline

The manually designed Category Partition black-box suite was frozen before
control-flow-guided evolution.

- T_BB tests: 35
- Failures: 0
- Errors: 0
- Skipped: 0

| Metric | Covered | Missed | Total | Coverage |
|---|---:|---:|---:|---:|
| Line | 439 | 683 | 1122 | 39.13% |
| Branch | 85 | 157 | 242 | 35.12% |

## Control-flow-guided evolution

The source code and JaCoCo coverage gaps were inspected to identify uncovered
or partially covered paths.

The original T_BB tests were kept unchanged. New manual control-flow-guided
tests were added in a separate test class.

### Batch 1

Added tests: 4

Main targets:

- JSONP callback and additional-header branches
- SSL trust-store and client-authentication branches
- HTTPS logviewer path
- HTTP logviewer fallback path

Result:

| Metric | Coverage |
|---|---:|
| Line | 41.27% |
| Branch | 41.74% |

Increment from T_BB:

- New covered lines: 24
- New covered branches: 16

### Batch 2

Added tests: 3

Main targets:

- invalid JSONP callback paths
- unset owner-resource optional fields
- non-empty cluster/resource aggregation

Result:

| Metric | Coverage |
|---|---:|
| Line | 42.16% |
| Branch | 45.45% |

Increment produced by Batch 2:

- New covered lines: 10
- New covered branches: 9

### Batch 3

Added tests: 3

Main targets:

- exception-to-JSON message branches
- stream-name sanitization
- transferred-stat sanitization

Result:

| Metric | Coverage |
|---|---:|
| Line | 43.85% |
| Branch | 50.41% |

Increment produced by Batch 3:

- New covered lines: 19
- New covered branches: 12

## Final T_CF

T_CF is defined as:

`T_CF = T_BB + control-flow-guided tests`

- T_BB tests: 35
- Added CF tests: 10
- Final T_CF tests: 45
- Failures: 0
- Errors: 0
- Skipped: 0

| Metric | T_BB | T_CF | Delta |
|---|---:|---:|---:|
| Line Coverage | 39.13% | 43.85% | +4.72 pp |
| Branch Coverage | 35.12% | 50.41% | +15.29 pp |

Absolute improvement:

- Covered lines: 439 -> 492 (+53)
- Covered branches: 85 -> 122 (+37)

Both selected adequacy metrics increased during every control-flow-guided
evolution batch.

T_CF is frozen at this point and becomes the input suite for the subsequent
mutation-testing phase.

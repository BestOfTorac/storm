# GitHub Actions CI - ISW2 Testing

## Objective

The ISW2 testing harness is integrated into GitHub Actions so that the
test suites developed for the Software Testing project are automatically
compiled and executed after changes to the project branch.

The integration is intended to make the developed tests part of a
repeatable Continuous Integration cycle rather than relying only on
local executions.

## Workflow

Workflow file:

`.github/workflows/isw2-testing.yml`

Triggers:

- push to `isw2-project`;
- pull request targeting `isw2-project`;
- manual `workflow_dispatch`.

## Environment

The CI job currently uses:

- GitHub-hosted Ubuntu runner;
- Microsoft OpenJDK 25;
- Maven;
- Maven dependency caching.

Java 25 matches the bytecode/runtime configuration used for the
Apache Storm 3.0.0 testing experiments.

## Current test suites

### Manual evolved suite

The workflow executes:

- T_BB: 35 tests;
- T_CF additions: 10 tests;
- T_MT additions: 8 tests.

Expected total:

`53 / 53 PASS`

These are executed through Maven Surefire.

### Random suite

The frozen Randoop T_RND suite is executed separately through JUnit 4.

Expected result:

`35 / 35 PASS`

The generated Randoop sources remain unchanged.

## Native Apache Storm tests

Apache Storm native test suites are not part of this testing harness.

The CI workflow also audits the dependency classpath used for T_RND and
fails if a `-tests.jar` artifact is detected.

## Separation of experimental suites

T_BB, T_CF, T_MT and T_RND remain logically separate.

The fact that they are executed by the same CI workflow does not merge
their experimental results.

Coverage and mutation adequacy continue to be measured independently for
each required experimental suite.

## Evolution

The workflow is intentionally designed to evolve with the project.

When the additional automatic suites and the second selected class are
completed, their regression checks can be added to the same CI pipeline.

## Reproducibility

A successful GitHub Actions execution demonstrates that the repository
can reproduce the expected test results in a clean remote environment,
independently from the developer workstation.

## First successful remote execution

The first execution of the `ISW2 Testing` GitHub Actions workflow
completed successfully on the `isw2-project` branch.

Observed GitHub Actions result:

- workflow: `ISW2 Testing`;
- trigger: push;
- status: `Success`;
- job: `UIHelpers test suites`;
- environment: GitHub-hosted Ubuntu runner;
- Java: Microsoft OpenJDK 25;
- manual suites: T_BB + T_CF + T_MT;
- expected manual tests: 53;
- random suite: T_RND / Randoop;
- expected random tests: 35;
- native Apache Storm tests: excluded.

The successful remote execution confirms that the current testing harness
is reproducible outside the local development workstation.

This CI validation does not replace the experimental coverage and
mutation measurements: those remain separately recorded for each suite.

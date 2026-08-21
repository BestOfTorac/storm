# EvoSuite — UIHelpers

Target class:

`org.apache.storm.daemon.ui.UIHelpers`

## Suite summary

- EvoSuite: 1.2.0
- Raw generated tests: 43
- Native-valid tests: 40
- Target-portable tests: 37
- Final measurement suite: `T_ES_TARGET` = 37 tests
- Target: Apache Storm 3.0.0 / Java 25

## Operational suite

The final EvoSuite suite is stored next to the other UIHelpers testing
strategies:

`src/test/java/it/uniroma2/isw2/storm/testing/uihelpers/es/`

Files:

- `generated/UIHelpers_ESTest.java`
- `generated/UIHelpers_ESTest_scaffolding.java`
- `runtime/EvoSuiteJava25Launcher.java`

The generated test file still contains all 43 original generated test
methods. The Java 25 compatibility launcher selects the 37 tests belonging
to `T_ES_TARGET`.

No generated test body or assertion was changed based on coverage,
mutation testing, or target execution feedback.

## Standard Maven isolation

The EvoSuite operational sources live under `src/test/java` so that the
suite is physically located next to BB, CF, MT, RND and LLM.

They are intentionally excluded from the standard Maven test compilation
and Surefire execution.

Reason:

EvoSuite 1.2.0 cannot execute its original EvoClassLoader/instrumentation
path against the Java 25 bytecode required by Apache Storm 3.0.0.

The suite is therefore compiled and executed through the independently
validated Java 25 compatibility protocol.

Standard `mvn test` was verified to:

- complete with BUILD SUCCESS;
- compile BB, CF, MT and RND normally;
- not compile EvoSuite operational sources;
- not execute EvoSuite through Surefire;
- keep integration tests separated from Surefire.

## Raw generation

Generation environment:

- Apache Storm 2.7.1
- Java 11
- EvoSuite 1.2.0
- original EvoRunner

The original generated test source and original EvoSuite scaffolding are
preserved byte-for-byte in:

`raw/`

## Native validation

The original 43-test suite was executed five times in its native
environment.

Stable result:

- 43 executed
- 40 passed
- 3 failed

Stable native failures:

- `test02`
- `test09`
- `test22`

## Port to Storm 3.0.0

The 43 generated test bodies were mechanically ported to Storm 3.0.0.

Five repeated executions of the complete target port produced:

- 43 executed
- 37 passed
- 6 failed

Stable failures:

- `test02`
- `test08`
- `test09`
- `test10`
- `test22`
- `test38`

## Exclusion classification

### Native-generation failures

- `test02`
- `test09`
- `test22`

These fail reproducibly in the original Storm 2.7.1 / Java 11
generation environment.

### Behavioral drift

- `test08`

This passes on Storm 2.7.1 both with and without EvoSuite instrumentation,
but fails on Storm 3.0.0 because the observed behavior of
`getJsonResponseHeaders` changed.

### EvoSuite instrumentation dependencies

- `test10` — VNET/network instrumentation
- `test38` — VFS/filesystem instrumentation

Both pass in the native EvoSuite-instrumented environment and fail on the
same Storm 2.7.1 environment when the EvoSuite instrumentation path is
bypassed.

Detailed evidence:

`evidence/excluded-tests.csv`

## Final target suite

`T_ES_TARGET` contains 37 target-portable tests.

Validation:

- 5/5 repeated executions passed;
- 37/37 tests passed on every execution;
- 0 failures;
- 0 ignored.

After moving the operational sources to `uihelpers/es`, the suite was
freshly recompiled from that location and again produced:

- 37 executed
- 37 passed
- 0 failed
- 0 ignored.

Old compiled EvoSuite classes were not used in that validation.

## Measurement protocol

`T_ES_TARGET` is frozen before JaCoCo and PIT measurement.

Coverage and mutation feedback were not used to:

- alter generated test bodies;
- alter assertions;
- select passing tests;
- classify exclusions.

The exclusions were established from native validity, release behavioral
drift and runtime compatibility evidence before measurement.

## Evidence

- `evidence/excluded-tests.csv`
- `evidence/repeatability.csv`
- `evidence/fingerprints.sha256`

<!-- T_ES_TROUBLESHOOTING_START -->

## Compatibility and troubleshooting log

This section records the main technical problems encountered while
building the EvoSuite test suite for `UIHelpers`, together with their
causes, solutions and validation evidence. It is intentionally kept in
the repository so that the final project report can be reconstructed
from reproducible evidence instead of memory.

### 1. Storm 3.0.0 Java baseline

**Problem**

An initial Java 21 execution path was attempted, but Apache Storm 3.0.0
classes could not be loaded.

**Cause**

The final Apache Storm 3.0.0 artifacts require Java 25 bytecode
(class-file major version 69). Java 21 supports class-file major version
65 and therefore cannot be the execution baseline for this target.

**Solution**

Use Microsoft OpenJDK 25 as the target execution JDK.

Java 11 is used only for the separate EvoSuite generation environment
described below.

---

### 2. EvoSuite 1.2.0 cannot analyze the Storm 3.0.0 bytecode directly

**Problem**

EvoSuite 1.2.0 could not directly analyze the classes belonging to the
Storm 3.0.0 / Java 25 target.

**Cause**

The ASM version bundled inside EvoSuite 1.2.0 predates Java 25 and cannot
parse class-file major version 69.

Changing only the JVM used to launch EvoSuite does not solve the problem,
because the incompatible bytecode is the bytecode being analyzed.

**Solution**

Generate the suite independently against Apache Storm 2.7.1 under
Java 11, where the target class can be analyzed by EvoSuite 1.2.0.

The original generation artifacts are preserved in `raw/`.

---

### 3. Mechanical port from Storm 2.7.1 to Storm 3.0.0

**Problem**

The generated source did not compile unchanged against Storm 3.0.0.

**Cause**

The target environment changed between releases. In particular, the
Jetty servlet namespace used by Storm changed.

The generated tests also depend on Clojure runtime types.

**Solution**

Apply only environment-compatibility changes:

- provide Clojure 1.10.3 on the dedicated EvoSuite classpath;
- migrate the generated Jetty `ServletHolder` import to the Jetty EE10
  namespace used by Storm 3.0.0.

No generated test input, oracle or assertion was changed.

The generated suite still contains all 43 generated test methods.

---

### 4. Original EvoRunner fails on Java 25

**Problem**

The mechanically ported tests compile on Java 25, but execution through
the original EvoSuite `EvoRunner` fails.

**Cause**

`EvoRunner` uses EvoSuite's legacy `EvoClassLoader`, which relies on the
same old ASM infrastructure and cannot process Java 25 compiled classes.

**Solution**

Introduce `EvoSuiteJava25Launcher`, a dedicated compatibility launcher
that:

- uses JUnit 4 directly;
- bypasses the legacy EvoSuite separate classloader;
- preserves the required EvoSuite runtime settings;
- does not modify generated test bodies.

---

### 5. Java module reflective-access restrictions

**Problem**

After bypassing `EvoClassLoader`, EvoSuite runtime initialization still
failed because of reflective access to JDK internals.

**Cause**

Modern Java module encapsulation prevents the old EvoSuite runtime from
reflectively accessing some JDK packages.

Two observed accesses were required:

- `java.desktop/java.awt`
- `java.base/java.net`

**Solution**

The dedicated EvoSuite Java 25 execution uses exactly:

`--add-opens=java.desktop/java.awt=ALL-UNNAMED`

and:

`--add-opens=java.base/java.net=ALL-UNNAMED`

No broader module-opening policy was introduced.

---

### 6. SecurityManager removal on Java 25

**Problem**

The generated EvoSuite scaffolding calls
`Sandbox.initializeSecurityManagerForSUT()`.

Java 25 throws an `UnsupportedOperationException` when this path attempts
to install a SecurityManager.

**Cause**

The Java SecurityManager mechanism used by legacy EvoSuite versions is no
longer available on the Java 25 target runtime.

**Solution**

Keep the original scaffolding unchanged in `raw/` and create a dedicated
Java 25 compatibility copy.

Only the SecurityManager initialization/reset operations were disabled:

- `Sandbox.initializeSecurityManagerForSUT()`
- `Sandbox.resetDefaultSecurityManager()`

The surrounding EvoSuite lifecycle calls, including
`goingToExecuteSUTCode()` and `doneWithExecutingSUTCode()`, remain.

---

### 7. Raw suite validation

The generated suite contains 43 tests.

It was executed five times in its native generation environment:

- Apache Storm 2.7.1
- Java 11
- original EvoRunner
- original EvoSuite instrumentation

Stable result on all five executions:

- 43 executed;
- 40 passed;
- 3 failed.

Stable native failures:

- `test02`
- `test09`
- `test22`

These tests are therefore classified as native-generation failures rather
than failures introduced by the Storm 3.0.0 port.

---

### 8. Full Storm 3.0.0 execution

The complete 43-test port was executed five times on:

- Apache Storm 3.0.0;
- Java 25;
- compatibility runtime.

Stable result:

- 43 executed;
- 37 passed;
- 6 failed.

Stable failures:

- `test02`
- `test08`
- `test09`
- `test10`
- `test22`
- `test38`

---

### 9. Failure-cause isolation

A control experiment executed the raw Storm 2.7.1 suite under Java 11
while bypassing EvoSuite instrumentation.

This distinguished target behavioral drift from instrumentation
dependencies.

#### `test08` — release behavioral drift

`test08` passes on Storm 2.7.1 both with and without EvoSuite
instrumentation but fails on Storm 3.0.0.

The generated oracle expects ten JSON response headers, while the
Storm 3.0.0 behavior produces eleven.

This is classified as release behavioral drift.

#### `test10` — VNET dependency

`test10` passes with the native EvoSuite instrumentation but fails when
that instrumentation is bypassed even on Storm 2.7.1.

The generated test relies on EvoSuite virtual-network behavior.

This is classified as a VNET instrumentation dependency.

#### `test38` — VFS dependency

`test38` also becomes failing when EvoSuite instrumentation is bypassed
on the original Storm 2.7.1 environment.

The generated test relies on EvoSuite virtual-filesystem behavior.

This is classified as a VFS instrumentation dependency.

---

### 10. Definition of the final EvoSuite suite

The three native-generation failures are excluded:

- `test02`
- `test09`
- `test22`

The target behavioral-drift test is excluded:

- `test08`

The two tests that cannot preserve their original EvoSuite instrumentation
semantics in the Java 25 compatibility runtime are excluded:

- `test10`
- `test38`

The final target-portable suite is therefore:

`T_ES_TARGET = 37 tests`

Five repeated executions produced:

- 37/37 passed;
- 0 failures;
- 0 ignored.

The selection was completed before JaCoCo and PIT measurements.

Coverage and mutation-testing feedback were not used to alter or select
the tests.

---

### 11. Physical placement next to the other test strategies

**Problem**

The first frozen EvoSuite artifacts were stored only under the dedicated
`evosuite/` evidence directory, making the actual suite visually distant
from BB, CF, MT and RND.

**Solution**

Place the operational suite under:

`src/test/java/it/uniroma2/isw2/storm/testing/uihelpers/es/`

The evidence directory keeps only:

- original raw generation artifacts;
- repeatability evidence;
- exclusion classification;
- fingerprints;
- this documentation.

The operational suite was freshly compiled from the new `es` location
and executed again.

Result:

- 37 executed;
- 37 passed;
- 0 failed;
- 0 ignored.

No previously compiled EvoSuite class was used in this validation.

---

### 12. Isolation from normal Maven tests

**Problem**

The operational EvoSuite source is physically under `src/test/java`, but
standard Maven execution must not attempt to compile or run it through
the incompatible original EvoSuite path.

**Solution**

The Maven Compiler Plugin excludes:

`**/uihelpers/es/**`

from standard test compilation.

Surefire also explicitly excludes:

`**/UIHelpers_ESTest.java`

The dedicated EvoSuite compatibility protocol compiles and executes the
suite separately.

Validation with standard `mvn test` produced:

- `BUILD SUCCESS`;
- BB compiled;
- CF compiled;
- MT compiled;
- RND compiled;
- no EvoSuite operational class compiled;
- no EvoSuite test executed by Surefire;
- no integration test executed by Surefire.

---

### 13. Git line-ending and fingerprint verification

**Problem**

During the first pre-commit fingerprint gate, the SHA-256 obtained from a
temporary file produced by `git checkout-index --temp` differed from the
stored source fingerprint.

Git also reported LF/CRLF conversion warnings.

**Cause**

A checkout representation is not a reliable source for calculating the
raw bytes stored in Git's index because checkout filters and line-ending
conversion can affect the produced temporary file.

Therefore the earlier comparison mixed two different representations:

- source/working-tree bytes;
- checkout/index representation bytes.

**Solution**

Two fingerprint concepts are now kept distinct.

**Source-origin fingerprints** identify the validated artifacts before
Git representation rules:

| Artifact | SHA-256 |
| --- | --- |
| Raw generated test | `67861A595589422E9DDC5B104E0386194F7F9AF674212CAB8AF53ADB27748235` |
| Raw generated scaffolding | `1E09A1DC121621D7A020573F90C0D0C0C4F515E2B2B691646BF3584C2B23B1F5` |
| Storm 3 ported test | `8D268D798EC1AC2D01389555F098ED5C1B6B373DC41A5E639A1BBDB867482AFD` |
| Java 25 compatibility scaffolding | `D701C88891103D6F719873260C42BCCB348124F73678EB7EFD2C95213E5AE51F` |
| Java 25 target launcher | `667C7A515E0AC4B8B6C29402D0C48D8598FF3FFC167D3A0E1764B4433411A6C6` |

The repository manifest `evidence/fingerprints.sha256` is instead rebuilt
from the exact staged Git blobs obtained with:

`git cat-file blob :<path>`

This gives a stable fingerprint of the representation that is actually
committed to the repository.

Before accepting staged content, the working-tree and staged text are also
compared after line-ending normalization. Any difference beyond line
endings causes the validation to fail.

---

### 14. Measurement freeze

The following state is considered frozen before measurement:

- raw generated suite: 43 tests;
- native-valid suite: 40 tests;
- final target-portable suite: 37 tests;
- target repeatability: 5/5 runs;
- final result: 37/37 passes;
- standard Maven test: BUILD SUCCESS.

From this point onward JaCoCo and PIT are measurement activities only.

No generated EvoSuite test body, input or assertion may be changed based
on coverage or mutation results.

<!-- T_ES_TROUBLESHOOTING_END -->

<!-- T_ES_GENERATED_WHITESPACE_START -->

### 15. Trailing whitespace in EvoSuite-generated sources

**Problem**

The final pre-commit validation using:

`git diff --cached --check`

reported many `trailing whitespace` diagnostics in the generated EvoSuite
test and scaffolding sources.

**Cause**

The whitespace is already present in the sources emitted by EvoSuite.
It is therefore part of the generated artifacts that are being preserved
for reproducibility.

The diagnostics do not represent a compilation or test failure.

Removing the whitespace would alter the generated files after their
generation/validation and would invalidate the source-origin SHA-256
fingerprints.

**Solution**

The generated files are deliberately preserved without formatting or
whitespace cleanup.

The repository validation distinguishes between:

- generated/frozen artifacts, whose integrity is checked using
  fingerprints and content equivalence;
- project-authored files, for which Git whitespace validation remains
  enabled.

Therefore `git diff --cached --check` is not used as a global gate across
the generated EvoSuite files. Instead the whitespace gate is applied only
to the files authored or maintained as part of the ISW2 testing harness.

This preserves both reproducibility and normal quality checks for the
hand-maintained project files.

<!-- T_ES_GENERATED_WHITESPACE_END -->

<!-- T_ES_JACOCO_MEASUREMENT_START -->

## JaCoCo measurement

### Measurement protocol

Coverage was measured only after `T_ES_TARGET` had been frozen.

Measurement environment:

- target: Apache Storm 3.0.0;
- Java: 25;
- EvoSuite: 1.2.0;
- JaCoCo: 0.8.15;
- frozen suite: `T_ES_TARGET`;
- tests executed: 37.

JaCoCo was attached directly to the dedicated Java 25 compatibility
launcher using the Java agent.

Instrumentation was restricted to:

`org.apache.storm.daemon.ui.UIHelpers`

The coverage run therefore used the same 37-test execution path that had
already passed the target-portability and repeatability gates.

The instrumented run produced:

- 37 executed;
- 37 passed;
- 0 failed;
- 0 ignored.

No test body, assertion, input or launcher filter was changed after seeing
the coverage result.

### Coverage result

For `UIHelpers`:

| Metric | Covered | Total | Coverage |
| --- | ---: | ---: | ---: |
| Lines | 256 | 1122 | 22.8164% |
| Branches | 51 | 242 | 21.0744% |
| Methods | 53 | 122 | 43.4426% |
| Instructions | 1308 | 5802 | 22.5440% |

The line and branch denominators were explicitly compared with the
previous BB, CF and RND measurements.

All suites use:

- 1122 executable lines;
- 242 branches.

The measurements are therefore directly comparable.

### Cross-suite coverage comparison

| Suite | Tests | Line coverage | Branch coverage |
| --- | ---: | ---: | ---: |
| T_BB | 35 | 39.1266% | 35.1240% |
| T_CF | 45 | 43.8503% | 50.4132% |
| T_RND | 35 | 10.2496% | 12.3967% |
| T_ES_TARGET | 37 | 22.8164% | 21.0744% |

`T_ES_TARGET` therefore obtains substantially higher structural coverage
than the random Randoop suite, while remaining below both the black-box
and control-flow suites.

This observation is recorded only as an experimental result and was not
used to modify the EvoSuite suite.

### JaCoCo CLI report argument splitting

**Problem**

The first coverage execution itself succeeded:

- 37/37 tests passed;
- `jacoco.exec` was produced;
- `UIHelpers` execution data was present.

However, the first subsequent JaCoCo CLI `report` command failed.

The error showed JaCoCo attempting to load an additional execution-data
file named:

`UIHelpers`

**Cause**

The initial report command used PowerShell `Start-Process` with an
`ArgumentList` containing the report name:

`T_ES UIHelpers Coverage`

The value containing spaces was split incorrectly and part of the report
name was interpreted by JaCoCo as an additional execution-data argument.

This was a report-generation problem only. The instrumented 37-test run
had already completed successfully.

**Solution**

The tests were not rerun.

The already produced `jacoco.exec` file was preserved and verified with
JaCoCo `execinfo`.

It contained execution data for exactly the target class:

`org/apache/storm/daemon/ui/UIHelpers`

The report was then generated using the PowerShell call operator instead
of `Start-Process`, and a report name without spaces:

`T_ES_UIHelpers_Coverage`

The recovered report produced CSV, XML and HTML output successfully.

The SHA-256 of `jacoco.exec` was checked before and after report
generation and remained identical, proving that the recovery reused the
same original instrumented execution.

### Permanent coverage artifacts

The validated coverage artifacts are stored in:

`results/uihelpers/automatic/es/coverage/`

Files:

- `jacoco.csv`
- `jacoco.xml`
- `tes-uihelpers-coverage-summary.csv`
- `tes-coverage-comparison.csv`

<!-- T_ES_JACOCO_MEASUREMENT_END -->


## Mutation testing (T_ES)

The frozen 37-test EvoSuite target suite was measured with PIT 1.25.8 using
the `DEFAULTS` mutator set and the same 338-mutant UIHelpers population used
for the other mutation experiments.

Results:

- generated mutants: 338;
- killed: 24;
- survived: 80;
- no coverage: 234;
- Mutation Score: 7.10%;
- Test Strength: 23.08%;
- no-coverage proportion: 69.23%.

The mutation population matches T_CF, T_MT, and T_RND exactly.

PIT executes T_ES through the validated JUnit 4 Java 25 compatibility adapter.
The adapter exposes one external test to PIT and internally executes the 37
frozen EvoSuite methods. Consequently, mutation adequacy is valid at suite
level, while PIT cannot provide reliable per-generated-test kill attribution.

The primary structural metric remains the JaCoCo measurement
(22.8164% line, 21.0744% branch). PIT's own line-coverage observation is kept
separate because its instrumentation differs from JaCoCo.

Full mutation evidence is stored under:

`isw2/testing/results/uihelpers/automatic/es/mutation/`

# UIHelpers T_LLM - Initial Microsoft 365 Copilot execution

Recorded:

`2026-08-23T18:01:18+02:00`

Repository HEAD before generated sources were committed:

`563534c81c69c8f73060fbc40e5e9c734401ff50`

## Generation

Provider:

`Microsoft 365 Copilot`

Access mode:

`WEB`

Model/mode label recorded by the experiment:

`M365 Copilot, basato sul modello di ragionamento GPT-5`

Effective P1 prompt SHA-256:

`A9C13ACC28667811CEF6000C6125C8AD57C0CA226C3027E01B5F41DAB37AEEEE`

## Generated suite

Raw source:

`isw2/testing/llm/uihelpers/raw/UIHelpersTest.java`

Executable source:

`isw2/testing/src/test/java/org/apache/storm/daemon/ui/UIHelpersTest.java`

Raw SHA-256:

`8099DE656838FD97BB09175F4377D079B7BB06112917380DDB8B64D264278881`

Executable SHA-256:

`8099DE656838FD97BB09175F4377D079B7BB06112917380DDB8B64D264278881`

Raw/executable identity:

`YES`

Generated ordinary `@Test` methods:

`35`

Parameterized tests:

`0`

Mockito references:

`0`

Sleep references:

`0`

## Initial execution

Java:

`Microsoft OpenJDK 25.0.4`

Maven:

`3.9.16`

Executed suite:

`org.apache.storm.daemon.ui.UIHelpersTest`

Tests:

`35`

Failures:

`0`

Errors:

`0`

Skipped:

`0`

Result:

`PASS`

## Adequacy isolation

At this stage:

- JaCoCo was not executed on T_LLM;
- PIT was not executed on T_LLM;
- no coverage feedback was supplied to Microsoft 365 Copilot;
- no mutation feedback was supplied to Microsoft 365 Copilot;
- no manual repair of the generated Java source was performed.

The exception stack traces visible in the Maven log were production logging
side effects produced by `exceptionToJson` scenarios and were not Surefire
test failures.

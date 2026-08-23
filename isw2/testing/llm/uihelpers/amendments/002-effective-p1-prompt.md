# Amendment 002 - Effective P1 prompt

## Context

An earlier P1 prompt had been committed before the first LLM interaction.

That earlier version separated functional analysis from test generation.

Before sending the first prompt to Microsoft 365 Copilot, the prompt strategy
was simplified after methodological review.

The prompt actually submitted to Microsoft 365 Copilot combined:

1. functional analysis of `UIHelpers`;
2. generation of exactly 35 JUnit Jupiter tests.

The effective prompt is preserved verbatim at:

`isw2/testing/llm/uihelpers/prompts/P1-effective-analysis-and-generation.txt`

## Important temporal note

This amendment is being recorded after the first Microsoft 365 Copilot
interaction.

Therefore it is not presented as a preregistered decision.

Its purpose is to preserve an accurate record of the prompt that was
actually used rather than silently rewriting the previously committed P1.

## Effective P1 outcome

Microsoft 365 Copilot produced:

- an analysis of `UIHelpers`;
- exactly 35 ordinary JUnit Jupiter tests;
- no parameterized tests;
- no Mockito usage;
- no real network usage;
- no Jetty server startup;
- no sleep-based synchronization.

The generated Java source was executed without manual repair.

Initial execution result:

`35 tests, 0 failures, 0 errors, 0 skipped`

No JaCoCo or PIT feedback had been supplied to Microsoft 365 Copilot before
or during this generation.

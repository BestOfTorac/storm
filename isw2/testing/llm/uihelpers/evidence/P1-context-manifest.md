# UIHelpers T_LLM - P1 context manifest

This manifest records the exact initial context intended for the first
Microsoft 365 Copilot interaction of the T_LLM experiment.

## Experiment state

Repository commit:

`288f7196a0e0bb670829a33263a7d9fb95d593af`

Timestamp:

`2026-08-23T17:30:52+02:00`

Provider:

`Microsoft 365 Copilot`

Access mode:

`WEB`

Model/mode label recorded before P1:

`M365 Copilot, basato sul modello di ragionamento GPT-5`

At manifest creation time:

- P1 has not yet been sent;
- no T_LLM tests have been generated;
- no T_LLM adequacy measurement has been performed.

## Initial files supplied to P1

### 1. Target production class

Repository path:

`storm-webapp/src/main/java/org/apache/storm/daemon/ui/UIHelpers.java`

Working-tree SHA-256:

`EB8AD591B36143E738297F100B81AA04E6D3B808BF4BD72C198BDDB7B5E85EDB`

Git blob at the frozen repository commit:

`2e4f64f2f1b7f19166b77cf9747ce00c31f91a97`

Role:

Primary production source under analysis.

### 2. Testing-module configuration

Repository path:

`isw2/testing/pom.xml`

Working-tree SHA-256:

`FE41AF4BF4E8D3841A78A897A4E3B21D79091D78C55AE7A263AF64068A3DE095`

Git blob at the frozen repository commit:

`c067a8e7ea3b9e61d393cd0fccacfdfb82fb2796`

Role:

Provides the technical Java/testing environment only.

## Context rule

The initial P1 interaction is intentionally restricted to the two files
listed above.

Microsoft 365 Copilot must not be assumed to have access to the local
repository, GitHub repository, IntelliJ project, existing test suites, or
experimental results.

If P1 identifies missing production context, the requested production file
will be reviewed separately before being supplied.

No existing test source or adequacy result may be added to the P1 context.

## Integrity references

Original preregistration SHA-256:

`4DBEABC63972F2824CB47881C3E63A4877D3F34DA5A09F939B941AB44DB262BA`

Provider amendment SHA-256:

`47F6C9BC779A4DCEDE497F36E842B97368F9BC276729DC524B3793D7F737EBB9`

Microsoft 365 Copilot environment SHA-256:

`9A8DD432EC8B8528F11E06CCC0F2A9FB8D2AC54A572565CA0975C13FC2FA5127`

Exact P1 prompt:

`isw2/testing/llm/uihelpers/prompts/P1-functional-analysis.txt`

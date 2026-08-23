# UIHelpers T_LLM - P3 mutation-guided context

P3 prompt status:

`FROZEN BEFORE SENDING`

Repository baseline:

`52c2ccd87b827adf43e9897e0dba818a69e778c9`

## Input suite

Suite:

`T_LLM P2`

Tests:

`35`

P2 SHA-256:

`4767540BADE73F4F391872AE8218F2005001166495B8FEA1A4E315431453CE94`

P2 baseline result:

`35/35 PASS`

## Structural adequacy known before P3

Line Coverage:

`80 / 1122 = 7.1301%`

Branch Coverage:

`37 / 242 = 15.2893%`

These coverage values are NOT included in the P3 prompt.

## Mutation baseline known before P3

Generated:

`338`

Killed:

`49`

Survived:

`1`

No Coverage:

`288`

Mutation Score:

`14.50%`

Test Strength:

`98.00%`

## Survivor supplied to the LLM

Class:

`org.apache.storm.daemon.ui.UIHelpers`

Method:

`sanitizeStreamName`

Line:

`1773`

Mutator:

`ConditionalsBoundaryMutator`

Description:

`changed conditional boundary`

No human-derived diagnosis of the survivor is included in the P3 prompt.

In particular, the prompt does not tell the LLM which concrete input is
expected to distinguish the original implementation from the mutant.

The purpose of P3 is to evaluate whether the LLM can independently diagnose
the surviving boundary mutation and produce a minimal mutation-guided
evolution while preserving exactly 35 tests.

P3 prompt SHA-256:

`F2680E4D19832EA88D3A4533488DA63EDED9D99A09AEC1E1B0FF981FCE0614CE`

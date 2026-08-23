# UIHelpers T_LLM - P3 initial execution

Recorded:

`2026-08-23T18:47:01+02:00`

Repository baseline before P3 suite freeze:

`9ee6c69c734cff7fe799d82ae16bdcf69a432bc3`

## Prompt

Prompt type:

`mutation-guided refinement`

P3 prompt SHA-256:

`F2680E4D19832EA88D3A4533488DA63EDED9D99A09AEC1E1B0FF981FCE0614CE`

P3 context manifest SHA-256:

`6911DF8C268DC71BA02283C8172350DCE39475E71E98ADEC4B3016D8031874B2`

The P3 prompt was frozen before being sent to Microsoft 365 Copilot.

The mutation survivor identity was supplied to the LLM, while the concrete
human-derived diagnosis of the distinguishing input was not supplied.

## Input baseline

P2 SHA-256:

`4767540BADE73F4F391872AE8218F2005001166495B8FEA1A4E315431453CE94`

P2 mutation baseline:

- generated: 338
- killed: 49
- survived: 1
- no coverage: 288
- mutation score: 14.50%
- test strength: 98.00%

The single surviving mutant affected `sanitizeStreamName` at line 1773 using
`ConditionalsBoundaryMutator`.

## Copilot P3 change

Copilot independently diagnosed the boundary involving the empty stream name.

It proposed replacing the existing test 35 with:

`sanitizeStreamNamePrefixesEmptyName`

Input:

`""`

Expected observable result:

`"_s"`

The total number of tests remained exactly 35.

The executable P3 suite was derived mechanically from frozen P2 by applying
only this declared test-35 replacement. This avoided markdown-rendering
escaping artifacts in the Copilot response.

No human-authored test behavior was introduced.

## Suite identity

P2 SHA-256:

`4767540BADE73F4F391872AE8218F2005001166495B8FEA1A4E315431453CE94`

P3 SHA-256:

`B23C679EE02E72F4F0BFAE3B050A666B4603D3D85EA76513842EA58C838A4F3B`

Executable SHA-256:

`B23C679EE02E72F4F0BFAE3B050A666B4603D3D85EA76513842EA58C838A4F3B`

P2 remained unchanged.

P3 and the executable source were byte-identical.

## Structural audit

- ordinary `@Test`: 35
- parameterized tests: 0
- old test 35 present: 0
- new test 35 present: 1
- Mockito usage: 0
- sleep usage: 0

Only test 35 differs between P2 and P3.

## Initial execution

Environment:

- Java: Microsoft OpenJDK 25.0.4
- Maven: 3.9.16
- test framework: JUnit Jupiter

Result:

- tests: 35
- failures: 0
- errors: 0
- skipped: 0

Therefore P3 passed the initial validity gate.

## Adequacy status

At the time P3 was frozen:

- P3 JaCoCo measurement: NOT RUN
- P3 PIT measurement: NOT RUN

No conclusion about adequacy improvement is made from the successful
execution alone.

The next stage is to re-measure structural coverage and mutation adequacy
against the same production target and mutation population used for P2.

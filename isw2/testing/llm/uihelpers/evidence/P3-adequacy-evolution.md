# UIHelpers T_LLM - P3 adequacy evolution and final LLM decision

Recorded:

`2026-08-23T19:00:43+02:00`

Repository commit at measurement time:

`efdd0fd6dc8ec86a1a97167a8e3cadfc95170321`

## Scope

P3 is the mutation-guided evolution of the frozen P2 LLM suite.

Both P2 and P3 contain exactly 35 ordinary JUnit Jupiter tests.

P2 SHA-256:

`4767540BADE73F4F391872AE8218F2005001166495B8FEA1A4E315431453CE94`

P3 SHA-256:

`B23C679EE02E72F4F0BFAE3B050A666B4603D3D85EA76513842EA58C838A4F3B`

The executable test source was byte-identical to frozen P3 during both
JaCoCo and PIT measurement.

No human-authored test behavior was added.

## P2 baseline

Structural adequacy:

- Line Coverage: 80 / 1122 = 7.1301%
- Branch Coverage: 37 / 242 = 15.2893%

Mutation adequacy:

- generated: 338
- killed: 49
- survived: 1
- no coverage: 288
- covered mutants: 50
- Mutation Score: 14.50%
- Test Strength: 98.00%

## P3 structural adequacy

JaCoCo 0.8.15:

- Line Coverage: 80 / 1122 = 7.1301%
- Branch Coverage: 36 / 242 = 14.8760%
- Method Coverage: 17 / 122 = 13.9344%
- Instruction Coverage: 467 / 5802 = 8.0489%

P2 -> P3 delta:

- Line Coverage: 0 lines, 0.0000 percentage points
- Branch Coverage: -1 branch, -0.4132 percentage points
- Instruction Coverage: -4 instructions

Therefore the mutation-guided replacement did not improve structural
coverage.

## P3 mutation adequacy

PIT:

- version: 1.25.8
- mutators: DEFAULTS
- threads: 1
- generated mutants: 338
- population difference against P2: 0

Results:

- killed: 48
- survived: 1
- no coverage: 289
- covered mutants: 49
- Mutation Score: 14.20%
- Test Strength: 97.96%

P2 -> P3 delta:

- killed: -1
- survived: 0
- no coverage: +1
- covered mutants: -1
- Mutation Score: -0.30 percentage points
- Test Strength: -0.04 percentage points

No abnormal PIT statuses were observed.

## Mutation transition audit

Exactly five mutants changed status while the mutation population remained
identical.

Transitions:

1. SURVIVED -> KILLED
   - method: sanitizeStreamName
   - line: 1773
   - mutator: ConditionalsBoundaryMutator
   - description: changed conditional boundary

2. KILLED -> NO_COVERAGE
   - method: sanitizeStreamName
   - line: 1773
   - mutator: RemoveConditionalMutator_EQUAL_ELSE

3. KILLED -> SURVIVED
   - method: sanitizeStreamName
   - line: 1773
   - mutator: RemoveConditionalMutator_ORDER_ELSE

4. KILLED -> NO_COVERAGE
   - method: sanitizeStreamName
   - line: 1774
   - mutator: EmptyObjectReturnValsMutator

5. NO_COVERAGE -> KILLED
   - method: sanitizeStreamName
   - line: 1776
   - mutator: EmptyObjectReturnValsMutator

The specific boundary mutant exposed by P2 was successfully killed by P3.

However, replacing the previous sanitizeStreamName scenario caused other
mutation-detection capabilities to be lost. One previously killed mutant
became a survivor and two previously killed mutants became no-coverage.

The mutation-guided refinement therefore solved the targeted weakness but did
not produce a global adequacy improvement.

## Interpretation

The experiment shows that mutation-guided LLM refinement is not necessarily
monotonic.

A prompt can successfully diagnose and address one specific surviving mutant
while simultaneously reducing coverage or fault-detection capability for
other behaviors when an existing test is replaced.

This result is retained rather than further tuning the suite to the PIT
population.

## Final LLM protocol decision

No P4 prompt will be executed for UIHelpers.

The LLM experiment stops after:

- P1: analysis and initial generation;
- P2: critical self-review/refinement;
- P3: mutation-guided refinement.

P2 remains the official `T_LLM` baseline for cross-technique comparison,
because it was frozen before adequacy feedback and represents the final
general-purpose LLM-generated suite.

P3 is retained separately as the mutation-guided evolution and as evidence of
the suite reaction to mutation feedback.

No further prompt is used to optimize the suite against the observed PIT
population.

## Production target identity

UIHelpers.class SHA-256:

`08C2E2460DBFF35E909285DCC11923A81AD2EF09A04CCB441DB2AD289927F79E`

## Raw artifact fingerprints

P3 JaCoCo CSV SHA-256:

`38D8A36C0131F6E2EF24DF06FA337F01DCF1348B2DEF987C1D4A9A2647D0ED13`

P3 JaCoCo XML SHA-256:

`36078243032EB6F3D7ACD2A359D898D3F4F4938A71F824A87DD57A7434EADF34`

P3 JaCoCo EXEC SHA-256:

`8CDE97A3831695E46E1E90550AC9CF979A53F37F5CEA074C734E479E6E9691B2`

P3 PIT mutations.xml SHA-256:

`C446AADECC7B354B55C4E20D2AEEB3D8E53A52B5E20FD35A189344483E24B0EF`

P3 PIT mutations.csv SHA-256:

`D774F462C27AE8FFAE10D4345B6BE4F0470A1BB6C3B7DE63E0FDF18BF7F09286`

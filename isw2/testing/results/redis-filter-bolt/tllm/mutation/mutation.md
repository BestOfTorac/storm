# RedisFilterBolt T_LLM - Mutation testing

## Status

VALID BASELINE RESULT

## Timestamp

2026-08-24T14:04:08+02:00

## Target

org.apache.storm.redis.bolt.RedisFilterBolt

## Suite

T_LLM_BASELINE

## Cardinality

11 tests

## Baseline identity

Freeze commit:

1be792bbef02651d1ff033f3f1e2aa8e949cd021

Coverage commit:

8e9470b63167c8ea67b62f60608355d957566acf

Test SHA-256:

B247D548F81DAA437A281A22F865C56810C4B0575368267F520FC26D64D17358

The LLM baseline remained unchanged throughout mutation testing.

## Tooling

PIT:

1.25.8

Mutators:

DEFAULTS

JUnit Platform Launcher:

6.1.1

Mockito Java agent:

5.23.0

## Execution methodology

The Maven PIT plugin attempt was invalid because the experimental testing module contains tests but no module-local production source, therefore PIT skipped the project with:

Project has either no tests or no production code.

A first direct PIT CLI execution correctly discovered the T_LLM test class but the PIT coverage minion exited during coverage generation.

The final valid execution used direct PIT CLI with:

- explicit mutableCodePaths containing the exact RedisFilterBolt bytecode from storm-redis 3.0.0;
- explicit project test classpath;
- pitest-junit5-plugin on the PIT tool classpath;
- JUnit Platform Launcher 6.1.1;
- Mockito 5.23.0 passed explicitly as a Java agent to the PIT minion;
- one execution thread;
- DEFAULTS mutators.

No test or production source was modified to obtain a successful PIT run.

## Population comparability

Reference population:

12 mutants

T_LLM population:

12 mutants

Identity comparison used:

- mutated class;
- mutated method;
- line number;
- mutator;
- mutation description;
- multiplicity.

Population identity:

PASS

## Results

Total mutants:

12

Killed:

9

Survived:

3

No coverage:

0

Timed out:

0

Run errors:

0

Memory errors:

0

Reached:

12

Mutation score:

75 %

Test strength:

75 %

## Surviving mutants

### Survivor 1

Method:

process

Line:

108

Mutator:

org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_ELSE

Description:

removed conditional - replaced equality check with false

### Survivor 2

Method:

process

Line:

112

Mutator:

org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator

Description:

changed conditional boundary

### Survivor 3

Method:

process

Line:

117

Mutator:

org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_ELSE

Description:

removed conditional - replaced equality check with false

## Interpretation boundary

These results describe the independently generated frozen T_LLM_BASELINE.

The baseline must not be modified using mutation feedback.

Any mutation-guided LLM refinement must be stored and evaluated as a separate derived suite.

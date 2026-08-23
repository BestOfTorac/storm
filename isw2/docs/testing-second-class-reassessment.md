# Software Testing - Second Class Suitability Reassessment

## Purpose

The Software Testing experiment requires two production classes.

The original class-selection ranking was retained. The reassessment described
here does not replace the ranking with an arbitrary manual choice.

Instead, candidate classes near the originally selected lower end of the same
ranking are evaluated sequentially for experimental suitability.

The purpose is to avoid a class whose behavior is so simple that the manual
black-box baseline already saturates the selected structural adequacy metrics,
making subsequent test-suite evolution uninformative.

No native Apache Storm tests are used in this reassessment.

---

# Candidate 1 - original last-2 selection

Selection:

`last-2`

Eligible rank:

`379 / 381`

Class:

`org.apache.storm.security.auth.DefaultHttpCredentialsPlugin`

Falessi inventory:

- TypeLOC: 62
- DeclaredMethods: 3
- NSMELLS: 0

## Initial assessment

The class initially appeared suitable because it contains several observable
authentication and impersonation decisions.

A Category Partition was defined before structural adequacy measurement.

The resulting manual black-box baseline contained exactly ten tests.

All ten tests passed.

## Structural adequacy result

JaCoCo 0.8.15 on the exact Apache Storm 3.0.0 production class:

- Line Coverage: 30 / 30 = 100.0000%
- Branch Coverage: 16 / 18 = 88.8889%
- Method Coverage: 5 / 5 = 100.0000%
- Instruction Coverage: 104 / 104 = 100.0000%

## Decision

`REJECTED_FOR_EXPERIMENTAL_TRIVIALITY`

The tests themselves were not rejected and did not fail.

The candidate was rejected because one of the two selected structural
adequacy metrics, Line Coverage, was already mathematically saturated by the
initial black-box suite before any control-flow-guided evolution.

Removing valid black-box tests after observing coverage merely to manufacture
a coverage gap was deliberately avoided.

The frozen black-box source is preserved as experimental evidence outside the
active Maven test source root.

No T_CF, mutation-guided evolution, random testing, EvoSuite or LLM cycle is
continued for this rejected candidate.

---

# Candidate 2 - next ranked class

Selection:

`last-3`

Eligible rank:

`378 / 381`

Class:

`org.apache.storm.scheduler.SupervisorResources`

Falessi inventory:

- TypeLOC: 78
- DeclaredMethods: 10
- NSMELLS: 0

Production source SHA-256:

`F0C7FF627B4ED9F70EF3E0E870E9AF000B24451445236F8DCFEA9B5486C1E57D`

## Pre-flight result

The candidate was evaluated before creating a Category Partition or tests.

Observed control-flow signals:

- if: 0
- switch: 0
- for: 0
- while: 0
- catch: 0
- ternary expressions: 2

The two ternary expressions occur in the constructor and only normalize null
generic-resource maps into empty maps.

Most of the remaining public API consists of:

- field getters;
- arithmetic for available CPU/memory;
- defensive map copies;
- arithmetic/resource accumulation.

## Decision

`REJECTED_AT_PREFLIGHT_FOR_TRIVIALITY_RISK`

Unlike the previous candidate, no test suite and no adequacy measurement were
created.

The rejection occurs before test design because the production structure
already indicates a very high risk that a small functional suite would
saturate structural coverage immediately.

This avoids repeating the DefaultHttpCredentialsPlugin experiment solely to
obtain another trivial baseline.

---

# Sequential replacement rule

The original ranking policy remains unchanged:

- structural filter unchanged;
- rank metric: NSMELLS;
- rank order: descending;
- tie break: FQCN ascending.

The lower-end candidate is advanced sequentially only when a candidate is
rejected for documented experimental triviality.

Therefore:

- `last-2` was evaluated and rejected empirically;
- `last-3` was evaluated and rejected at pre-flight;
- the next candidate must come from the next position in the same frozen
  ranking rather than from an arbitrary manually preferred class.

The official selection files are not updated until a replacement candidate
has passed the non-triviality pre-flight.

This keeps the class-selection process reproducible while also respecting the
requirement that the testing experiment should remain meaningful.

---

# Preservation of rejected evidence

The DefaultHttpCredentialsPlugin artifacts are intentionally retained.

They document:

- the Category Partition;
- the ten-test T_BB;
- the successful 10/10 execution;
- the JaCoCo result that triggered reassessment.

The Java test source is archived outside `src/test/java` so it does not become
part of the final active test population.

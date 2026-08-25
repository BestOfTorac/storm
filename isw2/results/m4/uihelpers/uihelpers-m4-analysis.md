# M4 - UIHelpers final analysis

Target: org.apache.storm.daemon.ui.UIHelpers",
        ",


- LOC: 1658
- NSMELLS: 77

The Dataset A correlation audit identified LOC and NSMELLS as positively correlated with BUGGY among the static features applicable to a generated source variant.

The negatively correlated features are historical/process measures (age, change-set, directory dispersion and entropy metrics). They are kept invariant when C0 is replaced by an artificial refactored variant, because the refactoring does not redefine the previous repository history of the class.

## Required M4 analysis

| Variant | Compiles? | Smells | Positive feature higher than C0? | Negative feature higher than C0? |
| --- | --- | --- | --- | --- |
| C1 | YES | 79; no new rule types; S5411 increases 2->4 | YES: LOC 1658->1686 and NSMELLS 77->79 | NO |
| C2 | YES | 12; no new rule types; 10 C0 rule types removed | YES: LOC 1658->1692 | NO |
| C3 | YES | 15; no new rule types; 7 rule types removed; S1192 38->1 | YES: LOC 1658->1689 | NO |
| C4 | YES | 15; no new rule types; 7 C0 rule types removed | YES: LOC 1658->1685 | NO |

## Smell interpretation

C1 introduces no new Sonar rule type, but increases the number of instances of the existing rule S5411 from 2 to 4.

C2, C3 and C4 introduce no new Sonar rule type. Their remaining smells belong only to rule types already present in C0.

## Positive-correlation interpretation

LOC is positively correlated with BUGGY in Dataset A (r = 0.278807) and is higher than C0 in every generated variant.

NSMELLS is also positively correlated with BUGGY (r = 0.168371). It increases only in C1 and decreases substantially in C2, C3 and C4.

Therefore the answer to the question whether any positively correlated feature is higher than C0 is YES for C1, C2, C3 and C4.

## Negative-correlation interpretation

The negatively correlated predictors identified in Dataset A are historical/process features rather than properties recomputed from the standalone generated Java source.

They remain unchanged in the C0-to-Cx replacement scenario. Therefore none is higher than C0 for C1, C2, C3 or C4.

# Testing Class Selection

## Reference release

The two classes used for the Software Testing experiment and for the
subsequent automated-refactoring experiment are selected from Apache Storm
3.0.0 (`v3.0.0`).

The production Java sources of the current project branch were verified to be
identical to the `v3.0.0` production sources before class selection.

## Initial population

The structural inventory contains 634 Java production source files:

- 538 classes;
- 89 interfaces;
- 6 enums;
- 1 annotation type.

Only primary Java types whose kind is `CLASS` participate in the class
selection.

## Structural filtering

The course specification requires filtering classes that are too small, for
example classes containing only a few simple methods, but it does not define a
numeric threshold.

Therefore, the threshold was established from the structural distribution of
the Storm 3.0.0 classes before inspecting their SonarCloud NSMELLS values.

Among concrete classes:

- first quartile TypeLOC: 32;
- median TypeLOC: 59;
- first quartile declared methods: 2;
- median declared methods: 4.

A class is therefore structurally eligible when:

- `TypeKind = CLASS`;
- `TypeLOC >= 32`;
- `DeclaredMethods >= 2`.

Constructors are deliberately not counted toward the method threshold.

The rule retains 381 of the 538 classes (70.82%) and excludes 157 classes as
structurally too small.

Abstract classes and classes belonging to external, Flux, examples and
integration-test modules are not excluded by additional ad-hoc rules because
the class-selection specification refers to all classes of the last release.

## Student-dependent selection

For the selector letter `V`:

`V = 22`

and:

`22 mod 5 = 2`

Therefore selection case 2 applies:

`Classes first +2 and last -2`

The two final classes will only be identified after SonarCloud NSMELLS values
have been collected and the complete ranking has been produced.

## Reproducibility

Structural inventory:

`isw2/datasets/testing/class_inventory_v3.0.0.csv`

Machine-readable selection policy:

`isw2/config/testing-class-selection.properties`
## Final selected classes

The eligible-class ranking contains 381 classes.

The ranking convention adopted by the project is:

- primary metric: `NSMELLS`;
- order: descending;
- deterministic tie-break: fully-qualified class name ascending.

For selector letter `V`:

`22 mod 5 = 2`

therefore case 2 is applied:

`Classes first +2 and last -2`

Using one-based positions, this corresponds to eligible ranks 3 and 379.

### C_0 A

`org.apache.storm.daemon.ui.UIHelpers`

- module: `storm-webapp`
- TypeLOC: 2389
- declared methods: 108
- NSMELLS: 77
- eligible rank: 3

### C_0 B

`org.apache.storm.security.auth.DefaultHttpCredentialsPlugin`

- module: `storm-server`
- TypeLOC: 62
- declared methods: 3
- NSMELLS: 0
- eligible rank: 379

The lower-ranked class belongs to a tie containing multiple classes with
`NSMELLS=0`. The course specification does not define a tie-breaking rule.
The project therefore uses the previously recorded deterministic
`FQCN_ASC` tie-break and does not alter the selection after observing the
result.

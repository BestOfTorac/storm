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
## Original ranking result

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

### Original C_0 B candidate

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
## Testing-suitability reassessment of the second class

The original Falessi-derived selection remains unchanged as a historical and
reproducible result:

`first +2` and `last -2`.

After test design began, the lower-end class was subjected to the additional
testing-suitability requirement of the Software Testing experiment.

The original `last-2` candidate,
`org.apache.storm.security.auth.DefaultHttpCredentialsPlugin`, was not removed
because its tests failed. Its independently designed ten-test black-box suite
passed 10/10. However, the first JaCoCo measurement performed only after the
suite had been frozen produced:

- Line Coverage: 30/30 = 100.0000%;
- Branch Coverage: 16/18 = 88.8889%.

Line Coverage was therefore already mathematically saturated before the
control-flow-guided test evolution.

Valid black-box tests were deliberately not removed after seeing the metric
merely to manufacture an artificial coverage gap.

The project consequently evaluated lower-end candidates sequentially in the
same frozen NSMELLS/FQCN ranking. The original ranking metric, structural
filter and deterministic tie-break were never changed.

The complete audit and rejection rationale are recorded in:

`isw2/docs/testing-second-class-reassessment.md`

The sequential search eventually reached `last-19`, eligible rank 362:

`org.apache.storm.redis.bolt.RedisFilterBolt`

This class was accepted because it provides a non-trivial but testable
functional surface:

- TypeLOC: 107;
- declared methods: 2, excluding constructors according to the original
  structural metric;
- four public executable entry points including the two constructors;
- multiple observable Redis filtering behaviours;
- control flow involving data-type dispatch, boundaries, success/failure and
  exception handling;
- no direct static calls in the target class;
- Redis access can be represented through the `JedisCommandsContainer`
  interface without requiring a real Redis service for the manually designed
  unit tests.

The fact that the class belongs to the `storm-redis` external module does not
introduce a new selection exception: the frozen class-selection policy already
contained `includeExternalModules=true`.

### Effective C_0 B

`org.apache.storm.redis.bolt.RedisFilterBolt`

- module: `external/storm-redis`;
- source:
  `external/storm-redis/src/main/java/org/apache/storm/redis/bolt/RedisFilterBolt.java`;
- TypeLOC: 107;
- declared methods: 2;
- NSMELLS: 0;
- eligible rank: 362;
- effective lower-end position: `last-19`.

This is an experimental-suitability replacement for the original `last-2`
slot. It does not redefine selection case 2 as `last-19`.

The machine-readable policy therefore retains:

`lastOffset=2`

and records the replacement separately with:

`secondClassEffectiveOffset=19`

The final active Software Testing classes are thus:

1. `org.apache.storm.daemon.ui.UIHelpers`
2. `org.apache.storm.redis.bolt.RedisFilterBolt`

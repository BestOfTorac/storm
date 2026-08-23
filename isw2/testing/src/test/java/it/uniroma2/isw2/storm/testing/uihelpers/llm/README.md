# UIHelpers LLM tests

The UIHelpers LLM experiment is present and complete.

This directory intentionally does not contain a duplicate executable Java
class.

## Canonical artifacts

Independent cross-technique LLM baseline:

`isw2/testing/llm/uihelpers/refined/P2-UIHelpersTest.java`

Mutation-guided LLM evolution:

`isw2/testing/llm/uihelpers/refined/P3-UIHelpersTest.java`

Current executable source:

`isw2/testing/src/test/java/org/apache/storm/daemon/ui/UIHelpersTest.java`

## Why the executable is not copied here

The generated test declares:

`package org.apache.storm.daemon.ui;`

and its Java class is:

`org.apache.storm.daemon.ui.UIHelpersTest`

Copying the same `.java` source into this directory while retaining the
canonical executable would create two source files defining the same fully
qualified Java class and would cause a duplicate-class compilation failure.

Changing the package or class solely for directory symmetry would also alter
the frozen LLM suite after measurement.

Therefore the executable remains in its package-aligned source location and
this directory provides the link between the unified UIHelpers test tree and
the separately preserved LLM experiment artifacts.

## Frozen identities

P2 remains the official T_LLM baseline for cross-technique comparison.

P3 remains the mutation-guided evolution.

The experiment must not be regenerated or reformatted merely to make its file
location match the BB/CF/MT/RND directories.

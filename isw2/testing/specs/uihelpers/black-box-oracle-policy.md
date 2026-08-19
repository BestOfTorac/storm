# UIHelpers - Black-box oracle policy

## Purpose

This document defines how expected results are established for the initial
manual Category Partition suite T_BB.

## Source priority

Oracle evidence is considered in the following order:

1. official Apache Storm documentation;
2. public API documentation and Javadoc;
3. documented externally observable contracts of the libraries/services used;
4. functionality that can be unambiguously inferred from the public interface.

The implementation of UIHelpers is not inspected when documentation is
sufficient.

Only when the available documentation is insufficient for a test that is
necessary to characterize an externally observable functionality may the
implementation be inspected.

Such cases must be explicitly marked as:

INFERRED_FROM_CODE

and the reason why source inspection was necessary must be documented.

## Oracle classification

Each T_BB group receives one of the following statuses:

- DOCUMENTED_EXACT:
  documentation determines the exact expected result;

- DOCUMENTED_PROPERTY:
  documentation determines a property of the result but not its complete
  textual or structural representation;

- PUBLIC_CONTRACT:
  the expected behavior follows from a documented public API or external
  service contract;

- INTERFACE_INFERENCE:
  the behavior can be inferred from the public interface without inspecting
  the implementation;

- INFERRED_FROM_CODE:
  source inspection was strictly necessary because available specifications
  were insufficient;

- UNSPECIFIED:
  available information is insufficient to define a defensible oracle.

## Separation from later testing stages

T_BB is not designed using control-flow information or coverage results.

Control-flow information is introduced only when evolving the manual suite
from T_BB to T_CF.

Mutation-test results are introduced only in the later T_MT evolution.

Native Apache Storm tests are not used as specification, reference tests, or
oracle sources.

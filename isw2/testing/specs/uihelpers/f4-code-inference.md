# UIHelpers F4 - Required code inference

## Black-box evidence

The F4 Category Partition was defined before implementation inspection.

Storm documentation provides functional requirements and configuration
properties for:

- UI HTTP port and host;
- HTTPS configuration;
- keystore and truststore settings;
- request-header buffer sizing;
- HTTP-binding configuration;
- optional and required client authentication;
- servlet filters and filter parameters.

No sufficiently precise documentation was found for the exact CORS holder,
access-logging holder, dynamic-port behavior, configurator invocation, or all
connector and filter construction details.

Therefore limited C0 implementation inspection is required only to complete
the exact oracles of the already-defined 32 F4 logical cases.

Implementation inspection must not be used to add categories, control-flow
paths, branch-derived inputs, or coverage-driven cases to T_BB.

## Defect-candidate rule

Documented behavior has priority over implementation-derived behavior.

If C0 contradicts an externally documented Storm requirement or property,
the expected result must not be silently changed to match C0. The discrepancy
must instead be recorded and investigated as a defect candidate.

Implementation-derived expectations are characterization oracles only where
the external contract is genuinely unspecified.

This distinction must be preserved when interpreting passing and failing tests.

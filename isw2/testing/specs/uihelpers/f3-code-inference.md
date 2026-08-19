# UIHelpers F3 - Required code inference

## Black-box evidence

The F3 Category Partition was designed before implementation inspection.

The Storm UI REST API documentation establishes the following functional
properties:

- REST responses use JSON;
- JSONP is supported;
- a callback query parameter may wrap JSON in a callback function.

These properties support part of TBB-015, TBB-017, and TBB-018.

However, the available documentation does not specify:

- the exact unauthorized-user representation;
- callback validation syntax;
- whether dotted callback identifiers are accepted;
- invalid callback forms;
- the 128-character callback boundary;
- the exact response-header map;
- structured-object and String serialization details;
- the exact textual JSONP wrapper;
- the complete behavior of makeStandardResponse overloads.

The exact-name documentation search produced no matches for the F3 helper
methods. Searches for callback-validation rules also produced no relevant
contract for the 128/129 boundary or identifier syntax.

Therefore limited C0 implementation inspection is required to establish exact
oracles for TBB-014 through TBB-019.

Implementation inspection is used only to determine expected externally
observable results for the already-defined 28 F3 logical cases. It must not be
used to create new categories, paths, branches, coverage-driven cases, or test
requirements.

Where documentation already defines a functional property, that documentation
remains the primary evidence. Source inspection only completes the missing
details required by the exact oracle.

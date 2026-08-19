# UIHelpers F2 - Required code inference

## Reason

The F2 Category Partition was designed before implementation inspection.

The following black-box specification sources were checked:

- the compiled public API exposed by UIHelpers;
- repository documentation excluding production Java sources;
- repository tests and project-generated ISW2 material were excluded.

The complete F2 public API consists of:

- urlFormat(String, Object...);
- isSecureLogviewer(Map);
- getLogviewerPort(Map);
- getLogviewerLink(String, String, Map, int);
- getNimbusLogLink(String, Map);
- getSupervisorLogLink(String, Map);
- getWorkerLogLink(String, int, Map, String);
- getWorkerDumpLink(String, long, String, Map).

A documentation-only search over 980 eligible files produced zero matches
for all eight operations.

The public signatures alone are insufficient to establish the precise
externally observable results required by TBB-007 through TBB-013.

Therefore limited C0 implementation inspection is required.

Implementation inspection is used only to determine expected results for the
already-defined F2 Category Partition frames. It is not used to introduce new
categories, branches, paths, coverage-driven cases, or test requirements.

Any exact oracle obtained through this inspection is classified as
INFERRED_FROM_CODE.

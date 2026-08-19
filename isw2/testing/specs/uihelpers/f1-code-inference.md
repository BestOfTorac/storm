# UIHelpers F1 - Required code inference

## Reason

The initial T_BB design was produced without using implementation details.

Before source inspection, the following specification sources were checked:

- repository documentation excluding production and test Java sources;
- local Apache Storm 3.0.0 Javadoc artifacts;
- the compiled public API exposed by UIHelpers.

The repository documentation search covered 980 eligible documentation files
and produced no matches for the F1 public operations.

No Apache Storm 3.0.0 Javadoc JAR was available locally.

The public API confirms the existence and signatures of:

- prettyUptimeStr(String, Object[][]);
- prettyUptimeSec(String);
- prettyUptimeSec(int);
- prettyUptimeMs(String);
- prettyUptimeMs(int);
- prettyExecutorInfo(ExecutorInfo);
- getWindowHint(String);
- sanitizeStreamName(String).

These signatures are insufficient to determine the precise externally
observable results required by the already designed F1 Category Partition
frames.

Therefore limited implementation inspection is necessary to establish the
F1 C0 oracles.

The implementation is used only to determine expected results for already
defined black-box frames. It is not used to add categories, branches,
coverage-driven cases, or new test requirements.

All resulting exact F1 oracles are classified as INFERRED_FROM_CODE.

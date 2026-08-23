# Amendment 001 - LLM provider correction

## Scope

This amendment applies to the preregistered UIHelpers LLM test-generation
experiment described in:

`isw2/testing/llm/uihelpers/llm-test-generation-protocol.md`

Original preregistration commit:

`af64a769afb889e34221a636376ff9a2af6fe0d3`

Original preregistration SHA-256:

`4DBEABC63972F2824CB47881C3E63A4877D3F34DA5A09F939B941AB44DB262BA`

Amendment timestamp:

`2026-08-23T17:27:33+02:00`

---

## 1. Reason for amendment

The original preregistration incorrectly identified the intended LLM
interface/provider as GitHub Copilot used from IntelliJ IDEA.

Before the first experimental LLM prompt, the intended tool was clarified.

The actual LLM used for the T_LLM experiment will be:

`Microsoft 365 Copilot`

Access mode:

`WEB`

Model/mode label exposed by the Microsoft 365 Copilot interface:

`M365 Copilot, basato sul modello di ragionamento GPT-5`

---

## 2. Temporal validity

This correction is recorded before any experimental UIHelpers T_LLM prompt
has been submitted to Microsoft 365 Copilot.

At the time of this amendment:

- zero UIHelpers T_LLM prompts have been sent;
- zero T_LLM Java tests have been generated or accepted;
- no T_LLM JaCoCo measurement has been performed;
- no T_LLM PIT measurement has been performed;
- the original preregistration remains available in Git history;
- the original preregistration file is not silently rewritten.

Therefore this amendment changes provider-specific metadata only and does
not alter results after observation.

---

## 3. Superseded provider-specific statements

Where the original preregistration refers specifically to:

- GitHub Copilot;
- the GitHub Copilot IntelliJ plugin;
- GitHub Copilot being used from IntelliJ IDEA;

those provider-specific statements are superseded by this amendment.

The effective LLM provider is:

`Microsoft 365 Copilot`

IntelliJ IDEA remains the development environment used to inspect, compile,
run, and maintain the Apache Storm project and generated Java tests.

---

## 4. Unchanged experimental design

The following preregistered decisions remain unchanged:

- target class:
  `org.apache.storm.daemon.ui.UIHelpers`;
- baseline suite:
  `T_LLM`;
- target cardinality:
  `N = 35`;
- logical test identifiers:
  `T01 ... T35`;
- prompt sequence:
  `P1 -> P2 -> P3 -> P4 -> optional P5`;
- exactly 35 ordinary executable baseline tests;
- Java 25;
- JUnit Jupiter;
- Mockito only when justified;
- five-run repeatability requirement;
- no use of existing UIHelpers test suites as intentional LLM context;
- no native Apache Storm tests as generation reference;
- no JaCoCo feedback before baseline freeze;
- no PIT feedback before baseline freeze;
- no adequacy-guided modification of T_LLM_BASELINE after freeze;
- independent later JaCoCo measurement;
- independent later PIT measurement;
- CI integration after baseline freeze.

---

## 5. Prompt-context policy

Microsoft 365 Copilot must be instructed to base the experiment on production
information only.

Allowed intentional context remains:

1. `UIHelpers.java`;
2. `isw2/testing/pom.xml`;
3. additional Apache Storm production classes only when necessary to
   understand production signatures, collaborators, types, or behavior.

The following remain excluded as intentional generation context:

- UIHelpers BB tests;
- UIHelpers CF tests;
- UIHelpers MT tests;
- UIHelpers RND tests;
- UIHelpers ES tests;
- UIHelpers integration tests;
- future UIHelpers LLM tests;
- JaCoCo reports;
- PIT reports;
- mutation survivors;
- adequacy comparisons.

---

## 6. Evidence policy

Because Microsoft 365 Copilot is not being used as an IntelliJ GitHub Copilot
plugin, the experiment will not record a GitHub Copilot plugin version.

Instead, the evidence will record:

- Microsoft 365 Copilot as provider;
- access mode (web or application);
- any model/mode label actually exposed by its UI;
- interaction timestamp;
- repository commit;
- exact prompts;
- complete responses where practical;
- generated code;
- repair interactions if required.

If Microsoft 365 Copilot does not expose the underlying model, the experiment
will explicitly record:

`NOT EXPOSED`

rather than infer or guess the model.

---

## 7. Amendment statement

This amendment is part of the preregistered experimental record.

It corrects an instrumentation/provider-description error before the first
LLM interaction and does not change the substantive design of the T_LLM
experiment.

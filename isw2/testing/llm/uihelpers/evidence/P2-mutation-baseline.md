# UIHelpers T_LLM P2 - Mutation baseline

Recorded:

`2026-08-23T18:36:52+02:00`

Frozen repository commit:

`8f0225261e084c5f0caee8899a2d9747c29cedb4`

## Suite

Suite:

`T_LLM P2`

Tests:

`35`

P2 SHA-256:

`4767540BADE73F4F391872AE8218F2005001166495B8FEA1A4E315431453CE94`

No test source was modified before or during mutation measurement.

## Production target

Class:

`org.apache.storm.daemon.ui.UIHelpers`

Production class SHA-256:

`08C2E2460DBFF35E909285DCC11923A81AD2EF09A04CCB441DB2AD289927F79E`

## PIT configuration

PIT:

`1.25.8`

JUnit 5 PIT plugin:

`1.2.3`

Mutators:

`DEFAULTS`

Threads:

`1`

Target tests:

`org.apache.storm.daemon.ui.UIHelpersTest`

Native Apache Storm test artifacts:

`NOT USED`

## Population comparability

Generated mutants:

`338`

Population difference against the T_CF reference:

`0`

Therefore the mutation population is directly comparable with the previous
UIHelpers suites.

## Baseline mutation results

| Result | Count |
|---|---:|
| Killed | 49 |
| Survived | 1 |
| No coverage | 288 |
| Covered mutants | 50 |
| Total mutants | 338 |

Mutation Score:

`14.50%`

Test Strength:

`98.00%`

No Coverage:

`85.21%`

No TIMED_OUT, RUN_ERROR, MEMORY_ERROR or NON_VIABLE mutants were observed.

## Interpretation

The LLM suite reaches only a small portion of the mutation population, which
is consistent with its low structural coverage.

However, of the 50 mutants reached by the suite, 49 are killed.

The main limitation of the suite is therefore mutation reachability rather
than oracle strength on the code it exercises.

## Single surviving mutant

Class:

`org.apache.storm.daemon.ui.UIHelpers`

Method:

`sanitizeStreamName`

Line:

`1773`

Mutator:

`ConditionalsBoundaryMutator`

Description:

`changed conditional boundary`

The production condition is:

`streamName.length() > 0 && Character.isLetter(streamName.charAt(0))`

The current P2 test exercises a non-empty stream name beginning with a letter,
but does not exercise the empty-string boundary.

For the original implementation, an empty string follows the `else` branch
and produces `_s`.

A boundary mutation changing the length comparison can therefore survive
without an empty-string test.

This observation is recorded as mutation-analysis evidence only. The frozen
P2 baseline has not been modified.

Any mutation-guided evolution will be recorded separately from this baseline.

## Raw artifact fingerprints

mutations.xml SHA-256:

`8C74AD9ABCFE719E8C880191BF18F08EC279B19DDB3131115B58724A0EF11138`

mutations.csv SHA-256:

`66C578883BFC466C18B1B0C7987ED7787E1620B02709E8E17F9065845FFA3950`

surviving-mutants.csv SHA-256:

`D29191CA4DBB8EF23CAA38A2B713AB513BCFB10AAADC7215F455BADD1E778900`

no-coverage-mutants.csv SHA-256:

`F5E51154FEED076EF824DA2B5723A788EACC72AEE9AFFF2265F4E980B8EECD43`

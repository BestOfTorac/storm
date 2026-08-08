# SZZ e identificazione dei bug-introducing changes

## Obiettivo

SZZ viene utilizzato nella Milestone 1 per ricostruire l'origine dei
defect e supportare il labeling delle classi buggy.

Il punto di partenza è il catalogo dei fix commit già validato:

```text
isw2/datasets/fix_commit_catalog.csv
```

Il risultato dell'analisi viene scritto in:

```text
isw2/datasets/szz_bug_introducing_changes.csv
```

## Strategia adottata

L'implementazione segue un approccio basic line-based SZZ.

Per ogni bug-fixing commit utilizzabile:

1. viene individuato il parent del fix commit;
2. viene calcolato il diff tra parent e fix;
3. vengono considerate le linee preesistenti rimosse o sostituite;
4. per tali linee viene eseguito `git blame` sul parent;
5. i commit restituiti dal blame sono considerati candidati
   bug-introducing commits.

Il diff viene eseguito con rilevamento dei rename:

```text
git -c diff.renameLimit=0 diff \
    --find-renames \
    --unified=0 \
    PARENT FIX
```

Il blame viene eseguito sulla versione del file precedente al fix.

L'analisi riguarda esclusivamente file Java classificati come
`PRODUCTION`.

## Fix commit multipli dello stesso defect

Uno stesso ticket Jira può essere implementato tramite più fix commit.

Un fix precedente appartenente allo stesso ticket non deve essere
considerato come bug-introducing commit dello stesso defect.

Pertanto, se il blame di una linea restituisce un commit che appartiene
al catalogo dei fix dello stesso `IssueKey`, quel commit viene escluso
dai risultati SZZ.

Nel run finale sono stati scartati 133 casi di questo tipo.

## Esclusione dei merge dall'analisi SZZ

Il catalogo dei fix contiene due strategie:

```text
NON_MERGE
FIRST_PARENT_MERGE
```

I commit `FIRST_PARENT_MERGE` sono mantenuti per il calcolo di `NFIX`,
dove interessa stabilire se un defect fix è entrato nella storia
raggiungibile di una classe.

Per SZZ vengono invece utilizzati esclusivamente i commit
`NON_MERGE`.

La motivazione è che il diff tra un merge e il suo first parent può
contenere modifiche di sincronizzazione del branch non direttamente
riconducibili al defect indicato nel messaggio di commit.

### Caso STORM-1419

L'analisi preliminare aveva incluso due merge associati a STORM-1419.

Il commit:

```text
023cebd062e002577e7a4ed38ef795060040f812
```

ha subject:

```text
Merge remote-tracking branch 'apache/master' into STORM-1419
```

e, rispetto al first parent, contiene:

```text
1366 files changed
12006 insertions
7380 deletions
1167 Java files changed
```

Applicare SZZ a tale merge produceva:

```text
1216 SZZ rows
861 production files
361 distinct BICs
3985 blamed lines
```

Quasi tutte queste modifiche derivavano dalla sincronizzazione di
`apache/master` nel branch e non potevano essere attribuite in modo
affidabile al defect STORM-1419.

Il secondo merge dello stesso ticket:

```text
b9bf574a11ecdf9957c8c5d526e0528c4e13ea97
```

era molto più piccolo, ma rimane comunque un merge e presenta la stessa
ambiguità causale.

Per evitare falsi bug-introducing changes, l'analisi SZZ finale esclude
quindi tutti i fix selezionati tramite `FIRST_PARENT_MERGE`.

Questa è una scelta metodologica conservativa del progetto e non una
regola aggiuntiva esplicitamente imposta dalle specifiche della
Milestone 1.

## Output

Il dataset SZZ ha schema:

```text
IssueKey
FixCommitId
FixSelectionStrategy
ParentCommitId
FixedFilePath
BlamedFilePath
BugIntroducingCommitId
BlamedLineCount
```

`FixedFilePath` identifica il file Java interessato dal fix.

`BlamedFilePath` rappresenta il percorso del file nel parent sul quale
è stato eseguito il blame ed è mantenuto separato per gestire
correttamente eventuali rename.

`BugIntroducingCommitId` è il commit candidato individuato tramite
blame.

`BlamedLineCount` indica quante delle linee considerate per quella
combinazione file/BIC sono state attribuite allo stesso commit.

## Risultato finale

Il run finale utilizza soltanto fix commit `NON_MERGE`:

```text
Input issue-commit pairs:                 1074
Input defects:                             584
Unique fix commits analyzed:              1064
SZZ rows:                                  3786
Defects with SZZ evidence:                  530
Unique bug-introducing commits:             957
Distinct fixed production files:            643
Git diff reads:                            1064
Git blame range reads:                     7113
Fix commits without deleted
production lines:                           103
Production files with deleted lines:       1905
Same-issue fix blames skipped:              133
```

Sono state verificate le seguenti invarianti:

```text
Non-NON_MERGE rows = 0
BlamedLineCount <= 0 = 0
Duplicate exact SZZ rows = 0
Same-issue fix used as BIC = 0
```

Dei 584 defect con almeno un fix `NON_MERGE`, 530 producono quindi
evidenza SZZ utilizzabile.

I defect privi di evidenza SZZ non vengono automaticamente considerati
non buggy: semplicemente non forniscono evidenza sufficiente per
attribuire in modo affidabile un bug-introducing change tramite questa
implementazione di SZZ.

## Ruolo nel labeling finale

SZZ costituisce uno degli input del labeling.

La successiva fase combina:

```text
SZZ
+
Affected/Fix Versions Jira
+
Proportion Total
+
release catalog
```

per determinare le osservazioni `(release, class)` da etichettare con:

```text
BUGGY = YES
```

Tutte le altre osservazioni rimarranno inizialmente:

```text
BUGGY = NO
```

secondo il workflow della Milestone 1.
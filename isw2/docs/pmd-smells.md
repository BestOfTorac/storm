# NSMELLS extraction with PMD

## Obiettivo

Questa fase calcola la metrica `NSMELLS` per ogni osservazione
classe-release production utilizzata nella Milestone 1.

Il requisito prevede l'uso di uno strumento di static analysis per
individuare i code smell delle classi Java.

Per il progetto Apache Storm viene utilizzato PMD.

## Tool

La configurazione utilizzata è:

```text
PMD 7.26.0
```

Il ruleset del progetto è congelato in:

```text
isw2/config/pmd-smells.xml
```

Lo stesso ruleset viene applicato a tutte le 18 release del dataset.

## Definizione di NSMELLS

Per ogni osservazione:

```text
(release, Java production class)
```

la metrica è definita come:

```text
NSMELLS =
numero di violazioni PMD del ruleset selezionato
rilevate nel file Java della classe
```

Una classe che non presenta alcuna violazione ha quindi:

```text
NSMELLS = 0
```

Più violazioni nello stesso file vengono conteggiate separatamente.

## Scope

L'analisi viene eseguita esclusivamente sulle classi:

```text
SourceCategory = PRODUCTION
```

presenti in:

```text
isw2/datasets/java_class_inventory.csv
```

Sono quindi esclususe dall'analisi le sorgenti classificate come:

```text
TEST
EXAMPLE
GENERATED
```

Il numero complessivo di osservazioni analizzate è:

```text
14611
```

## Release snapshots

Per ciascuna delle 18 release incluse nel dataset viene creato un
worktree Git temporaneo sul relativo commit ufficiale.

Il branch di lavoro dell'utente non viene modificato.

Per ogni snapshot PMD riceve tramite file list soltanto i file
production presenti nell'inventario della release.

Al termine dell'analisi il worktree temporaneo viene rimosso.

## Java language level

Per mantenere una configurazione uniforme sulle release storiche viene
utilizzato:

```text
java-1.8
```

## PMD ruleset

Il ruleset è orientato a design e manutenibilità e comprende:

```text
AvoidDeeplyNestedIfStmts
CognitiveComplexity
CyclomaticComplexity
DataClass
ExcessiveParameterList
GodClass
NcssCount
NPathComplexity
TooManyFields
TooManyMethods
```

I threshold delle regole non vengono modificati dall'analyzer: vengono
utilizzati quelli definiti da PMD.

## Output

La fase produce due dataset.

### smell_metrics.csv

```text
isw2/datasets/smell_metrics.csv
```

Header:

```text
ReleaseIndex,Version,CommitId,FilePath,NSMELLS
```

Contiene esattamente una riga per ogni osservazione production.

### pmd_smell_evidence.csv

```text
isw2/datasets/pmd_smell_evidence.csv
```

Header:

```text
ReleaseIndex,Version,CommitId,FilePath,RuleSet,Rule,Priority,Line,Description
```

Conserva una riga per ogni violazione PMD utilizzata nel calcolo di
`NSMELLS`.

Questo secondo file permette di ricostruire e verificare il valore
della metrica.

## Risultati

Il run finale ha prodotto:

```text
Production observations:          14611
Smell metric observations:        14611

Observations with NSMELLS > 0:     2301
Total PMD smell violations:        5755
Maximum NSMELLS:                     15

PMD evidence rows:                 5755
```

## Risultati per release

```text
Release  Version            Classes   Violations  Classes NSMELLS>0  Max

1        0.9.0.1               413       107              49          11
2        0.9.1-incubating      414       110              50          11
3        0.9.2-incubating      461       127              61          11
4        0.9.3                 540       150              68          11
5        0.9.4                 541       150              68          11
6        0.9.5                 541       151              68          11
7        0.10.0                721       283             118          11
8        0.9.6                 542       153              68          11
9        1.0.0                1114       473             188          11
10       0.10.1                721       283             118          11
11       1.0.1                1117       474             189          11
12       1.0.2                1125       477             189          11
13       0.9.7                 542       153              68          11
14       0.10.2                721       283             118          11
15       1.0.3                1189       553             209          13
16       1.1.0                1358       631             230          15
17       1.0.4                1192       556             208          13
18       1.1.1                1359       641             234          15
```

## Invarianti

Il generator valida automaticamente che:

```text
smell metric rows = production inventory rows

NSMELLS >= 0

ogni coppia (ReleaseIndex, FilePath) compare una sola volta

ogni file analizzato appartiene all'inventario production

ogni evidenza PMD corrisponde a una smell metric

sum(NSMELLS) = PMD evidence rows
```

Il run finale soddisfa tutti questi vincoli.

## Ruolo nelle milestone successive

`NSMELLS` entra come feature nel dataset finale della Milestone 1.

La stessa metrica sarà inoltre utilizzata nelle milestone successive
per distinguere classi con e senza code smell e per il ranking delle
classi coinvolte nell'analisi di refactoring.
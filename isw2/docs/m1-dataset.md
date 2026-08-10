# Milestone 1 - Final Dataset

## Obiettivo

Questo dataset rappresenta l'output finale della fase di dataset
creation della Milestone 1 del progetto ISW2 su Apache Storm.

L'unità di osservazione è:

```text
(release, Java production class)
```

Il dataset finale è disponibile in:

```text
isw2/datasets/storm_m1_dataset.csv
```

## Release considerate

Il catalogo completo contiene 52 release ufficiali di Apache Storm.

Per la costruzione del dataset vengono utilizzate le prime 18 release,
corrispondenti alla porzione iniziale prevista dalla Milestone 1.

Le release sono:

```text
1   0.9.0.1
2   0.9.1-incubating
3   0.9.2-incubating
4   0.9.3
5   0.9.4
6   0.9.5
7   0.10.0
8   0.9.6
9   1.0.0
10  0.10.1
11  1.0.1
12  1.0.2
13  0.9.7
14  0.10.2
15  1.0.3
16  1.1.0
17  1.0.4
18  1.1.1
```

Le informazioni provenienti dall'intera storia disponibile del
progetto vengono comunque utilizzate quando necessario per ricostruire
retrospettivamente la bugginess delle classi delle prime 18 release.

## Scope delle classi

Il dataset include esclusivamente sorgenti Java classificate come:

```text
PRODUCTION
```

Sono escluse:

```text
TEST
EXAMPLE
GENERATED
```

Il numero totale di osservazioni è:

```text
14611
```

## Identificatori

Le prime quattro colonne identificano univocamente l'osservazione:

```text
ReleaseIndex
Version
CommitId
FilePath
```

La chiave logica utilizzata durante l'assemblaggio è:

```text
(ReleaseIndex, FilePath)
```

Non esistono chiavi duplicate nel dataset finale.

## Metriche

### LOC

```text
LOC
```

Numero di linee di codice fisiche non vuote, escluse le linee
contenute esclusivamente nei commenti.

### Change history

```text
LOC_TOUCHED
NR
NFIX
NAUTH
```

`LOC_TOUCHED` misura la quantità complessiva di linee aggiunte e
rimosse nella storia considerata della classe.

`NR` rappresenta il numero di revisioni.

`NFIX` rappresenta il numero cumulativo di defect Jira distinti
associati a fix della classe.

`NAUTH` rappresenta il numero di autori distinti.

### Added LOC

```text
LOC_ADDED
MAX_LOC_ADDED
AVG_LOC_ADDED
```

Descrivono rispettivamente il totale, massimo e valore medio delle
linee aggiunte nelle modifiche della classe.

### Churn

```text
CHURN
MAX_CHURN
AVG_CHURN
```

Il churn utilizzato dal progetto è definito per modifica come:

```text
added lines - deleted lines
```

Le tre metriche rappresentano totale, massimo e valore medio.

### Change Set Size

```text
CHANGE_SET_SIZE
MAX_CHANGE_SET_SIZE
AVG_CHANGE_SET_SIZE
```

Misurano la dimensione dei change set associati alle modifiche della
classe.

Nel dataset storico originale le ultime due colonne erano denominate:

```text
MAX_CHANGE_SET
AVG_CHANGE_SET
```

Nel dataset finale sono rinominate esclusivamente per coerenza
terminologica; i valori non vengono modificati.

### Age

```text
AGE_WEEKS
WEIGHTED_AGE_WEEKS
```

L'età è espressa in settimane.

`WEIGHTED_AGE_WEEKS` rappresenta la variante pesata rispetto alle
modifiche della classe.

## Metriche aggiuntive

Oltre alle metriche principali vengono conservate sette feature raw
aggiuntive.

### Deleted LOC

```text
LOC_DELETED
MAX_LOC_DELETED
AVG_LOC_DELETED
```

Descrivono totale, massimo e media delle linee eliminate.

### Directory dispersion

```text
AVG_ND
MAX_ND
```

`ND` rappresenta il numero di directory parent distinte coinvolte in
una modifica.

### Entropy

```text
AVG_ENTROPY
MAX_ENTROPY
```

L'entropia è calcolata tramite Shannon entropy normalizzata sulla
distribuzione delle modifiche tra i file del change set.

Le metriche aggiuntive vengono mantenute nel dataset raw.

Eventuale ridondanza sarà valutata successivamente durante feature
selection e preprocessing dei classificatori.

## NSMELLS

```text
NSMELLS
```

Rappresenta il numero di violazioni prodotte dal ruleset PMD
selezionato per la classe in una determinata release.

La static analysis viene eseguita sulle classi production di ogni
snapshot delle 18 release.

Il run finale ha prodotto:

```text
NSMELLS > 0:              2301
NSMELLS = 0:             12310
Total PMD violations:     5755
Maximum NSMELLS:            15
```

La metodologia completa è documentata in:

```text
isw2/docs/pmd-smells.md
```

## BUGGY

```text
BUGGY
```

È la variabile target ed è deliberatamente posizionata come ultima
colonna del dataset.

I valori possibili sono:

```text
YES
NO
```

Il labeling utilizza:

```text
Jira defect
SZZ
Affected Version
Fix Version
Proportion Total
defect lifecycle
rename-aware class mapping
```

Una osservazione viene etichettata `YES` quando almeno una evidenza
sicura associa un defect alla classe nella release considerata.

Il risultato finale è:

```text
BUGGY=YES:       1243
BUGGY=NO:       13368
```

La metodologia completa è documentata in:

```text
isw2/docs/buggy-labeling.md
```

## Relazione BUGGY / NSMELLS

La distribuzione congiunta nel dataset finale è:

```text
                         NSMELLS>0   NSMELLS=0

BUGGY=YES                    479          764
BUGGY=NO                    1822        11546
```

Questi valori sono descrittivi del dataset e non implicano da soli una
relazione causale tra code smell e defect.

Saranno utilizzati nelle successive analisi sperimentali.

## Struttura finale

Il dataset contiene 29 colonne:

```text
ReleaseIndex
Version
CommitId
FilePath

LOC
LOC_TOUCHED
NR
NFIX
NAUTH

LOC_ADDED
MAX_LOC_ADDED
AVG_LOC_ADDED

CHURN
MAX_CHURN
AVG_CHURN

CHANGE_SET_SIZE
MAX_CHANGE_SET_SIZE
AVG_CHANGE_SET_SIZE

AGE_WEEKS
WEIGHTED_AGE_WEEKS

LOC_DELETED
MAX_LOC_DELETED
AVG_LOC_DELETED

AVG_ND
MAX_ND

AVG_ENTROPY
MAX_ENTROPY

NSMELLS

BUGGY
```

## Assemblaggio

L'assemblaggio viene eseguito da:

```text
DatasetGenerator.java
```

L'inventario delle classi production costituisce l'insieme canonico
delle osservazioni.

I seguenti dataset vengono uniti tramite:

```text
(ReleaseIndex, FilePath)
```

Input:

```text
java_class_inventory.csv
loc_metrics.csv
historical_metrics.csv
nfix_metrics.csv
smell_metrics.csv
buggy_labels.csv
```

Prima di produrre il file finale il generator verifica che tutti i
dataset metrici abbiano esattamente lo stesso insieme di chiavi
dell'inventario production.

Vengono inoltre verificate `Version` e `CommitId`.

## Invarianti finali

Il dataset generato soddisfa:

```text
Rows = 14611
Columns = 29

Duplicate (ReleaseIndex, FilePath) = 0
Missing values = 0

ReleaseIndex range = 1..18

BUGGY=YES = 1243
BUGGY=NO = 13368

NSMELLS>0 = 2301
NSMELLS=0 = 12310

BUGGY=YES + BUGGY=NO = 14611
NSMELLS>0 + NSMELLS=0 = 14611
```

Tutte le 14.611 osservazioni presenti nell'inventario production sono
rappresentate esattamente una volta nel dataset finale.

## Output

Il risultato finale della Milestone 1 è:

```text
isw2/datasets/storm_m1_dataset.csv
```

Questo file costituisce la base per le successive fasi di
preprocessing, feature selection e classificazione.
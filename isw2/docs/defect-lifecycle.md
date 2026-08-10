# Defect lifecycle e Proportion Total

## Obiettivo

Questa fase determina l'intervallo di release in cui un defect deve
essere considerato attivo.

Il workflow utilizza:

- il catalogo completo delle release ufficiali;
- i defect Jira validi;
- le evidenze SZZ;
- le Affected Version Jira;
- le Fix Version Jira;
- Proportion Total;
- Git ancestry come fallback per le sole Fix Version mancanti.

Il risultato viene scritto in:

```text
isw2/datasets/defect_lifecycle.csv
```

Il file contiene una riga per ogni defect con evidenza SZZ.

## Release numbering

Le release vengono numerate utilizzando direttamente `Index` di:

```text
isw2/datasets/release_catalog.csv
```

La numerazione considera tutte le 52 release ufficiali del catalogo.

Le prime 18 release hanno:

```text
DatasetIncluded=true
```

e costituiscono il dataset della Milestone 1.

## Opening Version

Per ogni defect:

```text
OV = ultima release ufficiale pubblicata
     non successivamente alla data di apertura Jira
```

Tutti i 530 defect con evidenza SZZ hanno una OV risolvibile.

## Proportion Total

Per i defect con lifecycle osservabile e consistente:

```text
IV <= OV <= FV
IV < FV
```

si calcola:

```text
P = (FV - IV) / (FV - OV)
```

Quando:

```text
FV - OV = 0
```

il denominatore viene posto uguale a:

```text
1
```

`P_TOTAL` è la media dei valori `P` utilizzabili sull'intero catalogo
dei defect Jira, non soltanto sui defect con evidenza SZZ.

Risultato:

```text
Eligible Jira defects:       1193
Usable defects for P_TOTAL:   301
P_TOTAL:         2.332764715904251
P median:        1.5
P minimum:       1.0
P maximum:      18.0
```

Il valore viene calcolato dinamicamente dall'analyzer e non è
hardcoded.

## Fix Version

Per ogni defect con evidenza SZZ, la FV viene determinata nel seguente
ordine.

### 1. Jira Fix Version

Se almeno una Fix Version Jira corrisponde a una release ufficiale del
catalogo:

```text
FV = earliest official Jira FixVersion
```

Questa strategia viene indicata come:

```text
JIRA_FIX_VERSION
```

ed è utilizzata per 524 defect.

### 2. Git containment fallback

Se Jira non contiene alcuna Fix Version ufficiale utilizzabile, viene
cercata la prima release ufficiale che contiene almeno uno dei fix
commit NON_MERGE che hanno prodotto evidenza SZZ.

La verifica utilizza:

```text
git merge-base --is-ancestor FIX RELEASE
```

Questa strategia viene indicata come:

```text
GIT_CONTAINMENT
```

ed è necessaria per 6 defect:

```text
STORM-307  -> 0.9.3
STORM-497  -> 0.9.3
STORM-513  -> 0.9.3
STORM-1997 -> 1.1.0
STORM-3084 -> 2.0.0
STORM-3307 -> 2.0.0
```

Il Git containment viene utilizzato esclusivamente come fallback per
determinare una FV mancante.

Non viene usato per eliminare evidenze SZZ associate ad altri fix
commit dello stesso defect, perché Storm presenta più linee di
sviluppo e maintenance branch paralleli.

## Introduction Version

La IV viene determinata nel seguente ordine.

### 1. Jira Affected Version

La earliest Affected Version ufficiale viene utilizzata se è
consistente:

```text
IV <= OV
IV < FV
```

La strategia è:

```text
JIRA_AFFECTED_VERSION
```

ed è utilizzata per 164 defect.

### 2. Proportion Total

Se la Affected Version è assente, non ufficiale oppure inconsistente,
si utilizza `P_TOTAL`.

Si calcola:

```text
distance = FV - OV

if distance == 0:
    distance = 1

LAV = FV - P_TOTAL * distance
```

La prima release discreta affetta è:

```text
EffectiveIV = ceil(LAV)
```

con limite inferiore:

```text
EffectiveIV >= 1
```

La strategia viene indicata come:

```text
PROPORTION_TOTAL
```

ed è utilizzata per 366 defect.

## Intervallo buggy

Per un determinato defect, le release potenzialmente affette sono:

```text
EffectiveIV <= release < FV
```

La Fix Version è quindi esclusa.

Il lifecycle stabilisce soltanto l'intervallo temporale.

La successiva fase utilizza SZZ per determinare quali classi devono
essere associate al defect all'interno di tale intervallo.

## Risultato finale

Il lifecycle finale contiene:

```text
Defects with SZZ:                    530

FV from Jira:                        524
FV from Git containment:               6

IV from Jira affected version:       164
IV from Proportion Total:            366

Lifecycle overlapping dataset:       333

SZZ rows retained:                  3786
SZZ fix references retained:         920
```

Le 3.786 evidenze SZZ vengono preservate integralmente.

## Invarianti validate

Sono stati verificati i seguenti vincoli:

```text
Lifecycle rows = 530
Duplicate IssueKey = 0

Missing OV = 0
Missing FV = 0
Missing EffectiveIV = 0

OV > FV = 0
EffectiveIV >= FV = 0
EffectiveIV < 1 = 0

SzzRows <= 0 = 0
SzzFixCommits <= 0 = 0

Proportion formula mismatches = 0
OverlapsDataset mismatches = 0
```

Per tutte le righe con:

```text
IVSource=JIRA_AFFECTED_VERSION
```

vale inoltre:

```text
EffectiveIV = ObservedIV
ObservedIV <= OV
ObservedIV < FV
```

Per tutte le righe con:

```text
IVSource=PROPORTION_TOTAL
```

`LAV` ed `EffectiveIV` sono stati verificati ricalcolando direttamente
la formula di Proportion Total.

## Ruolo nel labeling

`defect_lifecycle.csv` non rappresenta ancora il dataset finale.

Il prossimo passaggio combina:

```text
defect_lifecycle.csv
+
szz_bug_introducing_changes.csv
+
java_class_inventory.csv
+
Git rename history
```

per individuare le osservazioni:

```text
(release, class)
```

che devono avere:

```text
BUGGY=YES
```

Tutte le altre classi presenti nelle 18 release saranno inizializzate
con:

```text
BUGGY=NO
```
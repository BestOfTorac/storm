# BUGGY labeling

## Obiettivo

Questa fase assegna la variabile target `BUGGY` alle osservazioni
classe-release della Milestone 1.

Il dataset contiene esclusivamente le classi Java production presenti
nelle prime 18 release selezionate.

La procedura combina:

- defect Jira;
- SZZ;
- Affected Version;
- Fix Version;
- Proportion Total;
- lifecycle dei defect;
- inventario delle classi Java;
- lineage Git rename-aware.

## Input

Gli input principali sono:

```text
isw2/datasets/java_class_inventory.csv
isw2/datasets/defect_lifecycle.csv
isw2/datasets/szz_bug_introducing_changes.csv
```

L'inventario contiene 14.611 osservazioni production appartenenti alle
18 release incluse nel dataset.

## Workflow

Il labeling segue il workflow richiesto dalla Milestone 1.

SZZ viene utilizzato per identificare le classi coinvolte nei defect.

Affected Version, Fix Version e Proportion Total vengono utilizzati per
determinare l'intervallo di release in cui il defect è considerato
presente.

Per ogni defect:

```text
EffectiveIV <= release < FV
```

La Fix Version è quindi esclusa dall'intervallo buggy.

## Identità della classe

Il path di una classe può cambiare nel tempo a causa di rename,
spostamenti di package o riorganizzazioni dei moduli.

Per questo motivo il solo confronto testuale del path non è
sufficiente.

La procedura utilizza due strategie conservative.

### EXACT_BLAMED_PATH

Se il `BlamedFilePath` prodotto da SZZ esiste direttamente nella
release analizzata, la classe viene associata senza ulteriori
trasformazioni.

```text
MappingStrategy = EXACT_BLAMED_PATH
```

### GIT_RENAME_LINEAGE

Quando il path SZZ non esiste direttamente nella release, viene
ricostruito il lineage storico del file tramite:

```text
git log --follow --find-renames --name-status
```

Vengono considerate esclusivamente le entry Git di tipo:

```text
R
```

ovvero veri rename.

Le entry `C` (copy) non vengono utilizzate, perché la copia di un file
non dimostra che i due file rappresentino la stessa classe.

Una associazione rename-aware viene accettata soltanto quando:

1. il path appartiene al lineage Git della classe SZZ;
2. il file è realmente presente nell'inventario production della
   release;
3. il filename della classe rimane invariato;
4. nella release esiste un solo candidato che soddisfa questi vincoli.

La strategia viene registrata come:

```text
MappingStrategy = GIT_RENAME_LINEAGE
```

## Mapping non sicuri

Non viene forzata alcuna associazione quando non esiste una prova
sufficientemente forte dell'identità della classe.

In particolare non vengono utilizzati:

- basename matching da solo;
- similarità testuale dei path;
- copie Git (`C`);
- associazioni tra file con filename differente;
- semplici intersezioni tra gli insiemi di commit della history;
- scelta arbitraria tra più candidati.

Durante lo sviluppo è stata valutata anche una strategia basata sulla
condivisione di commit tra le history dei file.

L'esperimento ha prodotto associazioni chiaramente scorrette tra classi
differenti ed è stato quindi completamente scartato.

Nessuna associazione prodotta da tale esperimento è presente nel
dataset finale.

## Assenza di mapping

Se un defect appartiene temporalmente a una release ma non è possibile
identificare in modo sicuro una classe production corrispondente, non
viene generata alcuna evidenza positiva per quella release.

Questo può verificarsi, ad esempio, quando:

- la classe non esiste ancora nello snapshot;
- il modulo non è presente in quel ramo di sviluppo;
- la classe appartiene a una linea di manutenzione differente;
- non è disponibile un lineage Git univoco.

Questa condizione non modifica `EffectiveIV` o `FV`.

Il lifecycle descrive l'intervallo temporale del defect, mentre il
mapping determina se esiste una concreta osservazione classe-release a
cui applicare l'evidenza.

## Interpretazione di BUGGY=NO

Tutte le osservazioni production vengono inizializzate come:

```text
BUGGY=NO
```

Una osservazione diventa:

```text
BUGGY=YES
```

soltanto quando almeno una evidenza sicura SZZ/lifecycle viene associata
alla coppia:

```text
(release, class)
```

Pertanto `BUGGY=NO` significa che la procedura non ha associato
un'evidenza positiva alla classe-release.

Non implica una dimostrazione formale dell'assenza assoluta di defect.

## Output

La fase produce:

```text
isw2/datasets/buggy_labels.csv
isw2/datasets/buggy_evidence.csv
```

### buggy_labels.csv

Contiene una riga per ogni osservazione production:

```text
ReleaseIndex,Version,CommitId,FilePath,BUGGY
```

Il numero di righe è esattamente:

```text
14611
```

### buggy_evidence.csv

Conserva l'audit delle associazioni positive.

Per ogni evidenza vengono memorizzati:

- IssueKey;
- release;
- classe mappata;
- strategia di mapping;
- EffectiveIV;
- FV;
- sorgente della IV;
- sorgente della FV;
- BlamedFilePath;
- FixedFilePath.

Più evidenze possono riferirsi alla stessa coppia classe-release.

## Risultato finale

```text
Production observations:              14611

Candidate issue/release/class:          4676

EXACT_BLAMED_PATH evidence:             1248
GIT_RENAME_LINEAGE evidence:             936

Safe evidence rows:                     2184
Distinct defects with safe mapping:      250

Ambiguous safe lineage:                    0
No safe class mapping:                  2492

BUGGY=YES:                              1243
BUGGY=NO:                              13368
```

Le 2.184 evidenze positive corrispondono a 1.243 coppie
classe-release uniche, poiché più defect possono interessare la stessa
classe nella stessa release.

## BUGGY per release

```text
Release  Version            Production  BUGGY
1        0.9.0.1                   413     21
2        0.9.1-incubating          414     22
3        0.9.2-incubating          461     34
4        0.9.3                     540     44
5        0.9.4                     541     44
6        0.9.5                     541     46
7        0.10.0                    721     80
8        0.9.6                     542     55
9        1.0.0                    1114     78
10       0.10.1                    721     46
11       1.0.1                    1117    108
12       1.0.2                    1125    103
13       0.9.7                     542     32
14       0.10.2                    721     61
15       1.0.3                    1189    137
16       1.1.0                    1358    118
17       1.0.4                    1192    100
18       1.1.1                    1359    114
```

## Invarianti validate

Sul risultato finale sono stati verificati i seguenti vincoli:

```text
Production label rows = 14611
Duplicate (ReleaseIndex, FilePath) = 0

BUGGY=YES = 1243
BUGGY=NO = 13368

Evidence rows = 2184
Duplicate complete evidence rows = 0

EXACT_BLAMED_PATH = 1248
GIT_RENAME_LINEAGE = 936

Evidence outside [EffectiveIV, FV) = 0

Evidence missing from production inventory = 0
Evidence associated with BUGGY=NO = 0
BUGGY=YES without evidence = 0

EXACT FilePath != BlamedFilePath = 0
GIT_RENAME_LINEAGE with different filename = 0
```

La variabile `BUGGY` può quindi essere utilizzata come target nella
costruzione del dataset finale della Milestone 1.
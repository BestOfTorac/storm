# Milestone 1 - Final Dataset

## Obiettivo

La Milestone 1 costruisce il Dataset A utilizzato nelle successive fasi del progetto ISW2 su Apache Storm.

L'unita di osservazione e la coppia:

`(release, Java production class)`

Per ogni osservazione vengono raccolti:

- identificatori del progetto, della release e della classe;
- metriche di prodotto;
- metriche storiche e di processo;
- numero di defect fix;
- numero di code smell (`NSMELLS`);
- label di bugginess (`BUGGY`).

Sono mantenute due varianti dello stesso Dataset A:

- `isw2/datasets/storm_m1_dataset.csv`: `NSMELLS` calcolato con PMD;
- `isw2/datasets/storm_m1_dataset_sonarcloud.csv`: `NSMELLS` calcolato con SonarCloud.

Le due varianti hanno lo stesso insieme di osservazioni, lo stesso labeling e le stesse metriche.

L'unica colonna i cui valori differiscono e `NSMELLS`.

La variante SonarCloud viene utilizzata come Dataset A principale per le milestone successive.

La variante PMD viene mantenuta come implementazione alternativa e come controllo indipendente.

## Release considerate

Il catalogo completo contiene 52 release ufficiali di Apache Storm.

Per la costruzione del Dataset A vengono utilizzate le prime 18 release del catalogo, corrispondenti alla porzione iniziale prevista dalla Milestone 1.

Le release sono:

1. `0.9.0.1`
2. `0.9.1-incubating`
3. `0.9.2-incubating`
4. `0.9.3`
5. `0.9.4`
6. `0.9.5`
7. `0.10.0`
8. `0.9.6`
9. `1.0.0`
10. `0.10.1`
11. `1.0.1`
12. `1.0.2`
13. `0.9.7`
14. `0.10.2`
15. `1.0.3`
16. `1.1.0`
17. `1.0.4`
18. `1.1.1`

L'ordine segue il catalogo cronologico utilizzato dall'analyzer e non l'ordinamento semantico delle versioni.

Le informazioni provenienti dalla storia successiva del progetto vengono comunque utilizzate quando necessario per ricostruire retrospettivamente defect, fix e bugginess delle classi appartenenti alle prime 18 release.

## Scope delle classi

Il dataset include esclusivamente sorgenti Java classificati come `PRODUCTION`.

Sono esclusi:

- `TEST`;
- `EXAMPLE`;
- `GENERATED`.

L'inventario canonico e:

`isw2/datasets/java_class_inventory.csv`

Il numero totale di osservazioni e:

`14611`

Tutte le 14.611 osservazioni production sono rappresentate esattamente una volta in entrambi i dataset finali.

## Identificatori

Le colonne identificative iniziali sono:

- `Project`;
- `ReleaseIndex`;
- `Version`;
- `CommitId`;
- `FilePath`.

`Project` assume sempre il valore:

`STORM`

La chiave completa utilizzata per join e validazioni e:

`(ReleaseIndex, Version, CommitId, FilePath)`

Non sono presenti chiavi duplicate.

## Metriche

### LOC

`LOC`

Numero di linee di codice fisiche non vuote, escluse le linee contenute esclusivamente nei commenti.

### Change history

- `LOC_TOUCHED`
- `NR`
- `NFIX`
- `NAUTH`

`LOC_TOUCHED` misura la quantita complessiva di linee aggiunte e rimosse nella storia considerata della classe.

`NR` rappresenta il numero di revisioni.

`NFIX` rappresenta il numero cumulativo di defect Jira distinti associati a fix della classe.

`NAUTH` rappresenta il numero di autori distinti.

### Added LOC

- `LOC_ADDED`
- `MAX_LOC_ADDED`
- `AVG_LOC_ADDED`

Descrivono rispettivamente il totale, massimo e valore medio delle linee aggiunte nelle modifiche della classe.

### Churn

- `CHURN`
- `MAX_CHURN`
- `AVG_CHURN`

Il churn utilizzato dal progetto e definito per modifica come:

`added lines - deleted lines`

Le tre metriche rappresentano totale, massimo e valore medio.

### Change Set Size

- `CHANGE_SET_SIZE`
- `MAX_CHANGE_SET_SIZE`
- `AVG_CHANGE_SET_SIZE`

Misurano la dimensione dei change set associati alle modifiche della classe.

### Age

- `AGE_WEEKS`
- `WEIGHTED_AGE_WEEKS`

L'eta e espressa in settimane.

`WEIGHTED_AGE_WEEKS` rappresenta la variante pesata rispetto alle modifiche della classe.

## Metriche aggiuntive

Oltre alle metriche principali vengono conservate sette feature aggiuntive.

### Deleted LOC

- `LOC_DELETED`
- `MAX_LOC_DELETED`
- `AVG_LOC_DELETED`

### Directory dispersion

- `AVG_ND`
- `MAX_ND`

`ND` rappresenta il numero di directory parent distinte coinvolte in una modifica.

### Entropy

- `AVG_ENTROPY`
- `MAX_ENTROPY`

L'entropia e calcolata tramite Shannon entropy normalizzata sulla distribuzione delle modifiche tra i file del change set.

La definizione operativa completa delle metriche e disponibile in:

`isw2/docs/metrics-definition.md`

## NSMELLS

`NSMELLS` rappresenta il numero di code smell associati alla classe nello snapshot della release.

Sono state realizzate due implementazioni indipendenti.

### Variante PMD

Dataset finale:

`isw2/datasets/storm_m1_dataset.csv`

Il run finale ha prodotto:

- osservazioni totali: 14.611;
- osservazioni con `NSMELLS > 0`: 2.301;
- osservazioni con `NSMELLS = 0`: 12.310;
- `NSMELLS` totali: 5.755;
- massimo `NSMELLS`: 15.

La metodologia completa e documentata in:

`isw2/docs/pmd-smells.md`

### Variante SonarCloud

Dataset finale:

`isw2/datasets/storm_m1_dataset_sonarcloud.csv`

La misura utilizzata e la metrica SonarCloud:

`code_smells`

Il run finale ha prodotto:

- osservazioni totali: 14.611;
- osservazioni con `NSMELLS > 0`: 5.338;
- osservazioni con `NSMELLS = 0`: 9.273;
- `NSMELLS` totali: 30.430;
- osservazioni con parse error SonarJava: 7.

La metodologia completa e documentata in:

`isw2/docs/sonarcloud-smells.md`

Le 7 osservazioni con parse error sono conservate separatamente in:

`isw2/datasets/sonar_parse_errors.csv`

`SonarAnalysisStatus` non compare nel Dataset A finale e non viene utilizzato come feature.

## Confronto PMD e SonarCloud

I due Dataset A sono stati confrontati riga per riga.

Entrambi contengono:

- 14.611 righe;
- 30 colonne;
- 18 release;
- nessuna chiave duplicata;
- nessun valore mancante;
- lo stesso labeling `BUGGY`;
- le stesse metriche di prodotto e di processo.

L'unica colonna che differisce e:

`NSMELLS`

Il valore di `NSMELLS` differisce in 6.339 osservazioni.

La differenza e attesa poiche PMD e SonarCloud utilizzano motori e regole di static analysis differenti.

Le due misure non vengono combinate.

## BUGGY

`BUGGY` e la variabile target ed e posizionata come ultima colonna del dataset.

I valori possibili sono:

- `YES`;
- `NO`.

Il labeling utilizza:

- Jira defect;
- SZZ;
- Affected Version;
- Fix Version;
- Proportion;
- defect lifecycle;
- rename-aware class mapping.

Una osservazione viene etichettata `YES` quando almeno una evidenza sicura associa un defect alla classe nella release considerata.

Il risultato finale, identico nelle due varianti, e:

- `BUGGY=YES`: 1.243;
- `BUGGY=NO`: 13.368;
- totale: 14.611.

La metodologia completa e documentata in:

- `isw2/docs/buggy-labeling.md`;
- `isw2/docs/szz-analysis.md`;
- `isw2/docs/defect-lifecycle.md`.

## Struttura finale

Entrambi i Dataset A contengono 30 colonne:

1. `Project`
2. `ReleaseIndex`
3. `Version`
4. `CommitId`
5. `FilePath`
6. `LOC`
7. `LOC_TOUCHED`
8. `NR`
9. `NFIX`
10. `NAUTH`
11. `LOC_ADDED`
12. `MAX_LOC_ADDED`
13. `AVG_LOC_ADDED`
14. `CHURN`
15. `MAX_CHURN`
16. `AVG_CHURN`
17. `CHANGE_SET_SIZE`
18. `MAX_CHANGE_SET_SIZE`
19. `AVG_CHANGE_SET_SIZE`
20. `AGE_WEEKS`
21. `WEIGHTED_AGE_WEEKS`
22. `LOC_DELETED`
23. `MAX_LOC_DELETED`
24. `AVG_LOC_DELETED`
25. `AVG_ND`
26. `MAX_ND`
27. `AVG_ENTROPY`
28. `MAX_ENTROPY`
29. `NSMELLS`
30. `BUGGY`

## Dataset intermedi principali

Gli artefatti principali utilizzati per costruire e verificare il Dataset A sono:

- `release_catalog.csv`
- `java_class_inventory.csv`
- `loc_metrics.csv`
- `historical_metrics.csv`
- `nfix_metrics.csv`
- `defect_catalog.csv`
- `fix_commit_catalog.csv`
- `defect_lifecycle.csv`
- `szz_bug_introducing_changes.csv`
- `buggy_labels.csv`
- `smell_metrics.csv`
- `pmd_smell_evidence.csv`
- `sonar_smell_metrics.csv`
- `sonar_parse_errors.csv`

## Assemblaggio

L'inventario delle classi production costituisce l'insieme canonico delle osservazioni.

I dataset metrici vengono uniti utilizzando gli identificatori della coppia classe-release.

Durante l'assemblaggio vengono verificati anche `Version` e `CommitId`.

Prima di produrre i dataset finali viene verificato che tutti gli input contengano esattamente lo stesso insieme di osservazioni dell'inventario production.

La variante PMD utilizza `smell_metrics.csv`.

La variante SonarCloud utilizza `sonar_smell_metrics.csv`.

Tutte le altre metriche e il labeling `BUGGY` rimangono identici.

## Invarianti finali

Le verifiche finali hanno confermato:

- righe: 14.611;
- colonne: 30;
- release: 18;
- `ReleaseIndex` compreso tra 1 e 18;
- chiavi duplicate: 0;
- valori mancanti: 0;
- `BUGGY=YES`: 1.243;
- `BUGGY=NO`: 13.368;
- PMD `NSMELLS > 0`: 2.301;
- PMD `NSMELLS` totali: 5.755;
- SonarCloud `NSMELLS > 0`: 5.338;
- SonarCloud `NSMELLS` totali: 30.430;
- differenze di schema PMD/SonarCloud: 0;
- differenze `BUGGY` PMD/SonarCloud: 0;
- unica feature con valori differenti: `NSMELLS`.

## Output

I risultati finali della Milestone 1 sono:

- `isw2/datasets/storm_m1_dataset.csv`
- `isw2/datasets/storm_m1_dataset_sonarcloud.csv`

La variante SonarCloud costituisce il Dataset A principale per le successive fasi di preprocessing, classificazione e analisi what-if.

La variante PMD viene mantenuta come implementazione alternativa e strumento di confronto.
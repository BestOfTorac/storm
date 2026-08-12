# Progetto ISW2 - Apache Storm

Questo repository contiene il progetto svolto per il corso di Ingegneria del Software 2 dell'Universita degli Studi di Roma Tor Vergata.

## Progetto analizzato

- Progetto: Apache Storm
- Release di riferimento: 3.0.0
- Tag originale: `v3.0.0`
- Branch della baseline: `baseline-v3.0.0`
- Branch di sviluppo: `isw2-project`

## Struttura

- `analyzer`: analisi delle release e generazione dei dataset
- `config`: configurazioni degli strumenti
- `datasets`: dataset intermedi e finali
- `docs`: metodologia e protocollo sperimentale
- `results`: risultati delle analisi
- `testing`: artefatti relativi alla sperimentazione dei test

## Milestone

1. Dataset creation - completata
2. Classifier evaluation
3. What-if analysis con `NSMELLS = 0`
4. Automated refactoring
5. Software testing experimentation

## Milestone 1 - Dataset creation

La Milestone 1 analizza le prime 18 release selezionate dal catalogo di Apache Storm.

L'unita di osservazione e la coppia:

`(release, Java production class)`

Il dataset comprende esclusivamente sorgenti Java classificati come `PRODUCTION`.

Il risultato finale contiene:

- 18 release;
- 14.611 osservazioni classe-release;
- 30 colonne;
- 1.243 osservazioni `BUGGY=YES`;
- 13.368 osservazioni `BUGGY=NO`.

Sono disponibili due varianti dello stesso Dataset A.

### Dataset PMD

`isw2/datasets/storm_m1_dataset.csv`

In questa variante la feature `NSMELLS` viene calcolata tramite PMD.

Risultati principali:

- osservazioni con `NSMELLS > 0`: 2.301;
- `NSMELLS` totali: 5.755.

La metodologia e documentata in:

`isw2/docs/pmd-smells.md`

### Dataset SonarCloud

`isw2/datasets/storm_m1_dataset_sonarcloud.csv`

In questa variante la feature `NSMELLS` viene calcolata tramite la metrica SonarCloud `code_smells`.

Risultati principali:

- osservazioni con `NSMELLS > 0`: 5.338;
- `NSMELLS` totali: 30.430;
- osservazioni con parse error SonarJava: 7.

Le osservazioni con parse error sono conservate separatamente in:

`isw2/datasets/sonar_parse_errors.csv`

La metodologia e documentata in:

`isw2/docs/sonarcloud-smells.md`

### Confronto PMD / SonarCloud

I due Dataset A sono stati confrontati riga per riga.

Hanno:

- lo stesso numero di osservazioni;
- lo stesso schema;
- gli stessi identificatori;
- le stesse metriche di prodotto e di processo;
- lo stesso labeling `BUGGY`.

L'unica feature con valori differenti e `NSMELLS`.

PMD e SonarCloud producono un valore differente di `NSMELLS` in 6.339 osservazioni.

La variante SonarCloud viene utilizzata come Dataset A principale per le milestone successive.

La variante PMD viene mantenuta come implementazione alternativa e come controllo indipendente.

## Documentazione Milestone 1

La documentazione principale del Dataset A e disponibile in:

`isw2/docs/m1-dataset.md`

Le altre fasi della pipeline sono documentate in:

- `isw2/docs/release-selection.md`
- `isw2/docs/source-selection.md`
- `isw2/docs/metrics-definition.md`
- `isw2/docs/defect-fix-identification.md`
- `isw2/docs/defect-lifecycle.md`
- `isw2/docs/szz-analysis.md`
- `isw2/docs/buggy-labeling.md`
- `isw2/docs/pmd-smells.md`
- `isw2/docs/sonarcloud-smells.md`

## Principali dataset intermedi

Tra gli artefatti utilizzati per costruire e verificare il Dataset A sono conservati:

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

## Ambiente verificato

- Sistema operativo: Windows 11
- Apache Storm baseline: 3.0.0
- Java baseline Apache Storm 3.0.0: JDK 21
- Analyzer ISW2: Java 25
- Maven: 3.9.16

La baseline Apache Storm 3.0.0 e stata compilata con successo tramite Maven.

Le release storiche utilizzate per la static analysis SonarCloud richiedono inoltre configurazioni Java coerenti con il periodo storico analizzato. La procedura e descritta in `isw2/docs/sonarcloud-smells.md`.

## Software testing

I test nativi di Apache Storm non costituiscono i test generati nell'ambito della sperimentazione prevista dal progetto.

La relativa parte del progetto viene mantenuta separata dagli artefatti della Dataset Creation.
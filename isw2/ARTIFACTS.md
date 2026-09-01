# Mappa degli artefatti canonici

Questo documento identifica i principali artefatti da utilizzare come riferimento per la verifica del progetto e per la redazione dei report finali. Gli output intermedi o le campagne superate sono conservati quando utili alla tracciabilità, ma non devono essere confusi con i risultati conclusivi.

## Percorso Falessi

### Milestone 1 - Dataset

Dataset A principale:

- [`datasets/storm_m1_dataset_sonarcloud.csv`](datasets/storm_m1_dataset_sonarcloud.csv)

Variante alternativa PMD:

- [`datasets/storm_m1_dataset.csv`](datasets/storm_m1_dataset.csv)

Documentazione principale:

- [`docs/m1-dataset.md`](docs/m1-dataset.md)
- [`docs/release-selection.md`](docs/release-selection.md)
- [`docs/source-selection.md`](docs/source-selection.md)
- [`docs/metrics-definition.md`](docs/metrics-definition.md)
- [`docs/defect-fix-identification.md`](docs/defect-fix-identification.md)
- [`docs/defect-lifecycle.md`](docs/defect-lifecycle.md)
- [`docs/szz-analysis.md`](docs/szz-analysis.md)
- [`docs/buggy-labeling.md`](docs/buggy-labeling.md)
- [`docs/pmd-smells.md`](docs/pmd-smells.md)
- [`docs/sonarcloud-smells.md`](docs/sonarcloud-smells.md)

### Milestone 2 - Classificatori

Documentazione:

- [`docs/m2-classifiers.md`](docs/m2-classifiers.md)

Risultati:

- [`results/m2/classifier_metrics.csv`](results/m2/classifier_metrics.csv)
- [`results/m2/classifier_summary.csv`](results/m2/classifier_summary.csv)
- [`results/m2/best_classifier.csv`](results/m2/best_classifier.csv)
- [`results/m2/feature_selection.csv`](results/m2/feature_selection.csv)
- [`results/m2/feature_selection_summary.csv`](results/m2/feature_selection_summary.csv)

### Milestone 3 - What-if

Documentazione:

- [`docs/m3-what-if.md`](docs/m3-what-if.md)

Risultati:

- [`results/m3/what_if_predictions.csv`](results/m3/what_if_predictions.csv)
- [`results/m3/what_if_summary.csv`](results/m3/what_if_summary.csv)
- [`results/m3/what_if_table.csv`](results/m3/what_if_table.csv)
- [`results/m3/what_if_transitions.csv`](results/m3/what_if_transitions.csv)

### Milestone 4 - Refactoring

UIHelpers:

- [`results/m4/uihelpers/uihelpers-m4-analysis.md`](results/m4/uihelpers/uihelpers-m4-analysis.md)
- [`results/m4/uihelpers/uihelpers-m4-analysis.csv`](results/m4/uihelpers/uihelpers-m4-analysis.csv)

RedisFilterBolt:

- [`results/m4/redisfilterbolt/redisfilterbolt-m4-analysis.md`](results/m4/redisfilterbolt/redisfilterbolt-m4-analysis.md)
- [`results/m4/redisfilterbolt/redisfilterbolt-m4-analysis.csv`](results/m4/redisfilterbolt/redisfilterbolt-m4-analysis.csv)

## Percorso De Angelis

Classi target definitive:

- [`testing/classes.txt`](testing/classes.txt)

Selezione e rivalutazione delle classi:

- [`docs/testing-class-selection.md`](docs/testing-class-selection.md)
- [`docs/testing-second-class-reassessment.md`](docs/testing-second-class-reassessment.md)

Protocollo congelato per la matrice automatica M4:

- [`docs/testing/m4-automatic-suite-measurement-contract.md`](docs/testing/m4-automatic-suite-measurement-contract.md)

### Evidence manuali

UIHelpers:

- [`testing/results/uihelpers/`](testing/results/uihelpers/)

RedisFilterBolt:

- [`testing/results/redis-filter-bolt/`](testing/results/redis-filter-bolt/)

### Matrice automatica C0-C4

Consolidamento finale:

- [`testing/results/m4-generated/final-deangelis-consolidation/deangelis-30-unit-integrated-matrix.csv`](testing/results/m4-generated/final-deangelis-consolidation/deangelis-30-unit-integrated-matrix.csv)
- [`testing/results/m4-generated/final-deangelis-consolidation/deangelis-30-unit-report-table.csv`](testing/results/m4-generated/final-deangelis-consolidation/deangelis-30-unit-report-table.csv)
- [`testing/results/m4-generated/final-deangelis-consolidation/deangelis-c1-c4-deltas-vs-c0.csv`](testing/results/m4-generated/final-deangelis-consolidation/deangelis-c1-c4-deltas-vs-c0.csv)
- [`testing/results/m4-generated/final-deangelis-consolidation/deangelis-final-consolidation-summary.txt`](testing/results/m4-generated/final-deangelis-consolidation/deangelis-final-consolidation-summary.txt)
- [`testing/results/m4-generated/final-deangelis-consolidation/report-key-findings.txt`](testing/results/m4-generated/final-deangelis-consolidation/report-key-findings.txt)

Sonar delle suite generate:

- [`testing/results/m4-generated/sonar/final-campaign-v5-resume/sonar-30-unit-final-matrix.csv`](testing/results/m4-generated/sonar/final-campaign-v5-resume/sonar-30-unit-final-matrix.csv)

Le misure Sonar di questa campagna riguardano il **codice di test generato** e non devono essere confuse con le misure Sonar del production code impiegate nel percorso Falessi.

## Archivio storico

Il candidato `DefaultHttpCredentialsPlugin`, inizialmente esplorato e successivamente escluso dalla coppia finale di classi target, è conservato in:

- [`testing/archive/rejected-candidates/default-http-credentials-plugin/`](testing/archive/rejected-candidates/default-http-credentials-plugin/)

Il materiale in `archive/` documenta decisioni storiche e non appartiene al set sperimentale finale.

## Report

I report finali sono collocati in:

- [`reports/`](reports/)

Sono previsti due documenti distinti: un report per il percorso Falessi e un report per il percorso De Angelis.

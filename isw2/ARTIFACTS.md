# Mappa degli artefatti canonici

Questo documento rappresenta la **mappa di riferimento degli artefatti del progetto ISW2**.

Il repository contiene volutamente sia output conclusivi sia file intermedi, log, protocolli ed evidence prodotte durante lo sviluppo del progetto. La presenza di questi materiali è utile per la tracciabilità scientifica, ma rende necessario distinguere chiaramente ciò che deve essere utilizzato come risultato finale da ciò che documenta soltanto una fase del processo.

Lo scopo di `ARTIFACTS.md` è quindi rispondere alla domanda:

> Quale file devo utilizzare come riferimento quando devo verificare un risultato o riportarlo nel report?

---

## 1. Classificazione degli artefatti

Gli artefatti possono essere interpretati secondo tre livelli.

### 1.1 Artefatti canonici

Sono i file che rappresentano il risultato conclusivo di una pipeline o di una fase sperimentale.

Quando un valore deve essere riportato nel report, gli artefatti canonici devono essere considerati il riferimento principale.

### 1.2 Evidence e artefatti di supporto

Comprendono log di esecuzione, audit, file diagnostici, output intermedi e documenti che permettono di verificare come è stato ottenuto il risultato canonico.

Questi file hanno valore di tracciabilità, ma non devono sostituire il risultato finale.

### 1.3 Materiale storico o archiviato

Comprende candidati successivamente esclusi, tentativi precedenti e fasi sperimentali superate.

Viene conservato per documentare il percorso seguito, ma non appartiene al set finale dei risultati.

---

# Percorso Falessi

## 2. Milestone 1 - Dataset A

### Dataset principale

Il Dataset A utilizzato come riferimento per M2 e M3 è:

[`datasets/storm_m1_dataset_sonarcloud.csv`](datasets/storm_m1_dataset_sonarcloud.csv)

In questa variante la feature `NSMELLS` deriva da SonarCloud.

### Variante PMD

La variante alternativa è:

[`datasets/storm_m1_dataset.csv`](datasets/storm_m1_dataset.csv)

È conservata per confrontare l'effetto della diversa sorgente della feature smell.

### Documentazione metodologica

La descrizione complessiva della pipeline M1 è:

[`docs/m1-dataset.md`](docs/m1-dataset.md)

La costruzione del dataset è ulteriormente documentata da:

- [`docs/release-selection.md`](docs/release-selection.md)
- [`docs/source-selection.md`](docs/source-selection.md)
- [`docs/metrics-definition.md`](docs/metrics-definition.md)
- [`docs/defect-fix-identification.md`](docs/defect-fix-identification.md)
- [`docs/defect-lifecycle.md`](docs/defect-lifecycle.md)
- [`docs/szz-analysis.md`](docs/szz-analysis.md)
- [`docs/buggy-labeling.md`](docs/buggy-labeling.md)
- [`docs/pmd-smells.md`](docs/pmd-smells.md)
- [`docs/sonarcloud-smells.md`](docs/sonarcloud-smells.md)

Questi documenti devono essere letti insieme ai CSV presenti in `datasets/` quando si vuole ricostruire la provenienza di una feature o del labeling.

---

## 3. Milestone 2 - Classificatori

### Metodologia

[`docs/m2-classifiers.md`](docs/m2-classifiers.md)

Il documento specifica:

- algoritmi confrontati;
- protocollo di validazione;
- feature selection;
- balancing;
- prevenzione del data leakage;
- metriche di valutazione;
- criterio di selezione del classificatore.

### Risultati

Gli output principali sono raccolti in:

[`results/m2/`](results/m2/)

In particolare:

- [`results/m2/classifier_metrics.csv`](results/m2/classifier_metrics.csv): metriche prodotte dalle valutazioni;
- [`results/m2/classifier_summary.csv`](results/m2/classifier_summary.csv): sintesi delle configurazioni;
- [`results/m2/best_classifier.csv`](results/m2/best_classifier.csv): classificatore selezionato;
- [`results/m2/feature_selection.csv`](results/m2/feature_selection.csv): dettaglio delle feature selezionate;
- [`results/m2/feature_selection_summary.csv`](results/m2/feature_selection_summary.csv): sintesi del processo di feature selection.

Per stabilire quale modello alimenta M3 è necessario utilizzare `best_classifier.csv`, senza ricalcolare a posteriori la scelta.

---

## 4. Milestone 3 - What-if

### Metodologia

[`docs/m3-what-if.md`](docs/m3-what-if.md)

### Risultati canonici

- [`results/m3/what_if_predictions.csv`](results/m3/what_if_predictions.csv)
- [`results/m3/what_if_summary.csv`](results/m3/what_if_summary.csv)
- [`results/m3/what_if_table.csv`](results/m3/what_if_table.csv)
- [`results/m3/what_if_transitions.csv`](results/m3/what_if_transitions.csv)

`what_if_summary.csv` rappresenta il punto di ingresso più rapido per i risultati aggregati.

Le predizioni individuali devono invece essere verificate in `what_if_predictions.csv`.

La terminologia da mantenere è:

- A = dataset originale;
- B+ = osservazioni con smell;
- B = variante controfattuale di B+;
- C = osservazioni senza smell.

---

## 5. Milestone 4 - Refactoring Falessi

La baseline non refactorizzata è indicata come `C0`.

Le varianti `C1-C4` rappresentano differenti quantità di informazione di testing messa a disposizione durante il processo di refactoring.

### UIHelpers

Documentazione conclusiva:

[`results/m4/uihelpers/uihelpers-m4-analysis.md`](results/m4/uihelpers/uihelpers-m4-analysis.md)

Tabella strutturata:

[`results/m4/uihelpers/uihelpers-m4-analysis.csv`](results/m4/uihelpers/uihelpers-m4-analysis.csv)

### RedisFilterBolt

Documentazione conclusiva:

[`results/m4/redisfilterbolt/redisfilterbolt-m4-analysis.md`](results/m4/redisfilterbolt/redisfilterbolt-m4-analysis.md)

Tabella strutturata:

[`results/m4/redisfilterbolt/redisfilterbolt-m4-analysis.csv`](results/m4/redisfilterbolt/redisfilterbolt-m4-analysis.csv)

Questi quattro file costituiscono il riferimento conclusivo per rispondere alle domande Falessi su compilazione, smell e variazione delle feature correlate alla bugginess.

I README presenti nelle singole directory `c1/`, `c2/`, `c3/` e `c4/` sono invece evidence specifiche della singola variante.

---

# Percorso De Angelis

## 6. Classi target

L'elenco canonico delle classi è:

[`testing/classes.txt`](testing/classes.txt)

Le due classi definitive sono:

- `org.apache.storm.daemon.ui.UIHelpers`
- `org.apache.storm.redis.bolt.RedisFilterBolt`

La storia della selezione è documentata in:

- [`docs/testing-class-selection.md`](docs/testing-class-selection.md)
- [`docs/testing-second-class-reassessment.md`](docs/testing-second-class-reassessment.md)

---

## 7. Protocollo della matrice automatica

Il protocollo congelato utilizzato per la misura delle suite generate sulle varianti `C0-C4` è:

[`docs/testing/m4-automatic-suite-measurement-contract.md`](docs/testing/m4-automatic-suite-measurement-contract.md)

Questo documento deve essere utilizzato per interpretare:

- che cosa costituisce una unità sperimentale;
- quali suite vengono misurate;
- come vengono trattati compile blocker e suite non passing;
- come sono misurate coverage e mutation;
- come vengono calcolate le metriche strutturali;
- come vengono interpretate le misure Sonar.

---

## 8. Evidence manuali - UIHelpers

Directory principale:

[`testing/results/uihelpers/`](testing/results/uihelpers/)

Questa directory contiene evidence relative alle fasi:

- black-box;
- control-flow;
- mutation-guided;
- coverage;
- mutation testing;
- reliability;
- validazioni cumulative.

Il README storico interno alla directory è mantenuto come indice della sperimentazione della classe.

Per i risultati conclusivi occorre comunque verificare i file strutturati e i riepiloghi finali indicati nelle relative sottocartelle.

---

## 9. Evidence manuali - RedisFilterBolt

Directory principale:

[`testing/results/redis-filter-bolt/`](testing/results/redis-filter-bolt/)

Contiene:

- risultati T_BB;
- risultati T_CF;
- coverage;
- mutation testing;
- classificazione del survivor residuo;
- reliability;
- confronti finali.

Non esiste una `T_MT` dedicata per questa classe, perché non è stato identificato un survivor non equivalente che giustificasse un nuovo test.

---

## 10. Matrice automatica 30 unità

Il consolidamento conclusivo si trova in:

[`testing/results/m4-generated/final-deangelis-consolidation/`](testing/results/m4-generated/final-deangelis-consolidation/)

I file principali sono:

### Matrice integrata

[`testing/results/m4-generated/final-deangelis-consolidation/deangelis-30-unit-integrated-matrix.csv`](testing/results/m4-generated/final-deangelis-consolidation/deangelis-30-unit-integrated-matrix.csv)

Rappresenta il dataset integrato delle 30 unità.

### Tabella pronta per il report

[`testing/results/m4-generated/final-deangelis-consolidation/deangelis-30-unit-report-table.csv`](testing/results/m4-generated/final-deangelis-consolidation/deangelis-30-unit-report-table.csv)

Fornisce una rappresentazione più compatta delle misure utili alla documentazione finale.

### Delta C1-C4 rispetto a C0

[`testing/results/m4-generated/final-deangelis-consolidation/deangelis-c1-c4-deltas-vs-c0.csv`](testing/results/m4-generated/final-deangelis-consolidation/deangelis-c1-c4-deltas-vs-c0.csv)

Contiene i confronti tra ogni variante refactorizzata e la baseline corrispondente.

I delta PIT sono validi soltanto quando entrambe le unità confrontate dispongono di una misura `MEASURED`.

### Sintesi conclusiva

[`testing/results/m4-generated/final-deangelis-consolidation/deangelis-final-consolidation-summary.txt`](testing/results/m4-generated/final-deangelis-consolidation/deangelis-final-consolidation-summary.txt)

### Findings per il report

[`testing/results/m4-generated/final-deangelis-consolidation/report-key-findings.txt`](testing/results/m4-generated/final-deangelis-consolidation/report-key-findings.txt)

---

## 11. Sonar delle suite generate

La matrice Sonar conclusiva è:

[`testing/results/m4-generated/sonar/final-campaign-v5-resume/sonar-30-unit-final-matrix.csv`](testing/results/m4-generated/sonar/final-campaign-v5-resume/sonar-30-unit-final-matrix.csv)

Questa campagna riguarda **il codice delle suite di test generate**.

Non deve essere confusa con le misure Sonar relative al production code e con la feature `NSMELLS` utilizzata nel Dataset A Falessi.

Questa separazione è essenziale per evitare interpretazioni errate nei report.

---

## 12. Interpretazione di mutation score e N/A

La matrice finale contiene:

- 20 unità PIT `MEASURED`;
- 10 unità PIT `N/A`.

`N/A` significa che la misura non è valida nel contesto sperimentale osservato.

Non deve essere:

- convertito in zero;
- incluso artificialmente in una media;
- interpretato come assenza di mutanti uccisi.

Per i confronti tra `C0` e `C1-C4` devono essere utilizzate solo le coppie effettivamente comparabili.

---

## 13. Evidence storiche

Non tutti i file presenti nel repository costituiscono source of truth.

README, log o audit intermedi possono descrivere:

- un tentativo iniziale;
- una configurazione successivamente superata;
- un problema di compatibilità;
- uno stato precedente alle misure finali.

Questi file vengono conservati per tracciabilità e non vengono riscritti retroattivamente.

---

## 14. Archivio dei candidati esclusi

Il materiale di `DefaultHttpCredentialsPlugin` si trova in:

[`testing/archive/rejected-candidates/default-http-credentials-plugin/`](testing/archive/rejected-candidates/default-http-credentials-plugin/)

La directory documenta una scelta sperimentale precedente e non fa parte del set finale delle due classi target.

---

## 15. Regola pratica per report e verifica

Quando più file sembrano contenere lo stesso tipo di informazione, utilizzare il seguente ordine:

1. artefatto canonico indicato in questo documento;
2. summary finale della stessa fase;
3. evidence strutturata di supporto;
4. log e documentazione storica.

In caso di differenza apparente tra una evidence storica e un consolidamento conclusivo, non modificare retroattivamente l'evidence: verificare quale dei due file rappresenta lo stato finale della pipeline.

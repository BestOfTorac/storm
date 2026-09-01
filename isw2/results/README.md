# Risultati

La directory `isw2/results/` raccoglie i **risultati quantitativi conclusivi delle milestone Falessi successive alla costruzione del Dataset A**.

Questa cartella non deve essere utilizzata come contenitore generico di log o output temporanei. I file presenti qui rappresentano risultati che derivano dalle pipeline documentate in `isw2/docs/`.

La struttura è organizzata per milestone.

---

## 1. Relazione tra dataset e risultati

La distinzione principale è:

- [`../datasets/`](../datasets/README.md): dati e input strutturati;
- `results/`: risultati delle analisi effettuate su tali dati;
- [`../testing/results/`](../testing/results/README.md): risultati della sperimentazione De Angelis.

Questa separazione è importante soprattutto nella Milestone 4, dove esistono sia risultati relativi al production code refactorizzato sia misure relative alle suite di test generate.

---

# Milestone 2

## 2. Directory `m2/`

[`m2/`](m2/)

Contiene i risultati della valutazione dei classificatori.

Gli artefatti permettono di ricostruire:

- prestazioni delle configurazioni valutate;
- effetto di feature selection e balancing;
- feature selezionate nei diversi esperimenti;
- scelta del classificatore da utilizzare in M3.

I file principali includono:

- `classifier_metrics.csv`;
- `classifier_summary.csv`;
- `best_classifier.csv`;
- `feature_selection.csv`;
- `feature_selection_summary.csv`.

La metodologia è documentata in:

[`../docs/m2-classifiers.md`](../docs/m2-classifiers.md)

`best_classifier.csv` deve essere considerato il riferimento per identificare il modello selezionato.

---

# Milestone 3

## 3. Directory `m3/`

[`m3/`](m3/)

Raccoglie gli output conclusivi dell'esperimento what-if.

I principali file sono:

- `what_if_predictions.csv`;
- `what_if_summary.csv`;
- `what_if_table.csv`;
- `what_if_transitions.csv`.

### `what_if_predictions.csv`

Contiene il dettaglio delle predizioni utilizzate nel confronto.

### `what_if_summary.csv`

Contiene la sintesi quantitativa principale ed è il punto di ingresso consigliato per la redazione del report.

### `what_if_transitions.csv`

Permette di analizzare le transizioni tra la previsione sulla configurazione originale e quella ottenuta dopo l'azzeramento controfattuale di `NSMELLS`.

La metodologia è descritta in:

[`../docs/m3-what-if.md`](../docs/m3-what-if.md)

---

# Milestone 4

## 4. Directory `m4/`

[`m4/`](m4/)

Contiene gli artefatti relativi alle varianti refactorizzate delle due classi target.

La struttura è separata per classe:

- [`m4/uihelpers/`](m4/uihelpers/)
- [`m4/redisfilterbolt/`](m4/redisfilterbolt/)

All'interno di ciascuna directory sono presenti:

- le evidence delle singole varianti;
- i README storici associati alle varianti;
- l'analisi conclusiva;
- la tabella strutturata dei risultati.

---

## 5. UIHelpers

Il documento conclusivo è:

[`m4/uihelpers/uihelpers-m4-analysis.md`](m4/uihelpers/uihelpers-m4-analysis.md)

La tabella associata è:

[`m4/uihelpers/uihelpers-m4-analysis.csv`](m4/uihelpers/uihelpers-m4-analysis.csv)

Questi file sintetizzano il confronto tra `C0` e `C1-C4`.

Tra gli aspetti verificati rientrano:

- compilazione;
- variazione di `NSMELLS`;
- smell introdotti o rimossi;
- variazione di LOC;
- andamento delle feature correlate con la bugginess;
- risultati dei test post-hoc disponibili.

---

## 6. RedisFilterBolt

Il documento conclusivo è:

[`m4/redisfilterbolt/redisfilterbolt-m4-analysis.md`](m4/redisfilterbolt/redisfilterbolt-m4-analysis.md)

La tabella associata è:

[`m4/redisfilterbolt/redisfilterbolt-m4-analysis.csv`](m4/redisfilterbolt/redisfilterbolt-m4-analysis.csv)

Anche in questo caso l'analisi risponde esplicitamente alle domande richieste per la valutazione delle varianti.

---

## 7. C0 e C1-C4

La terminologia da utilizzare è:

- `C0`: production code originale;
- `C1`: refactoring con assenza di test forniti;
- `C2`: refactoring con T_BB;
- `C3`: refactoring con T_BB + T_CF;
- `C4`: refactoring con tutto il contesto manuale applicabile.

La numerazione identifica **condizioni sperimentali**, non una sequenza di versioni da considerare necessariamente migliorativa.

Non deve quindi essere assunto che `C4 > C3 > C2 > C1` in termini di qualità.

---

## 8. Production code e generated test code

Le misure contenute in questa cartella riguardano principalmente la parte Falessi e il production code delle varianti.

Le misure Sonar delle suite generate, utilizzate nella parte De Angelis, sono invece conservate sotto:

[`../testing/results/m4-generated/`](../testing/results/m4-generated/)

Le due campagne devono rimanere distinte.

---

## 9. Come usare questa cartella nel report

Per M2 utilizzare le tabelle finali di `m2/`.

Per M3 utilizzare `what_if_summary.csv` e, quando necessario, le predizioni individuali.

Per M4 utilizzare i due file `*-m4-analysis.md` insieme ai corrispondenti CSV.

Per un elenco centralizzato dei source of truth consultare:

[`../ARTIFACTS.md`](../ARTIFACTS.md)

# Risultati della sperimentazione di testing

La directory `isw2/testing/results/` contiene le **evidence quantitative e diagnostiche prodotte dalla sperimentazione De Angelis**.

A differenza di `isw2/results/`, che raccoglie principalmente gli output Falessi, questa cartella documenta l'esecuzione e la misura delle suite di test.

La quantità di file è elevata perché vengono preservati:

- risultati finali;
- log;
- audit;
- confronti;
- evidence di compilazione;
- coverage;
- mutation testing;
- reliability;
- behavior preservation;
- campagne Sonar;
- consolidamenti.

---

## 1. Struttura principale

La cartella è organizzata nelle seguenti aree.

### `uihelpers/`

[`uihelpers/`](uihelpers/)

Evidence relative alla classe:

`org.apache.storm.daemon.ui.UIHelpers`

### `redis-filter-bolt/`

[`redis-filter-bolt/`](redis-filter-bolt/)

Evidence relative alla classe:

`org.apache.storm.redis.bolt.RedisFilterBolt`

### `m4-generated/`

[`m4-generated/`](m4-generated/)

Misure della matrice automatica `C0-C4`.

### `ci/`

[`ci/`](ci/)

Evidence relative alla validazione tramite Continuous Integration.

---

# UIHelpers

## 2. Evoluzione manuale

La directory `uihelpers/` conserva le diverse fasi di testing manuale.

L'evoluzione parte dalla suite black-box, procede con il control-flow e viene successivamente supportata dal mutation testing.

Le evidence devono essere lette cumulativamente, distinguendo il set di test disponibile in ciascuna fase.

---

## 3. Reliability

Il set manuale finale viene valutato secondo il profilo operativo definito.

Nel campione osservato vengono eseguite 53 prove e si ottiene:

- `R_hat = 1`;
- `Q_hat = 0`.

Questi valori sono riferiti al campione e al profilo utilizzato e non devono essere descritti come “100% affidabilità in produzione”.

---

## 4. Anomalia C1

Durante la verifica post-hoc di `UIHelpers` C1, il set `T_MT` produce:

- 7 test passing;
- 1 test failing.

Il test coinvolto è:

`mt06CorsConfigurationParametersAreObservable`

L'anomalia fa parte delle evidence e deve rimanere visibile nella documentazione finale.

---

# RedisFilterBolt

## 5. Evoluzione manuale

Per `RedisFilterBolt` la suite manuale comprende:

- 11 test T_BB;
- 4 test T_CF;
- 15 test eseguibili nel set cumulativo finale.

Non viene aggiunto un T_MT dedicato.

---

## 6. Coverage e mutation

Sul target finale vengono osservati:

- line coverage: 45/45 = 100%;
- branch coverage: 21/21 = 100%.

PIT produce:

- 12 mutanti;
- 11 killed;
- 1 survivor.

Il survivor viene analizzato separatamente e classificato come equivalente/ridondante rispetto al comportamento osservabile.

---

## 7. Reliability

La valutazione finale del set manuale eseguibile registra tutte le 15 prove come passing nel profilo operativo considerato.

Anche in questo caso il risultato deve essere interpretato esclusivamente all'interno del profilo sperimentale.

---

# Matrice M4 automatica

## 8. `m4-generated/`

Questa directory contiene le misure delle 30 unità sperimentali:

**2 classi × 5 varianti × 3 tecniche**

Le tre tecniche sono:

- Randoop;
- EvoSuite;
- LLM.

Le cinque condizioni sono:

- C0;
- C1;
- C2;
- C3;
- C4.

---

## 9. Coverage

Le evidence di coverage permettono di confrontare le tecniche sulla stessa classe e variante.

Le metriche vengono riferite alla classe target, evitando che il support code della suite alteri il numeratore o il denominatore della production coverage.

---

## 10. Mutation testing

PIT viene interpretato utilizzando due concetti:

- mutation score;
- test strength.

Il consolidamento finale contiene:

- **20 unità MEASURED**;
- **10 unità N/A**.

`N/A` non equivale a mutation score zero.

Le unità N/A sono mantenute nel dataset finale perché la loro non misurabilità costituisce essa stessa una proprietà osservata della suite.

---

## 11. Sonar

La campagna Sonar finale copre tutte le 30 unità dal punto di vista dell'analisi del codice di test.

Le misure includono:

- numero di smell;
- technical debt;
- attributi Clean Code.

Queste misure riguardano il **generated test code**.

Non devono essere mescolate con `NSMELLS` del production code.

---

## 12. Qualità strutturale

Le suite vengono analizzate anche rispetto a proprietà strutturali che aiutano a interpretarne leggibilità e manutenibilità.

Questo è importante perché una suite più grande o con maggiore coverage non è automaticamente migliore dal punto di vista della qualità del codice di test.

---

## 13. Behavior preservation

Le evidence di behavior preservation verificano se le varianti refactorizzate mantengono il comportamento osservabile rispetto alle suite disponibili.

I fallimenti vengono conservati e non corretti retroattivamente.

---

# Consolidamento finale

## 14. Directory principale

Il riferimento finale è:

[`m4-generated/final-deangelis-consolidation/`](m4-generated/final-deangelis-consolidation/)

Questa directory integra in una singola vista:

- coverage;
- mutation;
- qualità strutturale;
- Sonar;
- delta rispetto a C0.

---

## 15. File principali

### Matrice completa

`deangelis-30-unit-integrated-matrix.csv`

Contiene le 30 combinazioni sperimentali.

### Tabella da report

`deangelis-30-unit-report-table.csv`

Riorganizza le informazioni in una forma più compatta.

### Delta

`deangelis-c1-c4-deltas-vs-c0.csv`

Contiene i confronti tra baseline e varianti.

### Summary

`deangelis-final-consolidation-summary.txt`

Riassume gli esiti principali della pipeline di consolidamento.

### Findings

`report-key-findings.txt`

Raccoglie osservazioni utili alla discussione del report.

---

## 16. Interpretazione corretta dei risultati

I risultati non supportano l'esistenza di una tecnica universalmente migliore.

Una tecnica può:

- ottenere maggiore coverage;
- avere mutation score differente;
- generare più o meno codice;
- produrre più smell;
- risultare non misurabile in alcune configurazioni.

Per questo motivo il confronto deve rimanere multidimensionale.

In particolare non viene costruito un punteggio composito arbitrario.

---

## 17. Record storici

Molte sottocartelle conservano audit e tentativi precedenti.

Questi materiali sono utili per:

- dimostrare la sequenza degli esperimenti;
- ricostruire problemi tecnici;
- verificare come è stato ottenuto il risultato finale.

Non devono però essere utilizzati al posto del consolidamento conclusivo.

Per identificare sempre il source of truth consultare:

[`../../ARTIFACTS.md`](../../ARTIFACTS.md)

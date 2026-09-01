# Sperimentazione di testing

La directory `isw2/testing/` contiene l'intera infrastruttura sperimentale relativa al **percorso De Angelis** e una parte delle evidence utilizzate nella Milestone 4.

È una delle aree più articolate del progetto perché conserva non soltanto le suite finali, ma anche specifiche, generatori, protocolli, configurazioni, output di misura e record storici.

Questo README descrive come interpretare la struttura senza confondere:

- test manuali;
- test generati automaticamente;
- suite originali e suite delle varianti;
- risultati intermedi;
- risultati finali;
- candidati esclusi.

---

## 1. Obiettivo della sperimentazione

Lo scopo è confrontare differenti strategie di testing sulle classi target e studiare come tali strategie interagiscono con le varianti refactorizzate.

Le due classi finali sono:

- `org.apache.storm.daemon.ui.UIHelpers`
- `org.apache.storm.redis.bolt.RedisFilterBolt`

Il riferimento canonico è:

[`classes.txt`](classes.txt)

---

## 2. Come sono state selezionate le classi

La scelta delle classi è documentata separatamente dalla fase di testing, in modo da evitare di giustificare la selezione a posteriori sulla base dei risultati ottenuti.

I documenti sono:

- [`../docs/testing-class-selection.md`](../docs/testing-class-selection.md)
- [`../docs/testing-second-class-reassessment.md`](../docs/testing-second-class-reassessment.md)

La seconda classe inizialmente esplorata è stata successivamente rivalutata. I suoi artefatti non sono stati cancellati ma spostati nell'archivio storico.

---

# Testing manuale

## 3. T_BB - Black-box testing

La prima suite viene progettata utilizzando informazioni sul comportamento atteso della classe.

La tecnica di riferimento è Category Partition.

I test cercano di coprire:

- funzionalità rappresentative;
- categorie di input;
- combinazioni significative;
- condizioni al contorno;
- errori osservabili;
- oracle semplici e difendibili.

La progettazione evita di basarsi sulla struttura interna del metodo quando il test deve essere classificato come black-box.

---

## 4. T_CF - Control-flow testing

La suite `T_CF` viene aggiunta dopo `T_BB`.

L'obiettivo non è ripetere gli stessi casi con nomi diversi, ma individuare percorsi di controllo non adeguatamente esercitati dalla suite precedente.

La suite finale della fase è quindi cumulativa:

`T_BB + T_CF`

---

## 5. T_MT - Mutation-guided testing

Il mutation testing viene utilizzato dopo le prime suite per individuare survivor eventualmente significativi.

Un survivor non implica automaticamente la creazione di un nuovo test.

Prima di aggiungere un `T_MT` vengono valutati:

- raggiungibilità;
- osservabilità;
- equivalenza;
- ridondanza rispetto a oracle già presenti;
- possibilità di costruire un test comprensibile.

Per `RedisFilterBolt`, questa analisi non ha giustificato la creazione di una suite T_MT dedicata.

---

# Suite automatiche

## 6. T_RND - Randoop

Randoop viene utilizzato come tecnica random/semi-random.

Le suite generate vengono congelate prima delle misure conclusive.

I file generati per M4 sono conservati nelle directory dedicate sotto:

[`generated-tests/`](generated-tests/)

---

## 7. T_ES - EvoSuite

La directory:

[`evosuite/`](evosuite/)

contiene materiali relativi alla generazione EvoSuite delle due classi.

EvoSuite 1.2.0 presenta vincoli di compatibilità con alcune versioni di bytecode Java e con determinate dipendenze multi-release.

Queste limitazioni fanno parte dell'esperimento.

Le suite non vengono “ripulite” retroattivamente per eliminare fallimenti o incompatibilità che influenzerebbero la validità del confronto.

---

## 8. T_LLM

La directory:

[`llm/`](llm/)

contiene:

- prompt;
- protocolli;
- context manifest;
- output;
- evidence relative alla generazione LLM.

La generazione è stata eseguita mediante Microsoft 365 Copilot.

Il contesto fornito al modello viene documentato per rendere la procedura il più possibile verificabile.

---

# Organizzazione della directory

## 9. `src/test/java/`

[`src/test/java/`](src/test/java/)

Contiene i test integrati nel testing harness.

Qui sono presenti sia le suite manuali sia i test necessari all'esecuzione delle suite automatiche congelate.

Questa directory rappresenta il livello effettivamente compilabile/eseguibile del testing harness.

---

## 10. `specs/`

[`specs/`](specs/)

Contiene specifiche, partizioni, materiali di progettazione e altri input utilizzati per derivare i test.

La separazione tra `specs/` e `src/test/java/` consente di distinguere la progettazione del test dalla sua implementazione eseguibile.

---

## 11. `generated-tests/`

[`generated-tests/`](generated-tests/)

Contiene le suite generate automaticamente.

La sottostruttura M4 organizza i test secondo:

- classe;
- variante `C0-C4`;
- tecnica `RND`, `ES` o `LLM`.

La matrice complessiva è:

**2 classi × 5 varianti × 3 tecniche = 30 unità sperimentali**

I file sono congelati: una suite che fallisce o non compila non viene modificata dopo aver osservato i risultati delle misure.

---

## 12. `results/`

[`results/`](results/README.md)

Contiene le evidence delle misure.

È suddivisa tra:

- `uihelpers/`;
- `redis-filter-bolt/`;
- `m4-generated/`;
- `ci/`.

La directory `m4-generated/` contiene il confronto sistematico delle 30 unità.

---

## 13. `archive/`

[`archive/`](archive/README.md)

Contiene materiale storicamente rilevante ma non appartenente alla configurazione finale.

Un file in `archive/` non deve essere utilizzato per calcolare i risultati conclusivi.

---

# Protocollo M4 automatico

## 14. Measurement contract

Il documento:

[`../docs/testing/m4-automatic-suite-measurement-contract.md`](../docs/testing/m4-automatic-suite-measurement-contract.md)

definisce il protocollo utilizzato per le misure finali.

È stato congelato prima della campagna conclusiva.

Questo impedisce di modificare criteri o suite sulla base dell'esito osservato.

---

## 15. Metriche

Le suite automatiche vengono confrontate mediante più dimensioni.

### Coverage

Sono considerate, sul target:

- line coverage;
- branch coverage;
- method coverage.

### Mutation testing

PIT viene utilizzato per misurare:

- mutation score;
- test strength.

### Qualità strutturale

Sono valutati elementi come:

- dimensione della suite;
- support code;
- assertion-like statements;
- naming e leggibilità.

### Sonar

Il codice dei test generati viene analizzato anche mediante Sonar.

Le misure riguardano:

- code smell;
- technical debt;
- attributi Clean Code.

---

## 16. PIT e unità N/A

Il consolidamento finale contiene:

- 20 unità `MEASURED`;
- 10 unità `N/A`.

Una unità `N/A` non viene trattata come una misura pari a zero.

Le cause includono:

- suite non completamente passing;
- compile blocker.

I delta tra una variante e `C0` sono calcolati solo quando entrambe le misure sono valide.

---

# Risultati manuali principali

## 17. UIHelpers

La suite manuale finale contiene 53 prove nel profilo operativo utilizzato per la stima di affidabilità.

Nel campione osservato:

- `R_hat = 1`;
- `Q_hat = 0`.

Nella verifica post-hoc della variante C1 rimane inoltre una anomalia documentata nel set `T_MT`:

`mt06CorsConfigurationParametersAreObservable`

Il risultato non viene corretto retroattivamente.

---

## 18. RedisFilterBolt

Il set manuale finale eseguibile comprende 15 test.

Sul target sono stati osservati:

- 100% line coverage;
- 100% branch coverage;
- 11 mutanti killed su 12.

Il survivor residuo non ha giustificato un ulteriore T_MT.

---

# Riproducibilità

## 19. Testing harness

Il testing harness Maven è definito in:

[`pom.xml`](pom.xml)

È separato dai test nativi Apache Storm per rendere controllabile il perimetro della sperimentazione.

---

## 20. Continuous Integration

La CI utilizza:

`.github/workflows/isw2-testing.yml`

e comprende controlli dedicati al progetto.

Gli output ricostruibili e voluminosi, come alcuni siti HTML JaCoCo/PIT e campagne Sonar intermedie, sono esclusi dal versionamento tramite `.gitignore`.

Gli artefatti strutturati finali restano invece versionati.

---

## 21. Come orientarsi

Se si vuole verificare il progetto senza leggere tutti gli artefatti:

1. consultare questo README;
2. aprire [`../ARTIFACTS.md`](../ARTIFACTS.md);
3. leggere il protocollo M4;
4. consultare [`results/README.md`](results/README.md);
5. entrare nelle evidence della classe o della tecnica di interesse.

I README più profondi possono rappresentare stati storici. Per il risultato conclusivo prevalgono gli artefatti indicati in `ARTIFACTS.md`.

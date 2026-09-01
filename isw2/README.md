# Progetto ISW2 - Apache Storm

Questa directory contiene il lavoro realizzato sul progetto open source **Apache Storm** per il corso di **Ingegneria del Software 2** dell'Università degli Studi di Roma Tor Vergata.

La baseline software adottata è **Apache Storm 3.0.0**, identificata dal tag Git `v3.0.0`. Tutti gli artefatti specifici dell'attività accademica sono mantenuti sotto `isw2/`, in modo da separare chiaramente il progetto universitario dal codice e dalla documentazione originali di Apache Storm.

Questo README costituisce il punto di ingresso principale per comprendere l'organizzazione del lavoro. Non sostituisce la documentazione metodologica o le evidence sperimentali: il suo scopo è spiegare come le diverse parti del repository si collegano tra loro e indicare dove trovare i risultati conclusivi.

---

## 1. Obiettivo generale

Il progetto studia Apache Storm da due prospettive complementari.

Il primo percorso riguarda la **software analytics e defect prediction**: costruzione di un dataset storico, analisi delle metriche software e degli smell, addestramento di classificatori, valutazione di uno scenario what-if e studio di refactoring automatici.

Il secondo percorso riguarda il **software testing**: progettazione manuale di test, generazione automatica mediante tecniche differenti, mutation testing, coverage, affidabilità, qualità del codice di test e valutazione delle varianti refactorizzate.

I due percorsi condividono alcuni artefatti della Milestone 4, ma sono mantenuti concettualmente distinti per preservare la tracciabilità degli esperimenti e facilitare la redazione dei due report finali.

---

## 2. Navigazione del repository ISW2

La directory è organizzata secondo la seguente struttura logica.

| Percorso | Ruolo |
| --- | --- |
| [`analyzer/`](analyzer/) | Implementazione degli analyzer e delle pipeline sviluppate per le milestone di software analytics |
| [`config/`](config/) | Configurazioni utilizzate dagli strumenti e dalle pipeline |
| [`datasets/`](datasets/README.md) | Dataset intermedi e finali costruiti durante M1 e utilizzati nelle milestone successive |
| [`docs/`](docs/README.md) | Documentazione metodologica: scelte, protocolli, procedure e motivazioni |
| [`results/`](results/README.md) | Output quantitativi conclusivi di M2, M3 e della parte Falessi di M4 |
| [`testing/`](testing/README.md) | Testing harness, test manuali, suite generate, specifiche ed evidence De Angelis |
| [`ARTIFACTS.md`](ARTIFACTS.md) | Mappa degli artefatti canonici da utilizzare come source of truth |
| [`reports/`](reports/README.md) | Area destinata ai report accademici finali |

La distinzione tra `datasets/`, `results/` e `testing/results/` è intenzionale:

- `datasets/` conserva i dati che costituiscono input o prodotti strutturati delle pipeline di analisi;
- `results/` contiene i risultati quantitativi delle milestone Falessi;
- `testing/results/` contiene le evidence e le misure della sperimentazione di testing.

---

# Parte I - Percorso Falessi

## 3. Milestone 1 - Costruzione del dataset

La prima milestone costruisce un dataset storico a livello di coppia **classe Java di produzione - release**.

La pipeline integra informazioni provenienti da più sorgenti:

- catalogo delle release;
- inventario delle classi di produzione;
- metriche dimensionali e strutturali;
- metriche di processo;
- informazioni sugli sviluppatori;
- ticket classificati come defect;
- fix commit;
- risultati dell'algoritmo SZZ;
- labeling `BUGGY`;
- smell rilevati mediante analisi statica.

Il Dataset A principale comprende **14.611 osservazioni classe-release** ottenute sulle release selezionate.

### NSMELLS

Durante M1 sono state mantenute due varianti della feature `NSMELLS`.

La prima utilizza PMD. La seconda utilizza il numero di `code_smells` rilevato tramite SonarCloud.

La variante SonarCloud rappresenta il **Dataset A principale** impiegato nelle milestone successive, mentre la variante PMD viene mantenuta per tracciabilità e confronto metodologico.

Per comprendere come il dataset è stato costruito, il punto di ingresso corretto è:

[`docs/m1-dataset.md`](docs/m1-dataset.md)

I file dati sono descritti invece in:

[`datasets/README.md`](datasets/README.md)

---

## 4. Milestone 2 - Classificazione

La seconda milestone utilizza il Dataset A per studiare la capacità di classificare le osservazioni come `BUGGY` o `CLEAN`.

Sono stati valutati:

- RandomForest;
- NaiveBayes;
- IBk.

La validazione è stata organizzata in modo da evitare contaminazione tra training e test.

In particolare:

- la feature selection è determinata utilizzando soltanto il training fold;
- SMOTE viene applicato esclusivamente al training set;
- le predizioni sui test fold sono mantenute out-of-fold.

Le prestazioni vengono analizzate tramite le metriche previste dal corso, tra cui Precision, Recall, AUC, Kappa e NPofB20.

La metodologia completa è documentata in:

[`docs/m2-classifiers.md`](docs/m2-classifiers.md)

I risultati numerici sono raccolti in:

[`results/m2/`](results/m2/)

Il classificatore selezionato per la milestone successiva è registrato esplicitamente negli artefatti M2, evitando di ricostruire la scelta a posteriori.

---

## 5. Milestone 3 - Analisi what-if

La terza milestone introduce uno scenario controfattuale per stimare quanti bug potrebbero essere evitati rimuovendo gli smell.

La terminologia utilizzata nel progetto segue quella prevista dal materiale del corso:

- **A**: Dataset A originale;
- **B+**: osservazioni con `NSMELLS > 0`;
- **C**: osservazioni con `NSMELLS = 0`;
- **B**: copia di B+ nella quale `NSMELLS` viene posto artificialmente a zero.

Il classificatore selezionato in M2 viene applicato al dataset controfattuale B.

I risultati finali sono:

- osservazioni effettivamente `BUGGY` in B+: **610**;
- osservazioni stimate `BUGGY` dopo la rimozione virtuale degli smell: **586**;
- classi buggy potenzialmente prevenibili: **24**;
- riduzione rispetto ai buggy di B+: **3,9344%**;
- riduzione rispetto a tutti i buggy di A: **1,9308%**.

Questi valori rappresentano una **stima what-if** e non dimostrano un rapporto causale tra smell e bug.

Metodologia:

[`docs/m3-what-if.md`](docs/m3-what-if.md)

Risultati:

[`results/m3/`](results/m3/)

---

## 6. Milestone 4 - Refactoring automatico

La quarta milestone valuta l'effetto di refactoring automatici su due classi target:

- `org.apache.storm.daemon.ui.UIHelpers`;
- `org.apache.storm.redis.bolt.RedisFilterBolt`.

La versione originale è indicata come `C0`.

Sono poi considerate quattro condizioni sperimentali caratterizzate da una quantità crescente di informazione di testing disponibile:

- **C1**: nessun test fornito;
- **C2**: disponibilità di `T_BB`;
- **C3**: disponibilità di `T_BB + T_CF`;
- **C4**: disponibilità dell'intero insieme manuale applicabile, compresi eventuali `T_MT`.

Per ogni variante viene verificato:

1. se il codice compila;
2. come cambia il numero di smell;
3. quali smell vengono introdotti o rimossi;
4. se aumentano feature positivamente correlate con la bugginess;
5. se aumentano feature negativamente correlate con la bugginess;
6. se il comportamento osservabile rimane coerente con la baseline nei test disponibili.

Le analisi conclusive sono disponibili in:

- [`results/m4/uihelpers/`](results/m4/uihelpers/)
- [`results/m4/redisfilterbolt/`](results/m4/redisfilterbolt/)

---

# Parte II - Percorso De Angelis

## 7. Classi target del testing

Il percorso di testing utilizza le stesse due classi finali:

- `org.apache.storm.daemon.ui.UIHelpers`
- `org.apache.storm.redis.bolt.RedisFilterBolt`

La selezione non è stata effettuata casualmente. Il processo di scelta e la rivalutazione della seconda classe sono documentati in:

- [`docs/testing-class-selection.md`](docs/testing-class-selection.md)
- [`docs/testing-second-class-reassessment.md`](docs/testing-second-class-reassessment.md)

Il file [`testing/classes.txt`](testing/classes.txt) costituisce l'elenco definitivo delle classi target.

---

## 8. Testing manuale

I test del progetto sono stati sviluppati senza utilizzare i test nativi di Apache Storm come suite sperimentale.

Le suite manuali sono organizzate secondo tre categorie.

### T_BB - Black-box testing

I test black-box sono derivati principalmente tramite Category Partition.

L'obiettivo è verificare il comportamento osservabile della classe sulla base di input, output, condizioni operative e condizioni al contorno, senza utilizzare la struttura interna del codice come criterio di progettazione.

### T_CF - Control-flow testing

Dopo la suite black-box vengono analizzati i percorsi di controllo non adeguatamente esercitati.

`T_CF` integra quindi i test iniziali con casi scelti sulla base della struttura del flusso di controllo.

### T_MT - Mutation-guided testing

Il mutation testing viene utilizzato per identificare eventuali debolezze residue della suite.

Un nuovo test viene aggiunto soltanto quando il survivor rappresenta un comportamento distinguibile e il nuovo oracle è tecnicamente e semanticamente giustificato.

Per questo motivo **RedisFilterBolt non possiede una suite T_MT dedicata**: il survivor residuo dopo `T_BB + T_CF` è stato classificato come equivalente o ridondante rispetto al comportamento osservabile. Non è stato introdotto un test artificiale soltanto per aumentare il mutation score.

---

## 9. Suite automatiche

Le suite manuali vengono confrontate con tre tecniche di generazione automatica.

### T_RND - Randoop

Randoop rappresenta l'approccio random/semi-random basato sulla generazione di sequenze di chiamate.

### T_ES - EvoSuite

EvoSuite rappresenta l'approccio search-based.

La versione utilizzata presenta vincoli di compatibilità con bytecode Java recente; tali problemi e le relative soluzioni sperimentali sono documentati nelle evidence e non vengono nascosti o corretti retroattivamente.

### T_LLM - Generazione mediante LLM

La suite LLM è stata generata mediante Microsoft 365 Copilot secondo un protocollo controllato.

Prompt, contesto fornito e anomalie della generazione sono conservati nelle cartelle dedicate, in modo da rendere il processo verificabile.

---

## 10. Matrice sperimentale C0-C4

Per la parte automatica di M4 è stata costruita una matrice composta da:

**2 classi × 5 varianti × 3 tecniche = 30 unità sperimentali**

Per ciascuna combinazione sono state congelate le suite prima delle misure finali.

Le principali dimensioni di confronto sono:

- line coverage;
- branch coverage;
- method coverage;
- mutation score;
- test strength;
- dimensione della suite;
- qualità strutturale;
- code smell;
- technical debt;
- Clean Code attributes Sonar;
- capacità di preservare il comportamento.

Il protocollo completo è disponibile in:

[`docs/testing/m4-automatic-suite-measurement-contract.md`](docs/testing/m4-automatic-suite-measurement-contract.md)

Il consolidamento conclusivo si trova in:

[`testing/results/m4-generated/final-deangelis-consolidation/`](testing/results/m4-generated/final-deangelis-consolidation/)

---

## 11. Interpretazione di PIT

Non tutte le 30 unità possono produrre una misura PIT valida.

Il risultato finale distingue:

- **20 unità MEASURED**;
- **10 unità N/A**.

Una configurazione `N/A` indica che la misura non è valida, ad esempio perché la suite non dispone di una baseline completamente passing oppure è presente un compile blocker.

`N/A` **non viene interpretato come mutation score pari a zero**.

Questa distinzione è fondamentale per evitare confronti quantitativi non validi.

---

## 12. Affidabilità e behavior preservation

Per `UIHelpers`, il set manuale finale comprende 53 prove eseguite secondo il profilo operativo definito.

Nel campione osservato:

- `R_hat = 1`;
- `Q_hat = 0`.

Questi valori descrivono esclusivamente il profilo e il campione sperimentale considerati e non costituiscono un'affermazione di affidabilità assoluta in produzione.

Per `RedisFilterBolt`, il set manuale finale eseguibile comprende 15 test.

La classe raggiunge:

- 100% line coverage sul target;
- 100% branch coverage sul target;
- 11 mutanti killed su 12.

Il survivor residuo è documentato e classificato separatamente.

Anche le anomalie vengono mantenute come evidence. In particolare, nella verifica post-hoc di `UIHelpers` C1 rimane documentato il fallimento di:

`mt06CorsConfigurationParametersAreObservable`

La presenza di tale fallimento non viene nascosta mediante modifiche retroattive alla suite.

---

## 13. Evidence storiche e risultati canonici

Il repository conserva sia risultati finali sia documenti prodotti durante le varie fasi sperimentali.

Questa scelta è intenzionale.

Alcuni README interni possono quindi descrivere uno stato intermedio e contenere espressioni come “not yet performed”. Tali documenti sono da interpretare come **record storico della fase in cui sono stati prodotti**, non come riepilogo dello stato finale del progetto.

Per sapere quale artefatto utilizzare come riferimento conclusivo è necessario consultare:

[`ARTIFACTS.md`](ARTIFACTS.md)

---

## 14. Riproducibilità e Continuous Integration

Il testing harness dedicato è definito in:

[`testing/pom.xml`](testing/pom.xml)

La CI del progetto è implementata nel workflow:

`.github/workflows/isw2-testing.yml`

Il workflow effettua controlli su:

- integrità degli artefatti strutturati;
- build dell'analyzer;
- testing harness;
- suite sperimentali;
- componenti EvoSuite;
- compilazione delle varianti M4;
- final gate complessivo.

Gli output HTML ricostruibili di JaCoCo e PIT e alcune campagne Sonar intermedie non vengono versionati. Restano invece versionati gli artefatti strutturati e le evidence necessarie a ricostruire i risultati conclusivi.

---

## 15. Come leggere il repository

Per una prima valutazione del progetto si suggerisce questo percorso:

1. leggere questo README;
2. consultare [`ARTIFACTS.md`](ARTIFACTS.md);
3. approfondire la metodologia in [`docs/`](docs/README.md);
4. consultare i dati in [`datasets/`](datasets/README.md);
5. verificare i risultati Falessi in [`results/`](results/README.md);
6. esplorare metodologia ed evidence De Angelis in [`testing/`](testing/README.md);
7. utilizzare [`reports/`](reports/README.md) per i documenti accademici conclusivi.

In questo modo è possibile distinguere rapidamente tra metodologia, dati, risultati conclusivi, evidence sperimentali e materiale storico.

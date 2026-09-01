# Progetto ISW2 - Apache Storm

Questo ramo contiene il progetto svolto per il corso di **Ingegneria del Software 2** dell'Università degli Studi di Roma Tor Vergata sul progetto open source **Apache Storm**.

La baseline utilizzata è **Apache Storm 3.0.0**, identificata dal tag `v3.0.0`. Il lavoro del progetto è mantenuto nel branch `isw2-project`, mentre il codice e la documentazione originali di Apache Storm restano separati dagli artefatti aggiunti sotto `isw2/`.

## Navigazione rapida

| Area | Contenuto |
| --- | --- |
| [`docs/`](docs/README.md) | Metodologia e documentazione delle milestone |
| [`datasets/`](datasets/README.md) | Dataset intermedi e finali |
| [`results/`](results/README.md) | Risultati quantitativi M2, M3 e M4 |
| [`testing/`](testing/README.md) | Sperimentazione di testing De Angelis e artefatti M4 |
| [`ARTIFACTS.md`](ARTIFACTS.md) | Mappa degli artefatti canonici da usare per report e verifica |
| [`reports/`](reports/README.md) | Collocazione dei report finali |

## Struttura del progetto

Il lavoro è articolato in due percorsi sperimentali complementari.

### Percorso Falessi

Il percorso Falessi comprende quattro milestone.

**M1 - Dataset creation.** È stato costruito un dataset a livello di coppia `(release, Java production class)` sulle prime 18 release selezionate. Il Dataset A contiene 14.611 osservazioni classe-release. Sono state mantenute due varianti della feature `NSMELLS`: una basata su PMD e una basata su SonarCloud. La variante SonarCloud costituisce il Dataset A principale impiegato nelle milestone successive.

**M2 - Classifier evaluation.** Sono stati confrontati RandomForest, NaiveBayes e IBk mediante validazione 10x10-fold, considerando feature selection e bilanciamento. La feature selection è eseguita esclusivamente sul training fold e SMOTE è applicato soltanto ai dati di training, evitando leakage verso il test fold. I risultati completi sono conservati in `results/m2/`.

**M3 - What-if analysis.** A partire dal Dataset A sono stati costruiti i sottoinsiemi `B+` (`NSMELLS > 0`) e `C` (`NSMELLS = 0`) e la variante controfattuale `B`, ottenuta ponendo `NSMELLS = 0` sulle osservazioni di `B+`. Il confronto ha stimato 610 classi buggy in `B+`, 586 in `B` e quindi 24 classi buggy potenzialmente prevenibili, pari al 3,9344% del sottoinsieme buggy di `B+` e all'1,9308% del totale buggy del Dataset A.

**M4 - Automated refactoring.** Per le classi target sono state costruite le varianti `C1`-`C4`, corrispondenti a quantità crescenti di informazione di testing disponibile al refactoring. Per ciascuna variante sono stati verificati compilazione, variazione degli smell e andamento delle feature correlate alla bugginess rispetto a `C0`.

La documentazione metodologica del percorso è indicizzata in [`docs/README.md`](docs/README.md), mentre i risultati quantitativi sono in [`results/README.md`](results/README.md).

### Percorso De Angelis

Il percorso di testing è stato eseguito sulle due classi finali:

- `org.apache.storm.daemon.ui.UIHelpers`
- `org.apache.storm.redis.bolt.RedisFilterBolt`

Le suite manuali sono state sviluppate senza utilizzare i test nativi di Apache Storm come test del progetto:

- `T_BB`: test black-box derivati tramite Category Partition;
- `T_CF`: test aggiuntivi guidati dal control flow;
- `T_MT`: test mutation-guided quando tecnicamente e semanticamente giustificati.

Sono state inoltre generate suite automatiche mediante:

- `T_RND`: Randoop;
- `T_ES`: EvoSuite;
- `T_LLM`: Microsoft 365 Copilot.

Per la parte M4 è stata completata una matrice di **30 unità sperimentali**: 2 classi × 5 varianti (`C0`-`C4`) × 3 tecniche (`RND`, `ES`, `LLM`). Le suite generate sono state congelate prima delle misure finali e confrontate mediante coverage, mutation testing, qualità strutturale e Sonar.

Per PIT, 20 unità dispongono di una misura valida e 10 sono riportate come `N/A` per baseline non interamente passing o compile blocker. `N/A` non viene interpretato come mutation score pari a zero.

La documentazione e le evidence del testing sono indicizzate in [`testing/README.md`](testing/README.md). La tabella integrata finale delle 30 unità è riportata nella mappa [`ARTIFACTS.md`](ARTIFACTS.md).

## Risultati di affidabilità

Sul set manuale finale di `UIHelpers` sono state eseguite 53 prove secondo il profilo operativo definito, tutte concluse con esito positivo (`R_hat = 1`, `Q_hat = 0` nel campione osservato).

Per `RedisFilterBolt` il set manuale finale eseguibile comprende 15 test. La classe raggiunge 100% di line coverage e branch coverage sul target; PIT produce 11 mutanti killed su 12, con un survivor classificato manualmente come equivalente/ridondante rispetto al comportamento osservabile. Non è stato introdotto un `T_MT` artificiale per questa classe.

Questi valori descrivono esclusivamente gli esperimenti e il profilo operativo osservato e non costituiscono una stima di affidabilità assoluta in produzione.

## Note sulle evidence sperimentali

Alcuni README presenti nelle sottocartelle di `testing/` e `results/m4/` costituiscono **record storici delle singole fasi sperimentali**. Vengono mantenuti nella forma in cui documentavano quella fase, anche quando riportano espressioni come “not yet performed”. Lo stato finale e i riferimenti canonici sono quelli indicati negli indici italiani e in `ARTIFACTS.md`.

Le suite automatiche, i risultati finali e le anomalie sperimentali non vengono corretti retroattivamente. In particolare:

- le unità PIT non misurabili restano `N/A`, non vengono trasformate in zero;
- il fallimento osservato nella verifica `UIHelpers` C1 relativa a `mt06CorsConfigurationParametersAreObservable` resta documentato;
- per `RedisFilterBolt` non viene inventata una suite `T_MT` quando non necessaria;
- le misure Sonar sulle suite generate sono distinte dalle misure Sonar sul production code delle varianti `C0`-`C4`.

## Ambiente principale

- Apache Storm: 3.0.0
- baseline Java di Storm 3.0.0: JDK 21
- analyzer ISW2: Java 25
- Maven: 3.9.16
- sistema operativo di sviluppo principale: Windows 11

Strumenti specifici della sperimentazione di testing e relativi vincoli di compatibilità sono documentati nella cartella `testing/` e nel protocollo di misura M4.

## Continuous Integration

Il workflow `.github/workflows/isw2-testing.yml` verifica l'integrità del repository, la build dell'analyzer, il testing harness, le suite EvoSuite e la compilazione delle varianti M4. Il `final gate` aggrega i controlli necessari alla validazione del progetto.

## Artefatti finali

Per evitare ambiguità tra output intermedi, campagne storiche e risultati conclusivi, utilizzare come riferimento la mappa:

**[`ARTIFACTS.md`](ARTIFACTS.md)**.

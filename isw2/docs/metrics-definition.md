# Definizione delle metriche

## Size (LOC)

Il materiale del corso definisce `Size (LOC)` come numero di linee
di codice, senza indicare una convenzione operativa più dettagliata.

Per rendere l'analisi riproducibile, in questo progetto LOC è definita
come il numero di righe fisiche che contengono almeno un elemento di
codice al di fuori dei commenti.

Non vengono conteggiate:

- righe vuote;
- righe contenenti soltanto spazi;
- righe costituite esclusivamente da commenti `//`;
- righe costituite esclusivamente da commenti `/* ... */`.

Vengono invece conteggiate:

- dichiarazioni di package e import;
- annotazioni;
- dichiarazioni di classi, metodi e attributi;
- istruzioni;
- righe contenenti soltanto parentesi graffe;
- righe contenenti codice seguito o preceduto da un commento.

I delimitatori di commento presenti dentro stringhe o letterali
carattere non vengono interpretati come commenti.

## Momento della misurazione

La LOC di una classe viene calcolata sul contenuto del file nel commit
associato alla relativa release.

La metrica descrive quindi la dimensione della classe nello snapshot
della release, non il numero di righe modificate durante l'intervallo.

## Ambito

La metrica viene calcolata soltanto per i file classificati come
`PRODUCTION`.

Test, esempi e sorgenti generati restano conservati nell'inventario
grezzo, ma non vengono inclusi nel dataset principale.

## Output intermedio

I valori vengono salvati in:

    isw2/datasets/loc_metrics.csv

Il file contiene:

- indice della release;
- versione;
- commit della release;
- percorso della classe;
- LOC.
## Metriche storiche

Le metriche contrassegnate con `*` nel materiale del corso possono
essere calcolate all'interno della singola release oppure
cumulativamente dalla release 0.

Per Apache Storm viene utilizzata la strategia cumulativa.

### Motivazione

La storia di Storm contiene linee di manutenzione parallele. Release
ordinate cronologicamente non sono necessariamente in relazione
diretta di discendenza Git.

Per questo motivo le metriche non vengono calcolate semplicemente
come differenza tra due release consecutive del catalogo.

Per ogni release viene invece analizzata la storia Git raggiungibile
dal commit associato alla release stessa.

### Metriche

Per ciascuna classe di produzione vengono calcolate:

- `LOC_TOUCHED`: somma delle LOC aggiunte e cancellate;
- `NR`: numero di revisioni della classe;
- `NAUTH`: numero di autori distinti;
- `LOC_ADDED`: somma delle LOC aggiunte;
- `MAX_LOC_ADDED`: massimo numero di LOC aggiunte in una revisione;
- `AVG_LOC_ADDED`: LOC aggiunte medie per revisione;
- `CHURN`: somma, sulle revisioni, di LOC aggiunte meno LOC cancellate;
- `MAX_CHURN`: massimo churn di una singola revisione;
- `AVG_CHURN`: churn medio per revisione;
- `CHANGE_SET_SIZE`: somma dei file committati insieme nelle revisioni
  che coinvolgono la classe;
- `MAX_CHANGE_SET`: massimo change set osservato;
- `AVG_CHANGE_SET`: change set medio per revisione;
- `AGE_WEEKS`: età della classe alla data della release, espressa in
  settimane;
- `WEIGHTED_AGE_WEEKS`: età delle revisioni ponderata tramite LOC
  touched.

`NFIX` viene calcolata separatamente dopo l'identificazione dei ticket
di defect fixing.

### Merge commit

I merge commit non vengono normalmente conteggiati come revisioni
aggiuntive, perché i commit appartenenti ai branch mergiati sono già
raggiungibili nella storia Git e contarli nuovamente potrebbe
duplicare le modifiche.

È stato però rilevato un caso specifico nella storia di Storm:
alcuni moduli, tra cui `storm-hbase` e `storm-hdfs`, sono entrati nella
first-parent history tramite merge commit.

Ignorare completamente i merge produceva 452 osservazioni con
`NR = 0`, relative a 57 percorsi distinti.

Per le sole classi che rimangono senza revisioni dopo l'analisi
ordinaria viene quindi individuata, sulla first-parent history, la
revisione nella quale il percorso compare per la prima volta.

Tale merge viene considerato esclusivamente come revisione di
introduzione della classe.

Dopo questa correzione tutte le 14.611 osservazioni hanno `NR >= 1`.

### Controlli di consistenza

Sul dataset delle metriche storiche sono verificati i seguenti
invarianti:

- 14.611 osservazioni;
- 18 release rappresentate;
- nessuna coppia classe-release duplicata;
- `NR >= 1`;
- `NAUTH <= NR`;
- `LOC_ADDED <= LOC_TOUCHED`;
- `abs(CHURN) <= LOC_TOUCHED`;
- `AGE_WEEKS >= 0`;
- `0 <= WEIGHTED_AGE_WEEKS <= AGE_WEEKS`;
- le metriche medie coincidono con il rispettivo totale diviso `NR`.

Il file intermedio prodotto è:

    isw2/datasets/historical_metrics.csv
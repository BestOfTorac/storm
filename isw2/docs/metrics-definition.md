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
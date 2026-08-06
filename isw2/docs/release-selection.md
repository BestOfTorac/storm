# Selezione delle release

## Obiettivo

Il catalogo delle release definisce l'insieme ordinato delle versioni di
Apache Storm utilizzate nel Milestone 1.

L'analisi è fissata alla release di riferimento `v3.0.0`, in modo che
l'esperimento rimanga riproducibile anche dopo la pubblicazione di nuove
versioni del progetto.

## Fonti

Il catalogo viene costruito combinando tre fonti:

1. Apache Jira, per le release storiche;
2. GitHub Releases, per le release successive non presenti in Jira;
3. i tag del repository Git locale, per individuare il commit esatto
   associato a ciascuna release.

Da Jira vengono considerate esclusivamente le versioni:

- marcate come rilasciate;
- non archiviate;
- dotate di una data di rilascio.

Da GitHub vengono considerate esclusivamente le release:

- pubblicate;
- non draft;
- non prerelease;
- non successive alla baseline `v3.0.0`.

## Normalizzazione

I nomi delle versioni vengono normalizzati rimuovendo il prefisso `v`
quando seguito da una cifra.

Ad esempio:

- `v2.8.0` diventa `2.8.0`;
- `v0.9.1-incubating` diventa `0.9.1-incubating`.

Questa normalizzazione permette di confrontare Jira, GitHub Releases
e tag Git senza perdere i nomi originali, che restano memorizzati
nelle rispettive colonne del CSV.

## Scelta della data

Quando una release è presente sia in Jira sia in GitHub, la data Jira
ha precedenza.

Per le release assenti da Jira viene utilizzata la data di pubblicazione
di GitHub Releases.

La data del commit associato al tag Git viene conservata separatamente:
essa identifica la revisione del codice e non coincide necessariamente
con la data pubblica della release.

## Ordinamento

Le release vengono ordinate per data di rilascio e, in caso di parità,
per versione normalizzata.

A ciascuna release viene assegnato un indice progressivo a partire da 1.

## Intervallo analizzato

Il materiale del corso richiede di ignorare l'ultimo 66% delle release,
analizzando quindi il primo 34%.

Il catalogo contiene 52 release:

    52 * 0.34 = 17.68

Poiché non è possibile selezionare una frazione di release, viene
utilizzato l'arrotondamento per eccesso:

    ceil(17.68) = 18

Questa scelta garantisce che la parte analizzata non sia inferiore
al 34% richiesto.

Sono quindi incluse le release con indice da 1 a 18. L'ultima release
inclusa è `1.1.1`; la prima release esclusa è `1.0.5`.

## Controlli di consistenza

Il catalogo generato contiene:

- 52 release;
- 18 release incluse nel dataset;
- nessuna versione duplicata;
- nessuna release priva di un tag Git;
- nessuna release priva del commit associato.

Il file prodotto è:

    isw2/datasets/release_catalog.csv
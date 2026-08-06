# Selezione dei sorgenti Java

## Unità di analisi

L'unità di analisi utilizzata nel Milestone 1 è il file Java presente
in una determinata release.

In accordo con il formato richiesto dal corso, il percorso del file
Java viene usato come identificatore della classe.

Una stessa classe presente in più release produce una distinta
osservazione per ciascuna release.

## Inventario

L'inventario viene costruito attraversando direttamente l'albero Git
del commit associato a ciascuna delle 18 release selezionate.

L'operazione viene eseguita tramite JGit e non richiede checkout
fisici, quindi non modifica la working tree del repository.

Il file prodotto è:

    isw2/datasets/java_class_inventory.csv

## Classificazione

Ogni file Java viene assegnato a una delle seguenti categorie:

- `PRODUCTION`: codice sorgente di produzione;
- `TEST`: test unitari, di integrazione e strumenti esclusivamente
  dedicati ai test;
- `EXAMPLE`: applicazioni e topologie dimostrative;
- `GENERATED`: codice prodotto automaticamente, principalmente dalle
  definizioni Apache Thrift;
- `OTHER`: file non riconosciuti dalle regole precedenti.

Le regole sono basate soprattutto sul source set e sul percorso Maven
o storico del progetto:

- `src/main/java`, `src/jvm` e `src/java` identificano normalmente
  codice di produzione;
- `src/test`, `test/jvm`, `tests` e `integration-test` identificano
  codice di test;
- i moduli `examples` e `storm-starter` identificano esempi;
- i percorsi `generated`, `generated-sources` e `gen-java`
  identificano codice generato.

## Package denominati testing

Apache Storm contiene classi collocate in package come:

    storm-core/src/jvm/org/apache/storm/testing

Queste classi appartengono al source set di produzione e possono
essere distribuite come utility del progetto.

La presenza della parola `testing` nel package non è quindi sufficiente
per classificare un file come test. Tali classi restano nella categoria
`PRODUCTION`, salvo che il percorso appartenga esplicitamente a un
source set di test.

## Politica di inclusione

Il dataset delle metriche utilizzerà esclusivamente le osservazioni
classificate come `PRODUCTION`.

Sono escluse:

- le classi di test;
- le classi degli esempi;
- le classi generate automaticamente.

Questa scelta evita che file non mantenuti manualmente o non appartenenti
al prodotto alterino le metriche, il numero di code smell e il labeling
dei difetti.

## Risultati dell'inventario

L'inventario contiene:

- 18 release;
- 18.320 osservazioni file-release;
- 14.611 osservazioni di produzione;
- 1.774 osservazioni di test;
- 844 osservazioni di esempio;
- 1.091 osservazioni di codice generato;
- 0 osservazioni non classificate;
- 1.962 percorsi di produzione distinti nell'intera storia analizzata.

Non sono presenti duplicati sulla coppia release-percorso.

Le osservazioni di produzione sono distribuite principalmente tra:

- `storm-core`: 10.084;
- `external`: 4.416;
- `storm-rename-hack`: 63;
- `storm-buildtools`: 37;
- `storm-netty`: 11.

I valori indicano osservazioni classe-release e non il numero di classi
uniche del singolo modulo.
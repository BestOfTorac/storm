# NSMELLS extraction with SonarCloud

## Obiettivo

Questa fase calcola `NSMELLS` per ciascuna coppia `(release, Java production class)` utilizzata nella Milestone 1.

La misura utilizzata è la metrica SonarCloud `code_smells` associata al file Java.

## Scope

L'analisi riguarda esclusivamente le classi con `SourceCategory = PRODUCTION` presenti in:

`isw2/datasets/java_class_inventory.csv`

Il dataset comprende:

- 18 release;
- 14.611 osservazioni production;
- una osservazione per ogni coppia classe-release.

Sono esclusi test, esempi e sorgenti generati.

## Analisi delle release storiche

Ogni release viene analizzata sul relativo commit Git storico.

Il codice sorgente originale non viene modificato per permettere l'analisi.

A SonarJava vengono fornite esplicitamente le proprietà:

- `sonar.java.source`
- `sonar.java.jdkHome`
- `sonar.java.binaries`
- `sonar.java.libraries`

Il Java source level viene impostato in funzione della release storica:

- Java 6 per le release che utilizzavano Java 6;
- Java 7 per le release che utilizzavano Java 7.

Per l'analisi delle release storiche viene utilizzato JDK 8 come `sonar.java.jdkHome`.

## Bytecode

Il bytecode utilizzato da SonarJava deriva dagli artefatti ufficiali delle release Apache Storm.

Prima dell'utilizzo è stata verificata la corrispondenza tra ogni sorgente Java production e la relativa classe compilata.

La verifica finale copre tutte le osservazioni:

14.611 / 14.611 production sources.

Questo consente di analizzare il codice sorgente storico senza ricompilare artificialmente tutte le vecchie release con un ambiente moderno.

## Dipendenze

Le dipendenze esterne sono state ricostruite separatamente per ciascuna release.

La ricostruzione utilizza, a seconda della release:

- dipendenze Maven storiche;
- distribuzioni ufficiali Apache Storm;
- risoluzione delle dipendenze transitive;
- repository Maven storicamente compatibili;
- selezione mirata dei JAR quando sono presenti conflitti di versione.

Una classpath viene considerata valida soltanto quando SonarJava non segnala simboli Java non risolti.

Non vengono quindi accettate analisi con unresolved Java symbols.

## Release 0.9.0.1

La release `0.9.0.1` è precedente alla completa migrazione di Storm a Maven e utilizza Leiningen.

Le dipendenze sono state ricostruite a partire dai relativi file `project.clj`.

Per questa release sono state analizzate 413 osservazioni production.

Il risultato è:

- osservazioni con `NSMELLS > 0`: 240;
- `NSMELLS` totali: 1.320;
- parse error: 0.

## Validazione per release

Per ogni release il generator verifica automaticamente che:

1. il numero di file restituiti da SonarCloud coincida con il numero di classi production attese;
2. non rimangano unresolved Java symbols;
3. la somma dei `code_smells` dei singoli file coincida con il valore `code_smells` del progetto analizzato.

Un'analisi che non soddisfa queste condizioni non viene accettata.

I risultati vengono salvati per release prima di procedere con la release successiva.

## Parse errors

Sono state rilevate 7 osservazioni con errore di parsing SonarJava.

Gli errori appartengono al codice sorgente storico originale e non vengono corretti modificando artificialmente gli snapshot delle release.

Le osservazioni interessate sono conservate nel file:

`isw2/datasets/sonar_parse_errors.csv`

Il file è un artefatto di audit e non costituisce una feature del Dataset A.

La colonna tecnica `SonarAnalysisStatus` non è quindi presente nel dataset finale utilizzato dai classificatori.

Le osservazioni con parse error dovranno essere considerate esplicitamente nelle successive analisi che attribuiscono significato a `NSMELLS = 0`, in particolare durante la costruzione dei dataset della Milestone 3.

## Output delle metriche SonarCloud

Le misure raccolte da SonarCloud sono conservate in:

`isw2/datasets/sonar_smell_metrics.csv`

Il file contiene esattamente 14.611 osservazioni, una per ciascuna coppia classe-release production.

## Risultati finali

Il run completo sulle 18 release ha prodotto:

- Production observations: 14.611
- SonarCloud observations: 14.611
- Observations con `NSMELLS > 0`: 5.338
- Observations con `NSMELLS = 0`: 9.273
- Total `NSMELLS`: 30.430
- Parse-error observations: 7

## Dataset A SonarCloud

Le misure SonarCloud vengono unite al Dataset A mantenendo invariati tutti gli altri attributi e il labeling `BUGGY`.

Il risultato finale è:

`isw2/datasets/storm_m1_dataset_sonarcloud.csv`

Il dataset contiene:

- 14.611 righe;
- 30 colonne;
- 18 release;
- 1.243 osservazioni `BUGGY=YES`;
- 13.368 osservazioni `BUGGY=NO`.

## Confronto con PMD

Viene mantenuta anche la precedente variante del Dataset A basata su PMD:

`isw2/datasets/storm_m1_dataset.csv`

I due dataset sono stati confrontati riga per riga.

Hanno:

- lo stesso numero di osservazioni;
- lo stesso schema;
- gli stessi identificatori;
- le stesse metriche di prodotto e di processo;
- lo stesso labeling `BUGGY`.

L'unica colonna i cui valori possono differire è `NSMELLS`.

Le due implementazioni producono valori differenti di `NSMELLS` in 6.339 osservazioni.

I risultati complessivi sono:

PMD:
- classi con `NSMELLS > 0`: 2.301;
- `NSMELLS` totali: 5.755.

SonarCloud:
- classi con `NSMELLS > 0`: 5.338;
- `NSMELLS` totali: 30.430.

La differenza è attesa perché PMD e SonarCloud utilizzano motori e regole di static analysis differenti.

Le due misure non vengono combinate.

Per le milestone successive viene utilizzata come variante principale:

`storm_m1_dataset_sonarcloud.csv`

mentre la variante PMD viene conservata come implementazione alternativa e come controllo indipendente.
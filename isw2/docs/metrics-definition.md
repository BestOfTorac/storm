# Definizione delle metriche

## Size (LOC)

Il materiale del corso definisce `Size (LOC)` come numero di linee di
codice, senza indicare una convenzione operativa più dettagliata.

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
della release, non il numero di righe modificate nella sua storia.

## Ambito

Le metriche del dataset principale vengono calcolate soltanto per i
file classificati come `PRODUCTION`.

Test, esempi e sorgenti generati restano conservati nell'inventario
grezzo, ma non vengono inclusi nel dataset principale.

## Output LOC

I valori LOC vengono salvati in:

```text
isw2/datasets/loc_metrics.csv
```

Il file contiene:

- indice della release;
- versione;
- commit della release;
- percorso della classe;
- LOC.

---

# Metriche storiche

Le metriche contrassegnate con `*` nel materiale del corso possono
essere calcolate all'interno della singola release oppure
cumulativamente dalla release 0.

Per Apache Storm viene utilizzata la strategia cumulativa.

## Storia considerata

La storia di Storm contiene linee di manutenzione parallele.
Release ordinate cronologicamente non sono necessariamente in una
relazione diretta di discendenza Git.

Per questo motivo le metriche non vengono calcolate come semplice
differenza tra due release consecutive del catalogo.

Per ogni coppia classe-release viene invece analizzata la storia Git
raggiungibile dal commit associato a quella specifica release.

In questo modo ciascuna release viene valutata rispetto alla propria
linea di sviluppo effettivamente raggiungibile.

## Identità della classe e rename

La storia di una classe viene ricostruita utilizzando Git nativo con
la semantica di:

```text
git log <release> --follow --no-merges
```

L'opzione `--follow` permette di continuare la storia oltre i rename
del file.

Questo è particolarmente importante per Apache Storm, nel quale sono
presenti refactoring e migrazioni di package di grandi dimensioni,
come il passaggio da:

```text
backtype.storm
```

a:

```text
org.apache.storm
```

Il limite di rilevamento dei rename viene disabilitato tramite:

```text
diff.renameLimit=0
```

per permettere a Git di analizzare anche revisioni con molti file
rinominati.

## Revisioni ordinarie e merge

Le revisioni ordinarie della classe vengono raccolte escludendo i
merge commit.

Questo evita di contare nuovamente una modifica già rappresentata dai
commit del branch successivamente integrato.

Storm contiene però anche classi e interi moduli che entrano nella
first-parent history direttamente tramite un merge.

Per gestire questi casi viene analizzato anche il punto più antico in
cui la classe compare nella first-parent history della release.

Se tale revisione è un merge e il file risulta aggiunto rispetto al
primo parent, il merge viene considerato come possibile revisione di
introduzione.

Prima di aggiungerlo viene verificato se l'introduzione sia già
rappresentata da una revisione non-merge appartenente al branch
integrato.

La regola utilizzata è quindi:

```text
introduzione già rappresentata nella normal history
    -> il merge viene ignorato

introduzione non rappresentata nella normal history
    -> il merge viene aggiunto come revisione di introduzione
```

Anche durante questo controllo la storia del file viene seguita oltre
eventuali rename.

Questa strategia permette contemporaneamente di:

- evitare il doppio conteggio di modifiche già presenti nella storia;
- recuperare classi introdotte realmente tramite merge;
- mantenere la continuità della storia attraverso i rename.

Nel dataset finale sono state individuate, cumulativamente sulle
osservazioni classe-release:

- 1.022 introduzioni tramite merge effettivamente recuperate;
- 3.843 possibili introduzioni tramite merge scartate perché già
  rappresentate nella normale storia Git.

Le due categorie corrispondono ai 4.865 candidati a introduzione
tramite merge individuati durante l'analisi.

---

# Metriche calcolate

Per ciascuna classe di produzione e per ciascuna release selezionata
vengono calcolate le seguenti metriche storiche.

## Revisioni e autori

- `NR`: numero di revisioni della classe nella storia considerata;
- `NAUTH`: numero di autori distinti delle revisioni.

Gli autori vengono identificati principalmente tramite indirizzo
email normalizzato in lowercase.

## LOC Touched

Per una revisione:

```text
LOC_TOUCHED_revision = LOC_ADDED_revision + LOC_DELETED_revision
```

La metrica `LOC_TOUCHED` è la somma delle LOC toccate in tutte le
revisioni considerate.

## LOC Added

Vengono calcolate:

- `LOC_ADDED`: somma delle LOC aggiunte;
- `MAX_LOC_ADDED`: massimo numero di LOC aggiunte in una singola
  revisione;
- `AVG_LOC_ADDED`: LOC aggiunte medie per revisione.

La media viene calcolata come:

```text
AVG_LOC_ADDED = LOC_ADDED / NR
```

## LOC Deleted

Vengono calcolate:

- `LOC_DELETED`: somma delle LOC cancellate;
- `MAX_LOC_DELETED`: massimo numero di LOC cancellate in una singola
  revisione;
- `AVG_LOC_DELETED`: LOC cancellate medie per revisione.

La media viene calcolata come:

```text
AVG_LOC_DELETED = LOC_DELETED / NR
```

Per costruzione:

```text
LOC_TOUCHED = LOC_ADDED + LOC_DELETED
```

## Churn

Per ogni revisione:

```text
CHURN_revision = LOC_ADDED_revision - LOC_DELETED_revision
```

Vengono quindi calcolate:

- `CHURN`: somma del churn delle revisioni;
- `MAX_CHURN`: massimo churn osservato in una singola revisione;
- `AVG_CHURN`: churn medio per revisione.

La media viene calcolata come:

```text
AVG_CHURN = CHURN / NR
```

Per costruzione:

```text
CHURN = LOC_ADDED - LOC_DELETED
```

## Change Set Size

Per ogni revisione della classe viene calcolato il numero complessivo
di file modificati dal commit.

Vengono quindi calcolate:

- `CHANGE_SET_SIZE`: somma dei change set size delle revisioni;
- `MAX_CHANGE_SET`: massimo change set size osservato;
- `AVG_CHANGE_SET`: change set size medio per revisione.

La media viene calcolata come:

```text
AVG_CHANGE_SET = CHANGE_SET_SIZE / NR
```

## Number of Modified Directories (ND)

Per ogni commit viene calcolato `ND` come numero di directory distinte
che contengono i file modificati dal commit.

Per una classe vengono poi calcolate:

- `AVG_ND`: numero medio di directory modificate nelle revisioni che
  coinvolgono la classe;
- `MAX_ND`: massimo numero di directory modificate osservato.

`ND` descrive quindi quanto una modifica sia distribuita
strutturalmente nel progetto, e non soltanto quanti file coinvolga.

## Entropy

L'entropia misura quanto le modifiche di un commit siano distribuite
tra i file modificati.

Per ogni file con statistiche LOC numeriche viene calcolato:

```text
changed_i = added_i + deleted_i
```

Successivamente:

```text
p_i = changed_i / total_changed
```

L'entropia di Shannon è:

```text
H = - sum(p_i * log2(p_i))
```

Per rendere confrontabili commit che modificano numeri differenti di
file viene utilizzata l'entropia normalizzata:

```text
Entropy = H / log2(n)
```

dove `n` è il numero di file con almeno una linea modificata e con
statistiche LOC numeriche.

Quando `n <= 1`, oppure non sono disponibili LOC modificabili,
l'entropia viene posta a `0`.

Per costruzione:

```text
0 <= Entropy <= 1
```

Per ciascuna classe vengono quindi calcolate:

- `AVG_ENTROPY`: entropia media delle revisioni;
- `MAX_ENTROPY`: massimo valore di entropia osservato.

## Age

`AGE_WEEKS` rappresenta l'età della classe alla data della release.

Viene calcolata come distanza, espressa in settimane, tra la revisione
più antica considerata per la classe e la data della release.

## Weighted Age

Per ogni revisione viene calcolata la sua età rispetto alla data della
release.

`WEIGHTED_AGE_WEEKS` è la media delle età delle revisioni ponderata
tramite le LOC touched della singola revisione.

In forma concettuale:

```text
WEIGHTED_AGE =
    sum(age_revision * LOC_TOUCHED_revision)
    /
    sum(LOC_TOUCHED_revision)
```

Se tutte le revisioni hanno `LOC_TOUCHED = 0`, viene utilizzato
`AGE_WEEKS`.

## NFIX

`NFIX` rappresenta il numero di defect fix che hanno interessato una
classe.

Nel materiale del corso `Nfix` è definita come `number of defect fixes`
ed è contrassegnata con `*`. Nel progetto viene quindi calcolata
cumulativamente dalla release 0 fino alla release considerata, in modo
coerente con le altre metriche storiche cumulative.

L'identificazione dei defect Jira e dei relativi fix commit è descritta
in `defect-fix-identification.md`.

Il calcolo parte dal catalogo finale:

```text
isw2/datasets/fix_commit_catalog.csv
```

Per ogni coppia release-classe viene ricostruita la storia Git della
classe fino al commit corrispondente alla release.

### Fix non-merge

Per i fix selezionati con strategia `NON_MERGE` viene utilizzata una
storia Git rename-aware che esclude i merge:

```text
git -c diff.renameLimit=0 log RELEASE \
    --follow \
    --no-merges \
    --format=%H \
    -- FILE
```

Gli hash ottenuti vengono confrontati con i fix commit `NON_MERGE`
presenti nel catalogo.

### Fix rappresentati da merge

Alcuni defect non possiedono un commit non-merge affidabile e sono stati
selezionati tramite la strategia `FIRST_PARENT_MERGE`.

Per questi casi viene utilizzata la first-parent history della classe:

```text
git -c diff.renameLimit=0 log RELEASE \
    --follow \
    --first-parent \
    --format=%H \
    -- FILE
```

Questa scelta è necessaria perché questi merge rappresentano
l'integrazione del fix nella linea ufficiale della release.

Un controllo sui 85 fix commit selezionati come
`FIRST_PARENT_MERGE`, corrispondenti a 1103 associazioni
commit-file, ha mostrato:

- `--full-history`: 1103 casi non riconosciuti su 1103;
- `--first-parent`: 0 casi non riconosciuti su 1103.

Per questa categoria di fix viene quindi utilizzata esplicitamente la
first-parent history.

### Conteggio dei defect

`NFIX` conta defect Jira distinti, non commit distinti.

Se più commit appartenenti allo stesso ticket `STORM-xxxx` modificano
la stessa classe, il ticket contribuisce una sola unità a `NFIX` per
quella classe.

Ad esempio:

```text
STORM-100 -> commit A -> Foo.java
STORM-100 -> commit B -> Foo.java
STORM-100 -> commit C -> Bar.java
```

produce:

```text
Foo.java -> +1 NFIX
Bar.java -> +1 NFIX
```

Questa è l'operazionalizzazione adottata per la definizione del corso
`number of defect fixes`: il defect viene contato una volta per classe,
indipendentemente dal numero di commit necessari per implementarne il
fix.

### Output

`NFIX` viene mantenuta inizialmente in un dataset separato:

```text
isw2/datasets/nfix_metrics.csv
```

con schema:

```text
ReleaseIndex,Version,CommitId,FilePath,NFIX
```

La separazione permette di mantenere invariato il dataset delle metriche
storiche già validato.

Il dataset `nfix_metrics.csv` contiene:

- 14611 osservazioni class-release;
- 3336 osservazioni con `NFIX > 0`;
- valore massimo di `NFIX` pari a 19;
- 0 valori negativi;
- 0 duplicati `(ReleaseIndex, FilePath)`;
- 0 chiavi presenti solamente in `nfix_metrics.csv`;
- 0 chiavi presenti solamente in `historical_metrics.csv`.

Le 14611 chiavi coincidono quindi esattamente con quelle di
`historical_metrics.csv`.

Il calcolo ha richiesto:

- 14611 letture della storia `NON_MERGE`;
- 13784 letture della first-parent history;
- 16 delle 18 release con almeno un fix `FIRST_PARENT_MERGE`
  raggiungibile.

---

# Cache delle metriche di commit

`CHANGE_SET_SIZE`, `ND` ed `Entropy` sono proprietà del commit.

Poiché lo stesso commit può interessare molte classi e comparire nella
storia di molte osservazioni classe-release, tali valori vengono
calcolati una sola volta per commit e successivamente riutilizzati
tramite cache.

Questo evita di ripetere inutilmente la stessa analisi Git.

---

# Output intermedio

Le metriche storiche vengono salvate in:

```text
isw2/datasets/historical_metrics.csv
```

Il file contiene:

- indice della release;
- versione;
- commit della release;
- percorso della classe;
- 21 metriche storiche.

La LOC dello snapshot rimane invece nel file separato
`loc_metrics.csv` e verrà unita alle metriche storiche durante la
costruzione del dataset finale.

---

# Controlli di consistenza

Sul dataset finale delle metriche storiche sono stati verificati i
seguenti invarianti:

- 14.611 osservazioni;
- 18 release rappresentate;
- nessuna coppia classe-release duplicata;
- nessun valore mancante;
- `NR >= 1`;
- `NAUTH <= NR`;
- `LOC_TOUCHED = LOC_ADDED + LOC_DELETED`;
- `CHURN = LOC_ADDED - LOC_DELETED`;
- `LOC_ADDED <= LOC_TOUCHED`;
- `LOC_DELETED <= LOC_TOUCHED`;
- `abs(CHURN) <= LOC_TOUCHED`;
- `AVG_LOC_ADDED = LOC_ADDED / NR`;
- `AVG_LOC_DELETED = LOC_DELETED / NR`;
- `AVG_CHURN = CHURN / NR`;
- `AVG_CHANGE_SET = CHANGE_SET_SIZE / NR`;
- `AVG_ND <= MAX_ND`;
- `AVG_ENTROPY <= MAX_ENTROPY`;
- `0 <= AVG_ENTROPY <= 1`;
- `0 <= MAX_ENTROPY <= 1`;
- `AGE_WEEKS >= 0`;
- `0 <= WEIGHTED_AGE_WEEKS <= AGE_WEEKS`.

Tutti i controlli risultano soddisfatti per le 14.611 osservazioni
generate.
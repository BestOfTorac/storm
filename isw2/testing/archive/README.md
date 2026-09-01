# Archivio storico del testing

La directory `isw2/testing/archive/` conserva materiale che ha avuto un ruolo reale nello sviluppo del progetto ma che **non appartiene alla configurazione sperimentale finale**.

L'archivio non è quindi una cartella di file inutilizzati.

La sua funzione è documentare decisioni, tentativi e candidati che hanno contribuito al processo sperimentale ma che sono stati successivamente esclusi.

---

## 1. Perché conservare il materiale escluso

Durante un progetto sperimentale è possibile modificare una scelta metodologica dopo aver raccolto nuove evidence.

Cancellare completamente il materiale precedente renderebbe più difficile comprendere:

- quale scelta era stata inizialmente effettuata;
- perché è stata rivalutata;
- quali risultati hanno motivato la decisione;
- se l'esperimento precedente era effettivamente stato svolto.

Per questo motivo il materiale viene archiviato anziché eliminato.

---

## 2. `rejected-candidates/`

[`rejected-candidates/`](rejected-candidates/)

Contiene classi inizialmente prese in considerazione e successivamente escluse dal set finale.

---

## 3. DefaultHttpCredentialsPlugin

Il candidato attualmente archiviato è:

[`rejected-candidates/default-http-credentials-plugin/`](rejected-candidates/default-http-credentials-plugin/)

La directory conserva:

### `tests/`

Il test black-box sviluppato durante la prima valutazione della classe.

### `results/tbb/`

Le evidence dell'esecuzione iniziale.

Il contenuto viene mantenuto invariato rispetto alla fase in cui è stato prodotto.

---

## 4. Perché non è una classe target finale

La rivalutazione metodologica della seconda classe è documentata in:

[`../../docs/testing-second-class-reassessment.md`](../../docs/testing-second-class-reassessment.md)

Dopo tale rivalutazione, la classe finale selezionata è `RedisFilterBolt`.

Le due classi definitive del progetto sono riportate in:

[`../classes.txt`](../classes.txt)

---

## 5. Come deve essere interpretato l'archivio

I file in questa directory:

- non fanno parte della matrice finale;
- non devono essere inclusi nelle medie o nei confronti conclusivi;
- non devono essere considerati test della configurazione finale;
- possono essere citati quando si descrive il percorso di selezione delle classi.

L'archivio ha quindi valore **storico e metodologico**, non quantitativo.

---

## 6. Source of truth

Per il testing finale utilizzare:

[`../README.md`](../README.md)

Per l'elenco degli artefatti canonici utilizzare:

[`../../ARTIFACTS.md`](../../ARTIFACTS.md)

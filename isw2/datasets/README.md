# Dataset

La directory `isw2/datasets/` contiene i **dati strutturati prodotti e utilizzati dalle pipeline di analisi del progetto**.

Questa cartella deve essere interpretata come il livello “dati” del progetto: qui vengono conservati cataloghi, inventari, metriche, labeling e dataset che vengono successivamente utilizzati dagli analyzer o dalle milestone successive.

Non deve essere confusa con `results/`, che contiene invece i risultati quantitativi finali delle analisi.

---

## 1. Ruolo nella pipeline

La costruzione del Dataset A non avviene in un singolo passaggio.

Le informazioni vengono ottenute da diverse sorgenti, validate separatamente e successivamente combinate.

Tra le principali famiglie di dati utilizzate durante M1 rientrano:

- catalogo delle release;
- inventario delle classi Java;
- metriche delle classi;
- informazioni storiche e di processo;
- ticket classificati come defect;
- fix commit;
- evidence SZZ;
- labeling delle osservazioni;
- misure degli smell.

I singoli CSV permettono di verificare separatamente ogni passaggio prima dell'assemblaggio finale.

---

## 2. Dataset A principale

Il dataset utilizzato come riferimento per le milestone successive è:

[`storm_m1_dataset_sonarcloud.csv`](storm_m1_dataset_sonarcloud.csv)

Ogni riga rappresenta una osservazione associata a una classe Java di produzione in una specifica release.

Il dataset combina le feature raccolte dalle diverse pipeline e la label `BUGGY`.

In questa variante `NSMELLS` deriva dalla metrica SonarCloud `code_smells`.

---

## 3. Variante PMD

È mantenuta anche la variante:

[`storm_m1_dataset.csv`](storm_m1_dataset.csv)

In questa versione `NSMELLS` deriva dalla pipeline PMD.

La presenza di entrambe le versioni è intenzionale: consente di mantenere traccia della differenza tra i due strumenti di static analysis senza sovrascrivere una misurazione con l'altra.

Il Dataset A principale impiegato nelle milestone successive rimane quello SonarCloud.

---

## 4. Artefatti intermedi di M1

Nella directory principale sono presenti file che rappresentano gli step intermedi della pipeline.

Tra questi possono comparire, a seconda della fase:

- `release_catalog.csv`;
- inventari delle classi;
- metriche dimensionali;
- metriche degli smell;
- cataloghi dei defect;
- fix commit;
- risultati SZZ;
- labeling.

Questi file hanno due funzioni:

1. consentire di ricostruire la provenienza delle colonne del Dataset A;
2. rendere verificabile la pipeline senza dover rieseguire ogni analyzer.

Per comprendere il significato metodologico degli artefatti è necessario consultare:

[`../docs/README.md`](../docs/README.md)

---

## 5. Sottocartella M3

[`m3/`](m3/)

Contiene dataset e artefatti utilizzati nella preparazione dell'analisi what-if.

La logica completa dell'esperimento è descritta in:

[`../docs/m3-what-if.md`](../docs/m3-what-if.md)

I risultati finali dell'analisi non devono essere letti da questa cartella, ma da:

[`../results/m3/`](../results/m3/)

---

## 6. Sottocartella testing

[`testing/`](testing/)

Contiene dataset e tabelle di supporto utilizzati durante la selezione e l'analisi delle classi target per il percorso di testing.

Questi dati non fanno parte del Dataset A utilizzato dai classificatori.

La separazione evita di confondere:

- dati destinati alla defect prediction;
- dati utilizzati per scegliere o analizzare le classi di testing.

---

## 7. Come interpretare i file

Un CSV presente in `datasets/` non è automaticamente un “risultato finale”.

È possibile distinguere:

- input strutturati;
- output intermedi;
- dataset consolidati;
- audit e tabelle di supporto.

Per sapere quale file utilizzare come source of truth consultare:

[`../ARTIFACTS.md`](../ARTIFACTS.md)

---

## 8. Relazione con le altre cartelle

Il flusso logico principale è:

`analyzer/` → `datasets/` → `results/`

La documentazione che descrive questa trasformazione è mantenuta in:

[`../docs/`](../docs/README.md)

In questo modo codice, metodologia, dati e risultati rimangono separati e verificabili indipendentemente.

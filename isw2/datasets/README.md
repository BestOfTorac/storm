# Dataset

Questa cartella contiene i dataset intermedi e finali prodotti dal progetto.

## Dataset A

Il dataset principale impiegato nelle milestone successive è:

- [`storm_m1_dataset_sonarcloud.csv`](storm_m1_dataset_sonarcloud.csv)

In questa variante `NSMELLS` deriva dalla metrica SonarCloud `code_smells`.

È mantenuta anche la variante alternativa:

- [`storm_m1_dataset.csv`](storm_m1_dataset.csv)

nella quale `NSMELLS` è ottenuto tramite PMD.

## Artefatti M1

Nella directory principale sono conservati anche gli artefatti utilizzati per costruire e verificare il Dataset A, tra cui catalogo delle release, inventario delle classi, metriche LOC e storiche, informazioni sui defect, fix commit, risultati SZZ, labeling e misure degli smell.

La metodologia di costruzione è documentata in [`../docs/m1-dataset.md`](../docs/m1-dataset.md).

## Milestone 3

La sottocartella [`m3/`](m3/) contiene dataset e artefatti di supporto per l'analisi what-if.

## Dataset di supporto al testing

La sottocartella [`testing/`](testing/) contiene dataset impiegati nella selezione e nell'analisi delle classi target della parte di testing.

Per distinguere dataset, risultati e artefatti canonici consultare [`../ARTIFACTS.md`](../ARTIFACTS.md).

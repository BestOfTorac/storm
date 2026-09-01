# Documentazione metodologica

La directory `isw2/docs/` raccoglie la **documentazione metodologica del progetto**.

Questa cartella non contiene principalmente risultati numerici: il suo obiettivo è spiegare **come** i risultati sono stati ottenuti, quali decisioni sono state prese, quali criteri sono stati utilizzati e quali controlli sono stati introdotti per mantenere il processo riproducibile.

È quindi il punto di riferimento corretto quando si vuole comprendere una pipeline senza dover ricostruire il metodo direttamente dal codice.

---

## 1. Ruolo della cartella

La documentazione è organizzata secondo l'evoluzione naturale del progetto:

1. costruzione del dataset;
2. addestramento e valutazione dei classificatori;
3. analisi what-if;
4. selezione delle classi e sperimentazione di testing;
5. protocollo di misura M4.

I documenti descrivono sia il **metodo previsto** sia le decisioni specifiche necessarie per applicarlo ad Apache Storm.

---

# Milestone 1

## 2. Selezione delle release

[`release-selection.md`](release-selection.md)

Documenta:

- criteri utilizzati per individuare le release;
- ordine temporale;
- selezione del sottoinsieme utilizzato per il dataset;
- relazione con il catalogo delle release.

È il documento da consultare quando si vuole capire perché una determinata versione di Storm compare o non compare nel dataset.

---

## 3. Selezione dei sorgenti

[`source-selection.md`](source-selection.md)

Definisce quali file Java vengono considerati production code e quali elementi devono essere esclusi dall'analisi.

Questa distinzione è necessaria per evitare che test, esempi o codice non rilevante alterino le metriche a livello di classe.

---

## 4. Definizione delle metriche

[`metrics-definition.md`](metrics-definition.md)

Descrive le feature raccolte nel Dataset A e il loro significato.

Il documento deve essere utilizzato insieme alle colonne del dataset per interpretare correttamente le variabili dimensionali, storiche e di processo.

---

## 5. Identificazione dei defect e dei fix

[`defect-fix-identification.md`](defect-fix-identification.md)

Descrive come vengono individuati:

- ticket associati a bug;
- fix commit;
- relazioni tra issue tracker e repository Git.

È il primo passaggio necessario alla successiva esecuzione di SZZ.

---

## 6. Ciclo di vita dei defect

[`defect-lifecycle.md`](defect-lifecycle.md)

Documenta come le informazioni temporali relative ai defect vengono integrate nella ricostruzione storica del progetto.

---

## 7. SZZ

[`szz-analysis.md`](szz-analysis.md)

Descrive il procedimento utilizzato per risalire dai fix commit ai commit potenzialmente bug-introducing.

Il risultato di SZZ è successivamente utilizzato per determinare l'intervallo di release nel quale una classe deve essere considerata buggy.

---

## 8. Buggy labeling

[`buggy-labeling.md`](buggy-labeling.md)

Descrive il processo con cui viene costruita la label `BUGGY`.

Il documento è particolarmente importante perché collega le informazioni sui defect con le osservazioni classe-release del Dataset A.

---

## 9. Code smell

Sono mantenute due pipeline di static analysis.

### PMD

[`pmd-smells.md`](pmd-smells.md)

Documenta la misurazione degli smell mediante PMD.

### SonarCloud

[`sonarcloud-smells.md`](sonarcloud-smells.md)

Documenta la misurazione mediante SonarCloud.

La variante SonarCloud è quella utilizzata come Dataset A principale nelle milestone successive.

---

## 10. Assemblaggio del Dataset A

[`m1-dataset.md`](m1-dataset.md)

È il documento di sintesi della Milestone 1.

Spiega come gli output delle pipeline precedenti vengono combinati in una singola osservazione classe-release e quali controlli vengono eseguiti prima di considerare il dataset concluso.

Per vedere i file prodotti dalla pipeline consultare:

[`../datasets/README.md`](../datasets/README.md)

---

# Milestone 2

## 11. Classificatori

[`m2-classifiers.md`](m2-classifiers.md)

Documenta:

- classificatori valutati;
- validazione;
- feature selection;
- balancing;
- prevenzione del leakage;
- metriche;
- selezione del modello utilizzato successivamente.

I risultati quantitativi sono conservati in:

[`../results/m2/`](../results/m2/)

---

# Milestone 3

## 12. What-if analysis

[`m3-what-if.md`](m3-what-if.md)

Descrive:

- costruzione di A, B+, B e C;
- trasformazione controfattuale di `NSMELLS`;
- applicazione del classificatore selezionato;
- interpretazione delle transizioni;
- limiti causali dell'esperimento.

I risultati sono disponibili in:

[`../results/m3/`](../results/m3/)

---

# Testing e Milestone 4

## 13. Selezione delle classi target

[`testing-class-selection.md`](testing-class-selection.md)

Documenta la selezione iniziale delle classi considerate per il testing.

---

## 14. Rivalutazione della seconda classe

[`testing-second-class-reassessment.md`](testing-second-class-reassessment.md)

Documenta perché una prima classe candidata non è stata mantenuta come target finale e come si è arrivati alla selezione di `RedisFilterBolt`.

Il materiale relativo alla classe scartata è conservato in archivio, non eliminato.

---

## 15. Protocollo delle suite automatiche M4

La sottocartella [`testing/`](testing/) contiene la documentazione metodologica specifica della matrice automatica.

Il documento principale è:

[`testing/m4-automatic-suite-measurement-contract.md`](testing/m4-automatic-suite-measurement-contract.md)

Il protocollo è stato congelato prima delle misure conclusive per evitare adattamenti retroattivi delle suite sulla base dei risultati.

---

## 16. Come utilizzare questa cartella

Per ricostruire una milestone, utilizzare la documentazione in questa directory insieme al relativo output:

- M1: `docs/` + [`../datasets/`](../datasets/README.md)
- M2: `docs/m2-classifiers.md` + [`../results/m2/`](../results/m2/)
- M3: `docs/m3-what-if.md` + [`../results/m3/`](../results/m3/)
- M4 Falessi: documentazione + [`../results/m4/`](../results/m4/)
- De Angelis: documentazione testing + [`../testing/`](../testing/README.md)

Per identificare il file conclusivo di ogni fase consultare sempre:

[`../ARTIFACTS.md`](../ARTIFACTS.md)

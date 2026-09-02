# Report finali

La directory `isw2/reports/` è destinata alla documentazione accademica conclusiva del progetto.

I report vengono mantenuti separati dalla documentazione metodologica presente in `docs/` perché hanno una funzione differente:

- `docs/` descrive in dettaglio come sono state realizzate le singole attività;
- `reports/` sintetizza metodologia, risultati, interpretazione e conclusioni nella forma richiesta per la consegna.

---

## 1. Report Falessi

Il primo report documenta l'intero percorso M1-M4.

La struttura di riferimento è:

1. Introduzione;
2. Metodologia;
3. Risultati;
4. Threats to validity;
5. Conclusioni.

La metodologia e i risultati devono seguire le quattro milestone:

### M1

Costruzione del Dataset A.

### M2

Valutazione dei classificatori.

### M3

Analisi what-if sulla rimozione degli smell.

### M4

Valutazione delle varianti refactorizzate.

Le conclusioni devono discutere sia l'impatto per il practitioner sia le implicazioni dal punto di vista della ricerca.

---

## 2. Report De Angelis

Il secondo report riguarda la sperimentazione di testing.

Deve descrivere:

- classi target;
- progettazione T_BB;
- evoluzione T_CF;
- mutation-guided testing;
- Randoop;
- EvoSuite;
- generazione LLM;
- coverage;
- mutation testing;
- reliability;
- refactoring C0-C4;
- qualità strutturale;
- Sonar;
- behavior preservation;
- confronto finale tra le tecniche.

Il corpo principale deve rimanere leggibile e interpretativo.

Le tabelle possono raccogliere i dettagli quantitativi, ma nel testo devono essere richiamate esplicitamente e discusse.

---

## 3. Source of truth

I report non devono essere compilati leggendo casualmente gli output presenti nel repository.

Il riferimento principale è:

[`../ARTIFACTS.md`](../ARTIFACTS.md)

Questo permette di evitare errori dovuti a:

- campagne intermedie;
- risultati superati;
- log diagnostici;
- file storici.

---

## 4. Regole di interpretazione importanti

Nella redazione devono essere preservate alcune distinzioni.

### PIT

Le unità `N/A` non devono essere trasformate in zero.

### Reliability

`R_hat = 1` nel profilo osservato non significa affidabilità assoluta in produzione.

### Sonar

Le misure Sonar del generated test code non devono essere confuse con quelle del production code.

### RedisFilterBolt

Non deve essere documentata una T_MT inesistente.

### UIHelpers C1

Il fallimento osservato nella verifica post-hoc deve rimanere visibile.

### Refactoring

Le varianti `C1-C4` non costituiscono necessariamente una progressione monotona di qualità.

---

## 5. Relazione con il repository

Per approfondire un'affermazione presente nei report è possibile seguire la catena:

`report` → `ARTIFACTS.md` → risultato canonico → evidence → metodologia

Questa struttura permette di mantenere il documento finale sintetico senza perdere la possibilità di verificare ogni dato.

---

## 6. Stato della cartella

L'audit incrociato finale è stato completato. I documenti definitivi versionati in questa directory sono:

- [`Torac_Valerio_Apache_Storm_Falessi.pdf`](Torac_Valerio_Apache_Storm_Falessi.pdf) — report finale del percorso Falessi, Milestone M1-M4;
- [`Torac_Valerio_Report_SoftwareTesting_STORM.pdf`](Torac_Valerio_Report_SoftwareTesting_STORM.pdf) — report finale del percorso Software Testing / De Angelis.

I report fanno riferimento agli artefatti canonici indicati in [`../ARTIFACTS.md`](../ARTIFACTS.md) e al branch `isw2-project`.
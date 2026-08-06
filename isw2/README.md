# Progetto ISW2 – Apache Storm

Questo repository contiene il progetto svolto per il corso di
Ingegneria del Software 2 dell'Università degli Studi di Roma Tor Vergata.

## Progetto analizzato

- Progetto: Apache Storm
- Release di riferimento: 3.0.0
- Tag originale: v3.0.0
- Branch della baseline: baseline-v3.0.0
- Branch di sviluppo: isw2-project

## Struttura

- `analyzer`: analisi delle release, creazione del dataset e classificazione
- `config`: configurazioni del progetto
- `datasets`: dataset prodotti durante gli esperimenti
- `docs`: protocollo sperimentale e documentazione
- `results`: risultati delle analisi
- `testing`: artefatti relativi alla sperimentazione dei test

## Milestone

1. Creazione del dataset
2. Valutazione dei classificatori
3. Analisi what-if con NSmells pari a zero
4. Caratterizzazione del refactoring automatico
5. Sperimentazione delle tecniche di software testing

## Ambiente verificato

- Sistema operativo: Windows 11
- Java: Microsoft OpenJDK 25.0.4
- Maven: 3.9.16
- Apache Storm: 3.0.0

La baseline è stata compilata con successo tramite Maven.
I test nativi di Apache Storm non fanno parte della sperimentazione sui test
e saranno esclusi dalle misurazioni effettuate per il progetto.

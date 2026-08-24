# Amendment 001 - Controlled rerun after external grounding

## Data

2026-08-24T13:21:22+02:00

## Stato

Applicato prima del secondo tentativo di generazione valido.

## Evento

Il primo tentativo di generazione LLM ha prodotto una risposta completa ma Microsoft 365 Copilot ha consultato autonomamente una pagina Web relativa ad AbstractRedisBolt di Apache Storm 2.7.1.

Questo comportamento non rispetta il controllo del contesto previsto dall'esperimento.

## Decisione

Il tentativo 01 viene conservato integralmente come evidence ma non viene utilizzato come T_LLM_BASELINE.

Viene eseguito un nuovo tentativo in una conversazione Microsoft 365 Copilot vuota.

Per il secondo tentativo vengono forniti esclusivamente:

1. RedisFilterBolt.java
2. AbstractRedisBolt.java
3. isw2/testing/pom.xml

Il nuovo prompt vieta esplicitamente la consultazione di fonti esterne e richiede di chiedere eventuale production context mancante invece di ricercarlo autonomamente.

## Vincoli invariati

La cardinalita rimane N = 11.

Non viene fornita alcuna informazione derivata dalle suite BB, CF, RND o EvoSuite, ne da JaCoCo o PIT.

Il secondo tentativo non utilizza i casi di test prodotti dal tentativo 01 come contesto.

## LLM

Provider:

Microsoft 365 Copilot

Accesso:

WEB

Model/mode label:

M365 Copilot, basato sul modello di ragionamento GPT-5

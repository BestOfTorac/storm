# P1 attempt 01 - invalid external grounding

## Status

INVALID FOR T_LLM_BASELINE

## Timestamp registrazione

`2026-08-24T13:18:40+02:00`

## Repository commit

`ac000df1ca451bced2fcbd88ed47791a376d19d5`

## Motivo dello scarto

La risposta di Microsoft 365 Copilot ha utilizzato autonomamente una fonte esterna relativa alla documentazione di AbstractRedisBolt di Apache Storm 2.7.1.

Il protocollo T_LLM richiede invece che la generazione della baseline sia basata sul production context intenzionalmente fornito durante l'esperimento.

Non è stato osservato uso intenzionale di:

- test BB;
- test CF;
- test RND;
- test EvoSuite;
- risultati JaCoCo;
- risultati PIT;
- mutation survivors.

L'invalidazione riguarda quindi il controllo del contesto e non una contaminazione derivante dalle suite sperimentali precedenti.

## Ulteriore osservazione

La risposta afferma che anche un'eccezione durante filterMapper.getKeyFromTuple(input) sarebbe gestita dal blocco catch di process.

Nel production source Apache Storm 3.0.0 la chiamata a getKeyFromTuple precede invece il blocco try.

## Decisione

Questo tentativo viene conservato esclusivamente come interaction evidence e non viene utilizzato come T_LLM_BASELINE.

Il successivo tentativo verrà eseguito in una nuova conversazione fornendo anche AbstractRedisBolt.java come production context controllato.

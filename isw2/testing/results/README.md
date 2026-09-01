# Risultati della sperimentazione di testing

Questa cartella contiene le evidence prodotte durante la sperimentazione sulle due classi target e durante la valutazione delle suite automatiche.

## UIHelpers

[`uihelpers/`](uihelpers/) contiene risultati, evidence e indici relativi a `org.apache.storm.daemon.ui.UIHelpers`.

Tra i risultati finali deve essere preservata anche l'anomalia osservata sulla variante C1: la verifica `mt06CorsConfigurationParametersAreObservable` non passa nel set `T_MT` post-hoc. Tale esito non viene corretto retroattivamente.

## RedisFilterBolt

[`redis-filter-bolt/`](redis-filter-bolt/) contiene risultati ed evidence relativi a `org.apache.storm.redis.bolt.RedisFilterBolt`.

Il set manuale finale eseguibile comprende 15 test. Non è presente un `T_MT` dedicato perché il survivor residuo è stato classificato come equivalente/ridondante rispetto al comportamento osservabile.

## Matrice automatica M4

[`m4-generated/`](m4-generated/) contiene coverage, mutation testing, analisi strutturale, Sonar, behavior preservation e consolidamenti delle 30 unità sperimentali.

Il riferimento principale per il report è:

[`m4-generated/final-deangelis-consolidation/`](m4-generated/final-deangelis-consolidation/)

Per PIT:

- 20 unità sono `MEASURED`;
- 10 unità sono `N/A`;
- `N/A` non equivale a mutation score zero.

Le suite generate sono congelate: non vengono riparate o rigenerate per rendere artificialmente misurabile una configurazione.

## Continuous Integration

[`ci/`](ci/) contiene evidence relative alla CI e alla sua validazione.

Per l'elenco puntuale degli artefatti canonici consultare [`../../ARTIFACTS.md`](../../ARTIFACTS.md).

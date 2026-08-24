# RedisFilterBolt T_LLM - P1 context manifest

## Esperimento

Target:

`org.apache.storm.redis.bolt.RedisFilterBolt`

Suite:

`T_LLM`

Cardinalità baseline:

`N = 11`

Commit di baseline del repository:

`89ad95215fd3f9ebeca6597742f6e5d0f1c989c0`

Branch:

`isw2-project`

## LLM

Provider:

`Microsoft 365 Copilot`

Modalità di accesso:

`WEB`

Etichetta modello/modalità mostrata dall'interfaccia:

`DA REGISTRARE PRIMA DI P1`

Timestamp interazione P1:

`DA REGISTRARE PRIMA DI P1`

## Contesto iniziale intenzionalmente fornito a P1

1. `external/storm-redis/src/main/java/org/apache/storm/redis/bolt/RedisFilterBolt.java`
2. `isw2/testing/pom.xml`

## Eventuale contesto production aggiuntivo

Nessuno al momento della preregistrazione.

Se Copilot richiede una firma o una classe production necessaria per comprendere o compilare correttamente i test, ogni artefatto aggiuntivo fornito dovrà essere registrato qui.

## Contesto intenzionalmente escluso prima del freeze

Non vengono forniti a Copilot:

- test BB di RedisFilterBolt;
- test CF;
- test RND;
- test EvoSuite;
- test nativi di Apache Storm;
- risultati JaCoCo;
- risultati PIT;
- survivor del mutation testing;
- confronti di coverage o mutation score;
- analisi dei gap individuati dalle suite precedenti.

L'obiettivo è mantenere T_LLM indipendente dalle altre tecniche di testing.

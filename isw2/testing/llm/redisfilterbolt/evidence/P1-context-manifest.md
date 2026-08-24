# RedisFilterBolt T_LLM - context manifest

## Esperimento

Target:

org.apache.storm.redis.bolt.RedisFilterBolt

Suite:

T_LLM

Cardinalita baseline:

N = 11

Branch:

isw2-project

Preregistration commit:

ac000df1ca451bced2fcbd88ed47791a376d19d5

## LLM

Provider:

Microsoft 365 Copilot

Modalita di accesso:

WEB

Etichetta modello/modalita mostrata dall'interfaccia:



## Attempt 01

Prompt:

P1-effective-analysis-and-generation.txt

Production context fornito:

1. external/storm-redis/src/main/java/org/apache/storm/redis/bolt/RedisFilterBolt.java
2. isw2/testing/pom.xml

Stato:

INVALID FOR T_LLM_BASELINE

Motivo:

Microsoft 365 Copilot ha introdotto autonomamente una fonte Web esterna relativa ad AbstractRedisBolt di Apache Storm 2.7.1.

Il timestamp esatto della prima interazione non era stato registrato prima dell'invio e non viene ricostruito retroattivamente.

La risposta completa e l'evidence dell'invalidazione sono conservate nel repository.

## Attempt 02

Stato:

PREPARED - NOT YET SUBMITTED

Prompt:

P1-attempt-02-controlled-context.txt

Production context intenzionalmente fornito:

1. external/storm-redis/src/main/java/org/apache/storm/redis/bolt/RedisFilterBolt.java
2. external/storm-redis/src/main/java/org/apache/storm/redis/bolt/AbstractRedisBolt.java
3. isw2/testing/pom.xml

AbstractRedisBolt.java viene aggiunto come production context controllato per evitare la necessita di consultare documentazione esterna sulla superclasse.

Timestamp preparazione:

2026-08-24T13:20:55+02:00

Repository HEAD prima della preparazione:

ac000df1ca451bced2fcbd88ed47791a376d19d5

## Regola per eventuale contesto aggiuntivo

Se Copilot dichiara che manca una classe o una firma production necessaria, tale artefatto potra essere fornito in un'interazione successiva e dovra essere registrato nel manifest.

## Contesto sperimentale escluso dalla baseline

Non viene fornito a Copilot materiale proveniente dalle suite sperimentali precedenti o dalle successive misure di adequacy.

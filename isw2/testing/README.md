# Sperimentazione di testing

Questa cartella contiene gli artefatti relativi al percorso di testing del progetto.

## Classi target definitive

Il file [`classes.txt`](classes.txt) identifica le due classi finali:

- `org.apache.storm.daemon.ui.UIHelpers`
- `org.apache.storm.redis.bolt.RedisFilterBolt`

La selezione è documentata in:

- [`../docs/testing-class-selection.md`](../docs/testing-class-selection.md)
- [`../docs/testing-second-class-reassessment.md`](../docs/testing-second-class-reassessment.md)

## Testing manuale

Le suite manuali sono sviluppate sotto [`src/test/java/`](src/test/java/) senza utilizzare i test nativi di Apache Storm come test del progetto.

Le categorie principali sono:

- `T_BB`: black-box / Category Partition;
- `T_CF`: control-flow;
- `T_MT`: mutation-guided, quando giustificato.

Per `RedisFilterBolt` non è stata introdotta una suite `T_MT` dedicata perché il survivor residuo dopo `T_BB + T_CF` è stato classificato come equivalente/ridondante rispetto al comportamento osservabile.

## Suite automatiche

- [`evosuite/`](evosuite/): generazione e supporto EvoSuite.
- [`llm/`](llm/): protocolli ed evidence della generazione LLM.
- [`generated-tests/`](generated-tests/): suite generate e congelate per la matrice M4.
- [`specs/`](specs/): specifiche e materiali di supporto.

Le tecniche confrontate sono Randoop (`T_RND`), EvoSuite (`T_ES`) e LLM (`T_LLM`).

## Risultati

Le evidence sperimentali sono indicizzate in [`results/README.md`](results/README.md).

Il consolidamento finale della matrice 2 classi × 5 varianti × 3 tecniche si trova sotto:

[`results/m4-generated/final-deangelis-consolidation/`](results/m4-generated/final-deangelis-consolidation/)

## Record storici

Alcuni README nelle sottocartelle descrivono lo stato di una singola fase sperimentale e vengono mantenuti invariati per preservarne la tracciabilità. Espressioni temporali presenti in tali documenti devono quindi essere interpretate nel contesto della fase a cui appartengono.

Lo stato conclusivo è rappresentato dagli artefatti elencati in [`../ARTIFACTS.md`](../ARTIFACTS.md).

## Archivio

Il materiale relativo a candidati analizzati ma non appartenenti al set finale è conservato in [`archive/`](archive/).

## Harness

Il file [`pom.xml`](pom.xml) definisce il testing harness dedicato del progetto. La CI associata è implementata nel workflow `.github/workflows/isw2-testing.yml` alla radice del repository.

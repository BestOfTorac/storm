# Documentazione metodologica

Questa cartella raccoglie la documentazione metodologica del progetto ISW2. I file descrivono le decisioni, i protocolli e le procedure impiegate nelle diverse milestone.

## Milestone 1

- [`release-selection.md`](release-selection.md): selezione delle release.
- [`source-selection.md`](source-selection.md): selezione dei sorgenti Java di produzione.
- [`metrics-definition.md`](metrics-definition.md): definizione delle metriche.
- [`defect-fix-identification.md`](defect-fix-identification.md): identificazione dei fix commit.
- [`defect-lifecycle.md`](defect-lifecycle.md): ricostruzione del ciclo di vita dei defect.
- [`szz-analysis.md`](szz-analysis.md): procedura SZZ.
- [`buggy-labeling.md`](buggy-labeling.md): costruzione del labeling `BUGGY`.
- [`pmd-smells.md`](pmd-smells.md): analisi degli smell con PMD.
- [`sonarcloud-smells.md`](sonarcloud-smells.md): analisi degli smell con SonarCloud.
- [`m1-dataset.md`](m1-dataset.md): assemblaggio e validazione del Dataset A.

## Milestone 2

- [`m2-classifiers.md`](m2-classifiers.md): protocollo di classificazione, validazione, feature selection e bilanciamento.

## Milestone 3

- [`m3-what-if.md`](m3-what-if.md): costruzione e valutazione dello scenario controfattuale con `NSMELLS = 0`.

## Testing e Milestone 4

- [`testing-class-selection.md`](testing-class-selection.md): selezione iniziale delle classi target.
- [`testing-second-class-reassessment.md`](testing-second-class-reassessment.md): rivalutazione della seconda classe e scelta finale di `RedisFilterBolt`.
- [`testing/m4-automatic-suite-measurement-contract.md`](testing/m4-automatic-suite-measurement-contract.md): protocollo congelato di misura delle suite automatiche sulle varianti `C0`-`C4`.

Per gli output quantitativi consultare [`../results/README.md`](../results/README.md). Per le evidence di testing consultare [`../testing/README.md`](../testing/README.md).

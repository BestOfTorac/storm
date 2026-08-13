# Milestone 2 - Classifier Evaluation

## Objective

Milestone 2 evaluates defect-prediction classifiers on the Dataset A produced
during Milestone 1 for Apache Storm.

The experiment compares the classifiers required by the course:

- Random Forest
- Naive Bayes
- IBk

The evaluation considers both feature selection and class balancing, producing
the four configurations:

| Feature Selection | Balancing |
|---|---|
| No | No |
| Yes | No |
| No | Yes |
| Yes | Yes |

The evaluated metrics are:

- Precision
- Recall
- AUC
- Cohen's Kappa
- NPofB20

## Dataset

The input dataset is:

`isw2/datasets/storm_m1_dataset_sonarcloud.csv`

It contains 14,611 observations and 24 predictors.

The prediction target is `BUGGY`, with `YES` representing the positive class.

Identifier attributes are excluded from the modeling dataset.

## Validation protocol

The experiment uses repeated stratified cross-validation:

- 10 repetitions
- 10 folds per repetition
- deterministic seeds from 1 to 10
- identical fold assignments for every classifier and preprocessing
  configuration

Each repetition therefore produces one complete set of out-of-fold predictions
for every classifier/configuration pair.

Every observation is used exactly once as test data in each repetition.

The final experiment contains:

- 4 preprocessing configurations
- 3 classifiers
- 10 repetitions
- 120 aggregated metric rows

Each aggregated metric row is computed from 14,611 out-of-fold predictions.

## Leakage prevention

All supervised preprocessing is performed inside each training fold.

For every fold:

1. the original dataset is split into training and test partitions;
2. when enabled, feature selection is fitted only on the training partition;
3. the fitted feature-selection transformation is then applied to the test
   partition;
4. when enabled, SMOTE is applied only to the training partition;
5. the classifier is trained on the resulting training data;
6. evaluation is performed on the untouched test observations, except for the
   feature transformation learned from the training fold.

SMOTE is never applied to test data.

This prevents information from the test fold from influencing preprocessing or
training.

## Feature selection

Feature selection uses the WEKA supervised `AttributeSelection` filter with:

- `CfsSubsetEval`
- `BestFirst`

The feature-selection model is independently fitted on every training fold.

Across the 100 training folds:

- minimum selected predictors: 6
- mean selected predictors: 10.63
- maximum selected predictors: 14

The most stable selected predictors are:

| Feature | Selected folds |
|---|---:|
| LOC | 100 / 100 |
| NFIX | 100 / 100 |
| AVG_CHANGE_SET_SIZE | 100 / 100 |
| AVG_ENTROPY | 100 / 100 |
| NSMELLS | 97 / 100 |
| MAX_CHURN | 95 / 100 |
| CHANGE_SET_SIZE | 89 / 100 |
| AVG_LOC_DELETED | 84 / 100 |
| MAX_ENTROPY | 81 / 100 |
| MAX_LOC_ADDED | 81 / 100 |

The complete feature-selection frequencies are available in:

`isw2/results/m2/feature_selection_summary.csv`

## Balancing

Class balancing uses WEKA SMOTE.

SMOTE is fitted and applied only to the training fold and increases the
minority `BUGGY=YES` class until the training classes are approximately
balanced.

The original test-class distribution is preserved.

## Results

Mean values over the 10 repetitions are reported below.

| Classifier | FS | Balancing | Precision | Recall | AUC | Kappa | NPofB20 |
|---|---|---|---:|---:|---:|---:|---:|
| RandomForest | No | No | 0.870054 | 0.786967 | 0.983134 | 0.811149 | 0.867739 |
| RandomForest | Yes | No | 0.840120 | 0.765004 | 0.974580 | 0.783204 | 0.855028 |
| RandomForest | No | Yes | 0.829856 | 0.838858 | 0.983270 | 0.818830 | 0.852615 |
| RandomForest | Yes | Yes | 0.767252 | 0.818504 | 0.974223 | 0.772009 | 0.842961 |
| NaiveBayes | No | No | 0.273251 | 0.316492 | 0.767368 | 0.222271 | 0.193725 |
| NaiveBayes | Yes | No | 0.282479 | 0.345615 | 0.754400 | 0.239689 | 0.224135 |
| NaiveBayes | No | Yes | 0.268978 | 0.351488 | 0.768617 | 0.230586 | 0.219308 |
| NaiveBayes | Yes | Yes | 0.253237 | 0.400241 | 0.756744 | 0.229955 | 0.249236 |
| IBk | No | No | 0.735881 | 0.703218 | 0.843978 | 0.693686 | 0.705873 |
| IBk | Yes | No | 0.757709 | 0.762430 | 0.934246 | 0.737639 | 0.790507 |
| IBk | No | Yes | 0.641038 | 0.788496 | 0.877561 | 0.676810 | 0.771923 |
| IBk | Yes | Yes | 0.665518 | 0.819308 | 0.942392 | 0.706903 | 0.813757 |

The complete means and standard deviations are available in:

`isw2/results/m2/classifier_summary.csv`

## Effect of feature selection and balancing

For Random Forest, feature selection does not improve the evaluated metrics in
this experiment.

Without balancing, Random Forest achieves the highest Precision and NPofB20:

- Precision: 0.870054
- NPofB20: 0.867739

With balancing and without feature selection, Random Forest achieves its
highest Recall, AUC, and Kappa:

- Recall: 0.838858
- AUC: 0.983270
- Kappa: 0.818830

SMOTE therefore produces a trade-off: it improves the ability to identify
buggy classes while reducing Precision and NPofB20.

## BClassifier selection

The classifier to carry forward to the following milestone is
**Random Forest**.

For every combination of feature selection and balancing, the classifier with
the highest mean value was identified separately for each of the five metrics.

This produces 20 metric/configuration comparisons.

Random Forest obtains:

- 19 wins out of 20 comparisons

IBk obtains:

- 1 win out of 20 comparisons

The only comparison not won by Random Forest is Recall with both feature
selection and balancing enabled:

- IBk Recall: 0.819308
- Random Forest Recall: 0.818504

The difference is approximately 0.0008.

Random Forest therefore shows the strongest and most consistent performance
across preprocessing configurations and evaluation metrics and is selected as
the M2 `BClassifier`.

The machine-readable selection is stored in:

`isw2/results/m2/best_classifier.csv`

## Generated artifacts

The final Milestone 2 results are:

- `isw2/results/m2/classifier_metrics.csv`
- `isw2/results/m2/feature_selection.csv`
- `isw2/results/m2/classifier_summary.csv`
- `isw2/results/m2/best_classifier.csv`
- `isw2/results/m2/feature_selection_summary.csv`

The first two files contain the raw experiment output, while the remaining
files contain the aggregated analysis used for the final M2 conclusions.

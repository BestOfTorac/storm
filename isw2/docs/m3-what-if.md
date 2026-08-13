# Milestone 3 - What-If Analysis

## Objective

Milestone 3 investigates the following counterfactual question:

> What would the estimated defectiveness of Apache Storm classes be if code
> smells were absent?

The experiment follows the What-If procedure defined for the course.

The classifier selected during Milestone 2 is used as the BClassifier.

For Apache Storm, the selected BClassifier is:

**Random Forest**

## Input dataset

The experiment uses the Dataset A generated during Milestone 1:

`isw2/datasets/storm_m1_dataset_sonarcloud.csv`

Dataset A contains:

- 14,611 observations;
- 24 numeric predictors;
- `BUGGY` as target variable;
- 1,243 observations with `BUGGY=YES`;
- 13,368 observations with `BUGGY=NO`.

The code-smell metric used by the What-If experiment is:

`NSMELLS`

## Dataset construction

Starting from Dataset A, three datasets are defined.

### B+

B+ contains all observations from A for which:

`NSMELLS > 0`

B+ contains:

- 5,338 observations;
- 610 observations with `BUGGY=YES`.

### C

C contains all observations from A for which:

`NSMELLS = 0`

C contains:

- 9,273 observations;
- 633 observations with `BUGGY=YES`.

Therefore:

`A = B+ union C`

and:

`610 + 633 = 1,243`

### B

B is the counterfactual version of B+.

Every observation in B is identical to the corresponding observation in B+
except for one attribute:

`NSMELLS = 0`

The original `BUGGY` value is preserved only as a reference for paired
analysis. It must not be interpreted as an observed actual value for the
counterfactual scenario.

B contains 5,338 observations.

## Counterfactual dataset validation

The B+/B transformation was validated row by row and attribute by attribute.

The final validation confirmed:

- B+ rows: 5,338;
- B rows: 5,338;
- C rows: 9,273;
- every B+ observation has `NSMELLS > 0`;
- every B observation has `NSMELLS = 0`;
- every C observation has `NSMELLS = 0`;
- B and B+ differ in exactly 5,338 `NSMELLS` cells;
- B and B+ have zero differences in all other attributes.

The M3 CSV writer preserves the original numeric values exactly.

An independent round-trip audit between Dataset A and `B+ union C` found:

- missing observation keys: 0;
- numeric predictor differences: 0;
- maximum numeric difference: 0.

This ensures that unchanged observations are numerically identical to their
original representation in Dataset A.

## BClassifierA

Milestone 2 selected Random Forest as the BClassifier.

For Milestone 3, a Random Forest classifier is trained on the complete Dataset
A. This trained model is referred to as:

`BClassifierA`

The classifier uses:

- Random Forest;
- all 24 predictors;
- no feature selection;
- no class balancing;
- the same default WEKA Random Forest configuration used during Milestone 2.

The model is then used to predict:

- A;
- B+;
- B;
- C.

The `BUGGY` class attribute is explicitly hidden from the classifier during
prediction.

## Prediction results

The final prediction table is:

| Dataset | Actual BUGGY | Estimated BUGGY |
|---|---:|---:|
| A | 1,243 | 1,241 |
| B+ | 610 | 609 |
| B | N/A | 586 |
| C | 633 | 632 |

The estimated values satisfy the expected partition invariant:

`Estimated(B+) + Estimated(C) = Estimated(A)`

that is:

`609 + 632 = 1,241`

## Official What-If result

Following the What-If procedure used in the course material, the estimated
number of buggy observations that could have been prevented is computed as:

`ActualBuggy(B+) - EstimatedBuggy(B)`

For Apache Storm:

`610 - 586 = 24`

Therefore, the experiment estimates that:

**24 buggy observations could have been prevented under the synthetic
no-smell scenario.**

Relative to all actually buggy observations containing at least one smell:

`24 / 610 * 100 = 3.9344%`

Relative to all buggy observations in Dataset A:

`24 / 1243 * 100 = 1.9308%`

The main Milestone 3 result is therefore:

- estimated prevented buggy observations: **24**;
- reduction among buggy observations with smells: **3.9344%**;
- reduction over all buggy observations: **1.9308%**.

## Pairwise B+ -> B analysis

In addition to the aggregate What-If calculation, the experiment compares each
B+ prediction with its corresponding B prediction.

The transition matrix is:

| Prediction in B+ | Prediction in B | Observations |
|---|---|---:|
| YES | YES | 581 |
| YES | NO | 28 |
| NO | YES | 5 |
| NO | NO | 4,724 |

The four transition groups sum to all 5,338 counterfactual observations.

The net reduction in predicted buggy observations is:

`609 - 586 = 23`

This can also be obtained from:

`28 YES->NO - 5 NO->YES = 23`

Additionally, among the 610 observations that were actually buggy in B+, 29
are predicted as non-buggy in the counterfactual B dataset.

These values are supplementary analyses and must not be confused with the
official aggregate What-If estimate of 24.

## Interpretation of the different quantities

Four related quantities are reported:

### 24 - official What-If estimate

`610 actual buggy in B+ - 586 estimated buggy in B`

This is the aggregate measure corresponding to the What-If procedure used in
the course material.

### 23 - net prediction reduction

`609 estimated buggy in B+ - 586 estimated buggy in B`

This measures the net change in the classifier predictions when `NSMELLS` is
changed from its original positive value to zero.

### 28 - YES to NO transitions

These are observations whose classifier decision directly changes from buggy
in B+ to non-buggy in B.

### 29 - original buggy observations predicted NO in B

These are observations with reference `BUGGY=YES` in B+ that receive a
non-buggy prediction in the synthetic B scenario.

The official M3 result remains **24** because it follows the aggregate
Actual(B+) versus Estimated(B) comparison required by the What-If procedure.

## Model accuracy

The predictions on A must not be interpreted as an independent estimate of
generalization performance because BClassifierA is trained on A itself.

The evidence that Random Forest is an appropriate BClassifier comes from
Milestone 2, where the classifiers were evaluated using repeated stratified
10x10-fold cross-validation.

Random Forest was selected as the BClassifier after obtaining the best mean
result in 19 of the 20 metric/preprocessing comparisons.

Milestone 3 therefore does not re-select or re-evaluate the classifier. It
uses the classifier selected by Milestone 2 to perform the counterfactual
experiment.

## Interpretation limits

B is a synthetic counterfactual dataset.

Setting `NSMELLS` to zero does not prove that removing code smells would
causally prevent exactly 24 defects.

The result represents the prediction of BClassifierA under a controlled
scenario in which only the smell-count feature is changed while all other
measured properties are kept constant.

The result should therefore be interpreted as a model-based estimate rather
than a causal claim.

## Generated artifacts

### Counterfactual datasets

- `isw2/datasets/m3/storm_m3_bplus.csv`
- `isw2/datasets/m3/storm_m3_b.csv`
- `isw2/datasets/m3/storm_m3_c.csv`

### Results

- `isw2/results/m3/what_if_predictions.csv`
- `isw2/results/m3/what_if_table.csv`
- `isw2/results/m3/what_if_summary.csv`
- `isw2/results/m3/what_if_transitions.csv`

### Implementation

- `M3DatasetBuilder.java`
- `M3WhatIfRunner.java`
- `M3SummaryGenerator.java`
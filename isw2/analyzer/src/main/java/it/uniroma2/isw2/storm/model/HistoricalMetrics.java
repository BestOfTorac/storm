package it.uniroma2.isw2.storm.model;

public record HistoricalMetrics(
    long locTouched,
    int revisions,
    int authors,

    long locAdded,
    int maxLocAdded,
    double avgLocAdded,

    long locDeleted,
    int maxLocDeleted,
    double avgLocDeleted,

    long churn,
    int maxChurn,
    double avgChurn,

    long changeSetSize,
    int maxChangeSet,
    double avgChangeSet,

    double avgNd,
    int maxNd,

    double avgEntropy,
    double maxEntropy,

    double ageWeeks,
    double weightedAgeWeeks
) {
}
package it.uniroma2.isw2.storm.model;

public record HistoricalMetrics(
    long locTouched,
    int revisions,
    int authors,
    long locAdded,
    int maxLocAdded,
    double avgLocAdded,
    long churn,
    int maxChurn,
    double avgChurn,
    long changeSetSize,
    int maxChangeSet,
    double avgChangeSet,
    double ageWeeks,
    double weightedAgeWeeks
) {
}
package it.uniroma2.isw2.storm.model;

public record GitJavaSourceFile(
    String filePath,
    String content
) {
}
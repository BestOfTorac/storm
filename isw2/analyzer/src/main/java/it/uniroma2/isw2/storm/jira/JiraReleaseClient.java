package it.uniroma2.isw2.storm.jira;

import it.uniroma2.isw2.storm.model.JiraReleaseInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class JiraReleaseClient {

    private static final Duration CONNECT_TIMEOUT =
        Duration.ofSeconds(20);

    private static final Duration REQUEST_TIMEOUT =
        Duration.ofSeconds(30);

    private static final String USER_AGENT =
        "ISW2-Storm-Analyzer/1.0";

    private final URI jiraBaseUri;
    private final HttpClient httpClient;

    private JiraReleaseClient(
            URI jiraBaseUri,
            HttpClient httpClient) {

        this.jiraBaseUri = Objects.requireNonNull(
            jiraBaseUri,
            "Jira base URI cannot be null."
        );

        this.httpClient = Objects.requireNonNull(
            httpClient,
            "HTTP client cannot be null."
        );
    }

    public static JiraReleaseClient apacheJira() {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        return new JiraReleaseClient(
            URI.create("https://issues.apache.org/jira/"),
            client
        );
    }

    public List<JiraReleaseInfo> fetchProjectVersions(
            String projectKey)
            throws IOException, InterruptedException {

        String normalizedProjectKey =
            normalizeProjectKey(projectKey);

        URI endpoint = jiraBaseUri.resolve(
            "rest/api/2/project/" + normalizedProjectKey
        );

        HttpRequest request = HttpRequest.newBuilder(endpoint)
            .timeout(REQUEST_TIMEOUT)
            .header("Accept", "application/json")
            .header("User-Agent", USER_AGENT)
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString(
                StandardCharsets.UTF_8
            )
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                "Jira request failed with HTTP status "
                    + response.statusCode()
                    + " for project "
                    + normalizedProjectKey
            );
        }

        return parseVersions(response.body());
    }

    private static String normalizeProjectKey(
            String projectKey) {

        Objects.requireNonNull(
            projectKey,
            "Project key cannot be null."
        );

        String normalized = projectKey
            .trim()
            .toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException(
                "Invalid Jira project key: " + projectKey
            );
        }

        return normalized;
    }

    private static List<JiraReleaseInfo> parseVersions(
            String responseBody)
            throws IOException {

        List<JiraReleaseInfo> versions = new ArrayList<>();

        try {
            JSONObject project = new JSONObject(responseBody);
            JSONArray jiraVersions =
                project.getJSONArray("versions");

            for (int index = 0;
                    index < jiraVersions.length();
                    index++) {

                JSONObject version =
                    jiraVersions.getJSONObject(index);

                String releaseDateValue =
                    version.optString("releaseDate", "");

                if (releaseDateValue.isBlank()) {
                    continue;
                }

                JiraReleaseInfo release =
                    new JiraReleaseInfo(
                        version.optString("id", ""),
                        version.optString("name", ""),
                        LocalDate.parse(releaseDateValue),
                        version.optBoolean("released", false),
                        version.optBoolean("archived", false)
                    );

                versions.add(release);
            }

        } catch (
                JSONException
                | DateTimeParseException exception) {

            throw new IOException(
                "Unable to parse the Jira versions response.",
                exception
            );
        }

        versions.sort(
            Comparator
                .comparing(JiraReleaseInfo::releaseDate)
                .thenComparing(JiraReleaseInfo::name)
                .thenComparing(JiraReleaseInfo::id)
        );

        return List.copyOf(versions);
    }
}
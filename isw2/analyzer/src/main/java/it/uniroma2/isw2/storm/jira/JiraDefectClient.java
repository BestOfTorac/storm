package it.uniroma2.isw2.storm.jira;

import it.uniroma2.isw2.storm.model.JiraDefectInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class JiraDefectClient {

    private static final Duration CONNECT_TIMEOUT =
        Duration.ofSeconds(20);

    private static final Duration REQUEST_TIMEOUT =
        Duration.ofSeconds(30);

    private static final String USER_AGENT =
        "ISW2-Storm-Analyzer/1.0";

    private static final int PAGE_SIZE = 100;

    private static final String ISSUE_FIELDS =
        String.join(
            ",",
            "summary",
            "issuetype",
            "status",
            "resolution",
            "created",
            "resolutiondate",
            "versions",
            "fixVersions"
        );

    private final URI jiraBaseUri;
    private final HttpClient httpClient;

    private JiraDefectClient(
            URI jiraBaseUri,
            HttpClient httpClient) {

        this.jiraBaseUri =
            Objects.requireNonNull(
                jiraBaseUri,
                "Jira base URI cannot be null."
            );

        this.httpClient =
            Objects.requireNonNull(
                httpClient,
                "HTTP client cannot be null."
            );
    }

    public static JiraDefectClient apacheJira() {

        HttpClient client =
            HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(
                    HttpClient.Redirect.NORMAL
                )
                .build();

        return new JiraDefectClient(
            URI.create(
                "https://issues.apache.org/jira/"
            ),
            client
        );
    }

    public List<JiraDefectInfo> fetchFixedBugs(
            String projectKey)
            throws IOException, InterruptedException {

        String normalizedProjectKey =
            normalizeProjectKey(
                projectKey
            );

        String jql =
            "project = "
                + normalizedProjectKey
                + " AND issuetype = Bug"
                + " AND status in (Closed, Resolved)"
                + " AND resolution = Fixed"
                + " ORDER BY key ASC";

        List<JiraDefectInfo> defects =
            new ArrayList<>();

        int startAt = 0;
        int total = Integer.MAX_VALUE;

        while (startAt < total) {

            SearchPage page =
                fetchPage(
                    jql,
                    startAt
                );

            defects.addAll(
                page.issues()
            );

            total =
                page.total();

            if (page.issues().isEmpty()) {
                break;
            }

            startAt +=
                page.issues().size();
        }

        defects.sort(
            Comparator
                .comparingInt(
                    (JiraDefectInfo defect) ->
                        issueNumber(
                            defect.key()
                        )
                )
                .thenComparing(
                    JiraDefectInfo::key
                )
        );

        return List.copyOf(
            defects
        );
    }

    private SearchPage fetchPage(
            String jql,
            int startAt)
            throws IOException, InterruptedException {

        String query =
            "jql="
                + encode(jql)
                + "&startAt="
                + startAt
                + "&maxResults="
                + PAGE_SIZE
                + "&fields="
                + encode(ISSUE_FIELDS);

        URI endpoint =
            jiraBaseUri.resolve(
                "rest/api/2/search?"
                    + query
            );

        HttpRequest request =
            HttpRequest.newBuilder(
                    endpoint
                )
                .timeout(
                    REQUEST_TIMEOUT
                )
                .header(
                    "Accept",
                    "application/json"
                )
                .header(
                    "User-Agent",
                    USER_AGENT
                )
                .GET()
                .build();

        HttpResponse<String> response =
            httpClient.send(
                request,
                HttpResponse.BodyHandlers
                    .ofString(
                        StandardCharsets.UTF_8
                    )
            );

        if (response.statusCode() != 200) {
            throw new IOException(
                "Jira defect request failed "
                    + "with HTTP status "
                    + response.statusCode()
                    + " at startAt="
                    + startAt
            );
        }

        return parsePage(
            response.body()
        );
    }

    private static SearchPage parsePage(
            String responseBody)
            throws IOException {

        try {
            JSONObject root =
                new JSONObject(
                    responseBody
                );

            int total =
                root.getInt(
                    "total"
                );

            JSONArray issues =
                root.getJSONArray(
                    "issues"
                );

            List<JiraDefectInfo> page =
                new ArrayList<>();

            for (
                int index = 0;
                index < issues.length();
                index++
            ) {
                JSONObject issue =
                    issues.getJSONObject(
                        index
                    );

                JSONObject fields =
                    issue.getJSONObject(
                        "fields"
                    );

                JiraDefectInfo defect =
                    new JiraDefectInfo(
                        issue.optString(
                            "id",
                            ""
                        ),
                        issue.optString(
                            "key",
                            ""
                        ),
                        readName(
                            fields,
                            "issuetype"
                        ),
                        readName(
                            fields,
                            "status"
                        ),
                        readName(
                            fields,
                            "resolution"
                        ),
                        parseTimestamp(
                            fields.optString(
                                "created",
                                ""
                            )
                        ),
                        parseNullableTimestamp(
                            fields.optString(
                                "resolutiondate",
                                ""
                            )
                        ),
                        readVersions(
                            fields.optJSONArray(
                                "versions"
                            )
                        ),
                        readVersions(
                            fields.optJSONArray(
                                "fixVersions"
                            )
                        ),
                        fields.optString(
                            "summary",
                            ""
                        ).strip()
                    );

                page.add(
                    defect
                );
            }

            return new SearchPage(
                total,
                List.copyOf(page)
            );

        } catch (
                JSONException
                | DateTimeParseException exception) {

            throw new IOException(
                "Unable to parse Jira defect response.",
                exception
            );
        }
    }

    private static String readName(
            JSONObject fields,
            String fieldName) {

        JSONObject value =
            fields.optJSONObject(
                fieldName
            );

        if (value == null) {
            return "";
        }

        return value.optString(
            "name",
            ""
        );
    }

    private static List<String> readVersions(
            JSONArray versions) {

        if (versions == null) {
            return List.of();
        }

        List<String> names =
            new ArrayList<>();

        for (
            int index = 0;
            index < versions.length();
            index++
        ) {
            JSONObject version =
                versions.optJSONObject(
                    index
                );

            if (version == null) {
                continue;
            }

            String name =
                version.optString(
                    "name",
                    ""
                ).trim();

            if (!name.isBlank()) {
                names.add(
                    name
                );
            }
        }

        return List.copyOf(
            names
        );
    }

    private static OffsetDateTime parseTimestamp(
            String value) {

        if (value == null
                || value.isBlank()) {

            throw new DateTimeParseException(
                "Missing Jira timestamp.",
                value == null ? "" : value,
                0
            );
        }

        return OffsetDateTime.parse(
            normalizeOffset(value)
        );
    }

    private static OffsetDateTime
            parseNullableTimestamp(
                String value) {

        if (value == null
                || value.isBlank()) {
            return null;
        }

        return OffsetDateTime.parse(
            normalizeOffset(value)
        );
    }

    private static String normalizeOffset(
            String value) {

        String normalized =
            value.trim();

        /*
         * Jira commonly returns:
         *
         * 2014-08-08T18:15:47.000+0000
         *
         * OffsetDateTime expects:
         *
         * 2014-08-08T18:15:47.000+00:00
         */
        if (
            normalized.matches(
                ".*[+-]\\d{4}$"
            )
        ) {
            int length =
                normalized.length();

            normalized =
                normalized.substring(
                    0,
                    length - 2
                )
                + ":"
                + normalized.substring(
                    length - 2
                );
        }

        return normalized;
    }

    private static String normalizeProjectKey(
            String projectKey) {

        Objects.requireNonNull(
            projectKey,
            "Project key cannot be null."
        );

        String normalized =
            projectKey
                .trim()
                .toUpperCase(
                    Locale.ROOT
                );

        if (
            !normalized.matches(
                "[A-Z][A-Z0-9_]*"
            )
        ) {
            throw new IllegalArgumentException(
                "Invalid Jira project key: "
                    + projectKey
            );
        }

        return normalized;
    }

    private static String encode(
            String value) {

        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
        );
    }

    private static int issueNumber(
            String issueKey) {

        if (issueKey == null) {
            return Integer.MAX_VALUE;
        }

        int separator =
            issueKey.lastIndexOf('-');

        if (
            separator < 0
                || separator
                    == issueKey.length() - 1
        ) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(
                issueKey.substring(
                    separator + 1
                )
            );

        } catch (NumberFormatException exception) {
            return Integer.MAX_VALUE;
        }
    }

    private record SearchPage(
        int total,
        List<JiraDefectInfo> issues
    ) {
    }
}
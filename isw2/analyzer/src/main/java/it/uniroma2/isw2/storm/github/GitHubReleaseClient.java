package it.uniroma2.isw2.storm.github;

import it.uniroma2.isw2.storm.model.GitHubReleaseInfo;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class GitHubReleaseClient {

    private static final Duration CONNECT_TIMEOUT =
        Duration.ofSeconds(20);

    private static final Duration REQUEST_TIMEOUT =
        Duration.ofSeconds(30);

    private static final int PAGE_SIZE = 100;

    private static final String USER_AGENT =
        "ISW2-Storm-Analyzer/1.0";

    private static final String API_VERSION =
        "2026-03-10";

    private final URI apiBaseUri;
    private final HttpClient httpClient;

    private GitHubReleaseClient(
            URI apiBaseUri,
            HttpClient httpClient) {

        this.apiBaseUri = Objects.requireNonNull(
            apiBaseUri,
            "GitHub API base URI cannot be null."
        );

        this.httpClient = Objects.requireNonNull(
            httpClient,
            "HTTP client cannot be null."
        );
    }

    public static GitHubReleaseClient publicApi() {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        return new GitHubReleaseClient(
            URI.create("https://api.github.com/"),
            client
        );
    }

    public List<GitHubReleaseInfo> fetchRepositoryReleases(
            String owner,
            String repositoryName)
            throws IOException, InterruptedException {

        String normalizedOwner =
            validateRepositoryComponent(
                owner,
                "owner"
            );

        String normalizedRepository =
            validateRepositoryComponent(
                repositoryName,
                "repository"
            );

        List<GitHubReleaseInfo> releases =
            new ArrayList<>();

        int page = 1;

        while (true) {
            URI endpoint = apiBaseUri.resolve(
                "repos/"
                    + normalizedOwner
                    + "/"
                    + normalizedRepository
                    + "/releases?per_page="
                    + PAGE_SIZE
                    + "&page="
                    + page
            );

            HttpRequest request =
                HttpRequest.newBuilder(endpoint)
                    .timeout(REQUEST_TIMEOUT)
                    .header(
                        "Accept",
                        "application/vnd.github+json"
                    )
                    .header(
                        "X-GitHub-Api-Version",
                        API_VERSION
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
                    HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                    )
                );

            checkResponse(
                response,
                normalizedOwner,
                normalizedRepository
            );

            JSONArray pageReleases =
                parseResponse(response.body());

            addReleases(
                pageReleases,
                releases
            );

            if (pageReleases.length() < PAGE_SIZE) {
                break;
            }

            page++;
        }

        releases.sort(
            Comparator
                .comparing(
                    GitHubReleaseInfo::publishedDate
                )
                .thenComparing(
                    GitHubReleaseInfo::tagName
                )
        );

        return List.copyOf(releases);
    }

    private static void checkResponse(
            HttpResponse<String> response,
            String owner,
            String repositoryName)
            throws IOException {

        if (response.statusCode() == 200) {
            return;
        }

        String remainingRequests =
            response.headers()
                .firstValue(
                    "x-ratelimit-remaining"
                )
                .orElse("unknown");

        throw new IOException(
            "GitHub request failed with HTTP status "
                + response.statusCode()
                + " for "
                + owner
                + "/"
                + repositoryName
                + ". Remaining rate limit: "
                + remainingRequests
        );
    }

    private static JSONArray parseResponse(
            String responseBody)
            throws IOException {

        try {
            return new JSONArray(responseBody);

        } catch (JSONException exception) {
            throw new IOException(
                "Unable to parse the GitHub releases response.",
                exception
            );
        }
    }

    private static void addReleases(
            JSONArray releaseArray,
            List<GitHubReleaseInfo> destination)
            throws IOException {

        try {
            for (
                int index = 0;
                index < releaseArray.length();
                index++
            ) {
                JSONObject release =
                    releaseArray.getJSONObject(index);

                String publishedAt =
                    release.optString(
                        "published_at",
                        ""
                    );

                if (publishedAt.isBlank()) {
                    continue;
                }

                LocalDate publishedDate =
                    OffsetDateTime
                        .parse(publishedAt)
                        .withOffsetSameInstant(
                            ZoneOffset.UTC
                        )
                        .toLocalDate();

                destination.add(
                    new GitHubReleaseInfo(
                        release.optString(
                            "tag_name",
                            ""
                        ),
                        release.optString(
                            "name",
                            ""
                        ),
                        publishedDate,
                        release.optBoolean(
                            "draft",
                            false
                        ),
                        release.optBoolean(
                            "prerelease",
                            false
                        )
                    )
                );
            }

        } catch (
                JSONException
                | DateTimeParseException exception) {

            throw new IOException(
                "Unable to parse a GitHub release.",
                exception
            );
        }
    }

    private static String validateRepositoryComponent(
            String value,
            String fieldName) {

        Objects.requireNonNull(
            value,
            fieldName + " cannot be null."
        );

        String normalized = value.trim();

        if (
            !normalized.matches(
                "[A-Za-z0-9_.-]+"
            )
        ) {
            throw new IllegalArgumentException(
                "Invalid GitHub "
                    + fieldName
                    + ": "
                    + value
            );
        }

        return normalized;
    }
}
package it.uniroma2.isw2.storm.sonar;

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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class SonarCloudClient {

    private static final URI BASE_URI =
        URI.create("https://sonarcloud.io/");

    private static final Duration CONNECT_TIMEOUT =
        Duration.ofSeconds(20);

    private static final Duration REQUEST_TIMEOUT =
        Duration.ofSeconds(45);

    private static final Duration CE_POLL_INTERVAL =
        Duration.ofSeconds(2);

    private static final Duration CE_TIMEOUT =
        Duration.ofMinutes(10);

    private static final int PAGE_SIZE = 500;

    private static final String USER_AGENT =
        "ISW2-Storm-Analyzer/1.0";

    private final HttpClient httpClient;
    private final String token;

    private SonarCloudClient(
            HttpClient httpClient,
            String token) {

        this.httpClient = Objects.requireNonNull(
            httpClient,
            "HTTP client cannot be null."
        );

        this.token = requireNonBlank(
            token,
            "SonarCloud token cannot be blank."
        );
    }

    public static SonarCloudClient fromEnvironment() {
        String token = System.getenv("SONAR_TOKEN");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                "Environment variable SONAR_TOKEN is not set."
            );
        }

        HttpClient client =
            HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        return new SonarCloudClient(
            client,
            token.strip()
        );
    }

    public void waitForCeTask(String ceTaskId)
            throws IOException, InterruptedException {

        String normalizedTaskId = requireNonBlank(
            ceTaskId,
            "Compute Engine task id cannot be blank."
        );

        long deadline =
            System.nanoTime()
                + CE_TIMEOUT.toNanos();

        while (true) {
            URI endpoint = BASE_URI.resolve(
                "api/ce/task?id="
                    + encode(normalizedTaskId)
            );

            JSONObject root = getJson(endpoint);
            JSONObject task = root.getJSONObject("task");
            String status = task.getString("status");

            switch (status) {
                case "SUCCESS" -> {
                    return;
                }

                case "FAILED", "CANCELED" ->
                    throw new IOException(
                        "SonarCloud analysis ended with status "
                            + status
                            + " for CE task "
                            + normalizedTaskId
                    );

                case "PENDING", "IN_PROGRESS" -> {
                    if (System.nanoTime() >= deadline) {
                        throw new IOException(
                            "Timed out while waiting for SonarCloud "
                                + "CE task "
                                + normalizedTaskId
                        );
                    }

                    Thread.sleep(
                        CE_POLL_INTERVAL.toMillis()
                    );
                }

                default -> throw new IOException(
                    "Unexpected SonarCloud CE status: "
                        + status
                );
            }
        }
    }

    public Map<String, Integer> fetchCodeSmells(
            String projectKey)
            throws IOException, InterruptedException {

        String normalizedProjectKey = requireNonBlank(
            projectKey,
            "SonarCloud project key cannot be blank."
        );

        Map<String, Integer> result =
            new LinkedHashMap<>();

        int page = 1;
        int total = Integer.MAX_VALUE;

        while ((page - 1) * PAGE_SIZE < total) {

            String query =
                "component=" + encode(normalizedProjectKey)
                    + "&metricKeys=code_smells"
                    + "&qualifiers=FIL"
                    + "&strategy=leaves"
                    + "&p=" + page
                    + "&ps=" + PAGE_SIZE;

            URI endpoint = BASE_URI.resolve(
                "api/measures/component_tree?" + query
            );

            JSONObject root = getJson(endpoint);

            JSONObject paging =
                root.getJSONObject("paging");

            total = paging.getInt("total");

            JSONArray components =
                root.getJSONArray("components");

            for (int index = 0;
                    index < components.length();
                    index++) {

                JSONObject component =
                    components.getJSONObject(index);

                String path = normalizePath(
                    component.optString("path", "")
                );

                if (path.isBlank()) {
                    continue;
                }

                int codeSmells =
                    readRequiredCodeSmells(
                        component.optJSONArray("measures"),
                        path
                    );

                Integer previous =
                    result.put(
                        path,
                        codeSmells
                    );

                if (previous != null) {
                    throw new IOException(
                        "Duplicate SonarCloud file path: "
                            + path
                    );
                }
            }

            page++;
        }

        return Map.copyOf(result);
    }

    public int fetchProjectCodeSmells(
            String projectKey)
            throws IOException, InterruptedException {

        String normalizedProjectKey = requireNonBlank(
            projectKey,
            "SonarCloud project key cannot be blank."
        );

        String query =
            "component=" + encode(normalizedProjectKey)
                + "&metricKeys=code_smells";

        URI endpoint = BASE_URI.resolve(
            "api/measures/component?" + query
        );

        JSONObject root = getJson(endpoint);

        JSONObject component =
            root.getJSONObject("component");

        return readRequiredCodeSmells(
            component.optJSONArray("measures"),
            normalizedProjectKey
        );
    }

    private JSONObject getJson(URI endpoint)
            throws IOException, InterruptedException {

        HttpRequest request =
            HttpRequest.newBuilder(endpoint)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .header(
                    "Authorization",
                    "Bearer " + token
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

        if (response.statusCode() != 200) {
            throw new IOException(
                "SonarCloud request failed with HTTP "
                    + response.statusCode()
                    + " for "
                    + endpoint.getPath()
                    + ": "
                    + abbreviate(response.body())
            );
        }

        try {
            return new JSONObject(response.body());

        } catch (JSONException exception) {
            throw new IOException(
                "Unable to parse SonarCloud JSON response.",
                exception
            );
        }
    }

    private static int readRequiredCodeSmells(
            JSONArray measures,
            String component)
            throws IOException {

        if (measures == null) {
            throw new IOException(
                "SonarCloud did not return code_smells for "
                    + component
            );
        }

        for (int index = 0;
                index < measures.length();
                index++) {

            JSONObject measure =
                measures.optJSONObject(index);

            if (measure == null) {
                continue;
            }

            if (!"code_smells".equals(
                    measure.optString("metric", ""))) {
                continue;
            }

            String value =
                measure.optString("value", "").strip();

            if (value.isBlank()) {
                throw new IOException(
                    "Blank SonarCloud code_smells value for "
                        + component
                );
            }

            try {
                int parsed =
                    Integer.parseInt(value);

                if (parsed < 0) {
                    throw new IOException(
                        "Negative SonarCloud code_smells value for "
                            + component
                    );
                }

                return parsed;

            } catch (NumberFormatException exception) {
                throw new IOException(
                    "Invalid SonarCloud code_smells value: "
                        + value
                        + " for "
                        + component,
                    exception
                );
            }
        }

        throw new IOException(
            "SonarCloud measure code_smells missing for "
                + component
        );
    }

    private static String normalizePath(String value) {
        return value
            .replace('\\', '/')
            .replaceAll("^\\./+", "")
            .strip();
    }

    private static String requireNonBlank(
            String value,
            String message) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.strip();
    }

    private static String encode(String value) {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
        );
    }

    private static String abbreviate(String value) {
        if (value == null) {
            return "";
        }

        String normalized =
            value.replaceAll("\\s+", " ").strip();

        if (normalized.length() <= 500) {
            return normalized;
        }

        return normalized.substring(0, 500) + "...";
    }
}
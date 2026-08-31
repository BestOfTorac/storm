package org.apache.storm.daemon.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.storm.DaemonConfig;
import org.apache.storm.generated.ExecutorInfo;
import org.apache.storm.generated.TopologyHistoryInfo;
import org.junit.jupiter.api.Test;

class UIHelpersLLMTest {

    @Test
    void t01PrettyUptimeSecStringFormatsZeroAsEmptyString() {
        assertEquals("", UIHelpers.prettyUptimeSec("0"));
    }

    @Test
    void t02PrettyUptimeSecIntFormatsSecondsOnly() {
        assertEquals("59s", UIHelpers.prettyUptimeSec(59));
    }

    @Test
    void t03PrettyUptimeSecFormatsExactMinute() {
        assertEquals("1m 0s", UIHelpers.prettyUptimeSec("60"));
    }

    @Test
    void t04PrettyUptimeSecFormatsHoursMinutesAndSeconds() {
        assertEquals("1h 1m 1s", UIHelpers.prettyUptimeSec(3661));
    }

    @Test
    void t05PrettyUptimeSecFormatsDays() {
        assertEquals("1d 1h 1m 1s", UIHelpers.prettyUptimeSec(90061));
    }

    @Test
    void t06PrettyUptimeMsStringFormatsMillisecondsOnly() {
        assertEquals("999ms", UIHelpers.prettyUptimeMs("999"));
    }

    @Test
    void t07PrettyUptimeMsFormatsExactSecond() {
        assertEquals("1s 0ms", UIHelpers.prettyUptimeMs(1000));
    }

    @Test
    void t08PrettyUptimeMsFormatsCompositeDuration() {
        assertEquals("1m 1s 1ms", UIHelpers.prettyUptimeMs(61001));
    }

    @Test
    void t09PrettyUptimeSecRejectsNonNumericInput() {
        assertThrows(NumberFormatException.class, () -> UIHelpers.prettyUptimeSec("abc"));
    }

    @Test
    void t10PrettyUptimeStrUsesCallerSuppliedDividers() {
        Object[][] dividers = {{"u", 10}, {"d", null}};
        assertEquals("2d 5u", UIHelpers.prettyUptimeStr("25", dividers));
    }

    @Test
    void t11UrlFormatLeavesUnreservedValuesReadable() {
        assertEquals("/api/alpha-1", UIHelpers.urlFormat("/api/%s", "alpha-1"));
    }

    @Test
    void t12UrlFormatEncodesSpacesAndUnicodeUtf8() {
        assertEquals("/api/a%20b/caf%C3%A9", UIHelpers.urlFormat("/api/%s/%s", "a b", "café"));
    }

    @Test
    void t13UrlFormatConvertsNullArgumentToLiteralNull() {
        assertEquals("/api/null", UIHelpers.urlFormat("/api/%s", (Object) null));
    }

    @Test
    void t14PrettyExecutorInfoUsesInclusiveTaskRange() {
        ExecutorInfo info = mock(ExecutorInfo.class);
        when(info.get_task_start()).thenReturn(3);
        when(info.get_task_end()).thenReturn(7);
        assertEquals("[3-7]", UIHelpers.prettyExecutorInfo(info));
    }

    @Test
    void t15UnauthorizedUserJsonContainsStableErrorAndUserMessage() {
        Map<String, Object> result = UIHelpers.unauthorizedUserJson("alice");
        assertEquals(2, result.size());
        assertEquals("No Authorization", result.get("error"));
        assertEquals("User alice is not authorized.", result.get("errorMessage"));
    }

    @Test
    void t16JsonHeadersWithoutCallbackUseJsonContentType() {
        Map<?, ?> headers = UIHelpers.getJsonResponseHeaders(null, null);
        assertEquals("application/json;charset=utf-8", headers.get("Content-Type"));
        assertEquals("no-cache, no-store", headers.get("Cache-Control"));
        assertEquals("*", headers.get("Access-Control-Allow-Origin"));
        assertEquals("nosniff", headers.get("X-Content-Type-Options"));
    }

    @Test
    void t17JsonHeadersWithValidCallbackUseJavascriptContentType() {
        Map<?, ?> headers = UIHelpers.getJsonResponseHeaders("app.callbacks.done", null);
        assertEquals("application/javascript;charset=utf-8", headers.get("Content-Type"));
    }

    @Test
    void t18JsonHeadersAllowExplicitOverrides() {
        Map<String, String> overrides = Map.of("Content-Type", "custom/type", "X-Test", "yes");
        Map<?, ?> headers = UIHelpers.getJsonResponseHeaders(null, overrides);
        assertEquals("custom/type", headers.get("Content-Type"));
        assertEquals("yes", headers.get("X-Test"));
    }

    @Test
    void t19JsonBodySerializesMapWithoutCallback() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "storm");
        data.put("count", 2);
        assertEquals("{\"name\":\"storm\",\"count\":2}", UIHelpers.getJsonResponseBody(data, null, true));
    }

    @Test
    void t20JsonBodyReturnsRawStringWhenSerializationDisabled() {
        assertEquals("{\"ok\":true}", UIHelpers.getJsonResponseBody("{\"ok\":true}", null, false));
    }

    @Test
    void t21JsonBodyWrapsRawPayloadForValidCallback() {
        assertEquals("cb_1({\"ok\":true});",
                UIHelpers.getJsonResponseBody("{\"ok\":true}", "cb_1", false));
    }

    @Test
    void t22JsonBodyIgnoresSyntacticallyInvalidCallback() {
        assertEquals("{}", UIHelpers.getJsonResponseBody("{}", "alert(1)", false));
    }

    @Test
    void t23JsonBodyIgnoresCallbackLongerThan128Characters() {
        String callback = "a".repeat(129);
        assertEquals("{}", UIHelpers.getJsonResponseBody("{}", callback, false));
    }

    @Test
    void t24ExceptionToJsonUsesExceptionMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(new IllegalArgumentException("bad input"), 400);
        assertEquals("400 Bad Request", result.get("error"));
        assertEquals("bad input", result.get("errorMessage"));
    }

    @Test
    void t25ExceptionToJsonFallsBackToExceptionClassName() {
        Map<String, Object> result = UIHelpers.exceptionToJson(new IllegalStateException(), 500);
        assertEquals("500 Server Error", result.get("error"));
        assertEquals(IllegalStateException.class.getName(), result.get("errorMessage"));
    }

    @Test
    void t26TopologyOperationResponseContainsOperationIdAndSuccess() {
        Map<String, Object> result = UIHelpers.getTopologyOpResponse("topology-1", "activate");
        assertEquals(3, result.size());
        assertEquals("activate", result.get("topologyOperation"));
        assertEquals("topology-1", result.get("topologyId"));
        assertEquals("success", result.get("status"));
    }

    @Test
    void t27ProfilingDisabledResponseIsStable() {
        Map<String, Object> result = UIHelpers.getProfilingDisabled();
        assertEquals(Map.of(
                "status", "disabled",
                "message", "Profiling is not enabled on this server"), result);
    }

    @Test
    void t28SecureLogviewerIsFalseWhenHttpsPortIsAbsent() {
        assertFalse(UIHelpers.isSecureLogviewer(Map.of(DaemonConfig.LOGVIEWER_PORT, 8000)));
    }

    @Test
    void t29SecureLogviewerAcceptsZeroHttpsPort() {
        assertTrue(UIHelpers.isSecureLogviewer(Map.of(DaemonConfig.LOGVIEWER_HTTPS_PORT, 0)));
    }

    @Test
    void t30SecureLogviewerRejectsNegativeHttpsPort() {
        assertFalse(UIHelpers.isSecureLogviewer(Map.of(DaemonConfig.LOGVIEWER_HTTPS_PORT, -1)));
    }

    @Test
    void t31LogviewerPortSelectsHttpsPortWhenSecure() {
        Map<String, Object> config = Map.of(
                DaemonConfig.LOGVIEWER_PORT, 8000,
                DaemonConfig.LOGVIEWER_HTTPS_PORT, 8443);
        assertEquals(8443, UIHelpers.getLogviewerPort(config));
    }

    @Test
    void t32LogviewerLinkUsesHttpAndEncodesFileName() {
        Map<String, Object> config = Map.of(DaemonConfig.LOGVIEWER_PORT, 8000);
        assertEquals("http://worker%20one:8000/api/v1/log?file=a%20b.log",
                UIHelpers.getLogviewerLink("worker one", "a b.log", config, 6700));
    }

    @Test
    void t33LogviewerLinkUsesHttpsPortWhenConfigured() {
        Map<String, Object> config = Map.of(
                DaemonConfig.LOGVIEWER_PORT, 8000,
                DaemonConfig.LOGVIEWER_HTTPS_PORT, 8443);
        assertEquals("https://worker:8443/api/v1/log?file=worker.log",
                UIHelpers.getLogviewerLink("worker", "worker.log", config, 6700));
    }

    @Test
    void t34SanitizeStreamNameReplacesInvalidCharactersAndPrefixesNonLetter() {
        assertEquals("orders.main-v2", UIHelpers.sanitizeStreamName("orders.main-v2"));
        assertEquals("orders_eu", UIHelpers.sanitizeStreamName("orders/eu"));
        assertEquals("_s_9stream", UIHelpers.sanitizeStreamName("9stream"));
        assertEquals("_s", UIHelpers.sanitizeStreamName(""));
    }

    @Test
    void t35SanitizeTransferredStatsTransformsInnerKeysWithoutMutatingInput() {
        Map<String, Map<String, Long>> input = new LinkedHashMap<>();
        Map<String, Long> inner = new LinkedHashMap<>();
        inner.put("valid-stream", 4L);
        inner.put("bad/stream", 9L);
        input.put("600", inner);

        Map<String, Map<String, Long>> result = UIHelpers.sanitizeTransferredStats(input);

        assertEquals(Map.of("600", Map.of("valid-stream", 4L, "bad_stream", 9L)), result);
        assertTrue(input.get("600").containsKey("bad/stream"));
        assertFalse(input.get("600").containsKey("bad_stream"));
    }
}

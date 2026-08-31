package org.apache.storm.daemon.ui;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.storm.DaemonConfig;
import org.apache.storm.generated.ExecutorInfo;
import org.apache.storm.generated.TopologyHistoryInfo;
import org.junit.jupiter.api.Test;

/**
 * Unit tests derived only from the supplied UIHelpers source and testing-module pom.xml.
 */
class UIHelpersLLMTest {

    @Test
    void t01PrettyUptimeSecZeroProducesEmptyString() {
        assertEquals("", UIHelpers.prettyUptimeSec(0));
    }

    @Test
    void t02PrettyUptimeSecBelowMinuteUsesSecondsOnly() {
        assertEquals("59s", UIHelpers.prettyUptimeSec(59));
    }

    @Test
    void t03PrettyUptimeSecExactMinuteOmitsZeroSeconds() {
        assertEquals("1m", UIHelpers.prettyUptimeSec("60"));
    }

    @Test
    void t04PrettyUptimeSecCombinesDaysHoursMinutesAndSeconds() {
        assertEquals("1d 1h 1m 1s", UIHelpers.prettyUptimeSec(90_061));
    }

    @Test
    void t05PrettyUptimeMsBelowSecondUsesMillisecondsOnly() {
        assertEquals("999ms", UIHelpers.prettyUptimeMs(999));
    }

    @Test
    void t06PrettyUptimeMsExactSecondOmitsZeroMilliseconds() {
        assertEquals("1s", UIHelpers.prettyUptimeMs("1000"));
    }

    @Test
    void t07PrettyUptimeMsCombinesMinutesSecondsAndMilliseconds() {
        assertEquals("1m 1s 1ms", UIHelpers.prettyUptimeMs(61_001));
    }

    @Test
    void t08PrettyUptimeStrUsesCallerSuppliedDividers() {
        Object[][] dividers = {{"u", 10}, {"t", null}};
        assertEquals("2t 3u", UIHelpers.prettyUptimeStr("23", dividers));
    }

    @Test
    void t09PrettyUptimeSecNegativeProducesEmptyString() {
        assertEquals("", UIHelpers.prettyUptimeSec(-1));
    }

    @Test
    void t10PrettyUptimeSecRejectsNonNumericInput() {
        assertThrows(NumberFormatException.class, () -> UIHelpers.prettyUptimeSec("not-a-number"));
    }

    @Test
    void t11UrlFormatPercentEncodesEachArgument() {
        assertEquals("/component/a%20b/stream/x%2Fy", UIHelpers.urlFormat("/component/%s/stream/%s", "a b", "x/y"));
    }

    @Test
    void t12UrlFormatPreservesUnreservedCharacters() {
        assertEquals("https://host.example:8080/a-b_c.txt", UIHelpers.urlFormat("https://%s:%s/%s", "host.example", 8080, "a-b_c.txt"));
    }

    @Test
    void t13UrlFormatConvertsNullArgumentToLiteralNull() {
        assertEquals("value=null", UIHelpers.urlFormat("value=%s", (Object) null));
    }

    @Test
    void t14PrettyExecutorInfoFormatsInclusiveTaskRange() {
        ExecutorInfo info = mock(ExecutorInfo.class);
        when(info.get_task_start()).thenReturn(3);
        when(info.get_task_end()).thenReturn(7);

        assertEquals("[3-7]", UIHelpers.prettyExecutorInfo(info));
    }

    @Test
    void t15UnauthorizedUserJsonContainsStableErrorContract() {
        Map<String, Object> result = UIHelpers.unauthorizedUserJson("alice");

        assertEquals(Map.of(
            "error", "No Authorization",
            "errorMessage", "User alice is not authorized."
        ), result);
    }

    @Test
    void t16JsonHeadersWithoutCallbackUseJsonContentType() {
        Map<String, Object> headers = UIHelpers.getJsonResponseHeaders(null, null);

        assertAll(
            () -> assertEquals("application/json;charset=utf-8", headers.get("Content-Type")),
            () -> assertEquals("no-cache, no-store", headers.get("Cache-Control")),
            () -> assertEquals("*", headers.get("Access-Control-Allow-Origin")),
            () -> assertEquals("nosniff", headers.get("X-Content-Type-Options"))
        );
    }

    @Test
    void t17JsonHeadersWithValidCallbackUseJavascriptContentType() {
        Map<String, Object> headers = UIHelpers.getJsonResponseHeaders("app.callbacks.done", null);
        assertEquals("application/javascript;charset=utf-8", headers.get("Content-Type"));
    }

    @Test
    void t18JsonHeadersWithInvalidCallbackFallBackToJson() {
        Map<String, Object> headers = UIHelpers.getJsonResponseHeaders("alert(1)", null);
        assertEquals("application/json;charset=utf-8", headers.get("Content-Type"));
    }

    @Test
    void t19JsonHeadersApplyCallerOverridesLast() {
        Map<String, String> extra = Map.of(
            "Content-Type", "application/problem+json",
            "X-Test", "present"
        );
        Map<String, Object> headers = UIHelpers.getJsonResponseHeaders(null, extra);

        assertAll(
            () -> assertEquals("application/problem+json", headers.get("Content-Type")),
            () -> assertEquals("present", headers.get("X-Test"))
        );
    }

    @Test
    void t20JsonBodySerializesSimpleMap() {
        assertEquals("{\"answer\":42}", UIHelpers.getJsonResponseBody(Map.of("answer", 42), null, true));
    }

    @Test
    void t21JsonBodyCanUseAlreadySerializedPayload() {
        String json = "{\"ready\":true}";
        assertEquals(json, UIHelpers.getJsonResponseBody(json, null, false));
    }

    @Test
    void t22JsonBodyWrapsSerializedPayloadForValidCallback() {
        assertEquals("cb({\"answer\":42});", UIHelpers.getJsonResponseBody(Map.of("answer", 42), "cb", true));
    }

    @Test
    void t23JsonBodyDoesNotWrapPayloadForInvalidCallback() {
        String json = "{\"ready\":true}";
        assertEquals(json, UIHelpers.getJsonResponseBody(json, "bad-callback", false));
    }

    @Test
    void t24JsonBodyRejectsCallbackLongerThan128Characters() {
        String callback = "a".repeat(129);
        assertEquals("{}", UIHelpers.getJsonResponseBody("{}", callback, false));
    }

    @Test
    void t25WrapJsonInCallbackUsesJsonpSyntax() {
        assertEquals("handler([1,2]);", UIHelpers.wrapJsonInCallback("handler", "[1,2]"));
    }

    @Test
    void t26ExceptionToJsonUsesExceptionMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(new IllegalArgumentException("bad input"), 400);

        assertAll(
            () -> assertEquals("400 Bad Request", result.get("error")),
            () -> assertEquals("bad input", result.get("errorMessage"))
        );
    }

    @Test
    void t27ExceptionToJsonFallsBackToClassNameForEmptyMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(new IllegalStateException(""), 500);
        assertEquals(IllegalStateException.class.getName(), result.get("errorMessage"));
    }

    @Test
    void t28SanitizeStreamNamePreservesAllowedLeadingLetterName() {
        assertEquals("orders.v1-main_stream", UIHelpers.sanitizeStreamName("orders.v1-main_stream"));
    }

    @Test
    void t29SanitizeStreamNameReplacesDisallowedCharacters() {
        assertEquals("order_items_eu_", UIHelpers.sanitizeStreamName("order items@eu!"));
    }

    @Test
    void t30SanitizeStreamNamePrefixesNameStartingWithDigit() {
        assertEquals("_s1stream", UIHelpers.sanitizeStreamName("1stream"));
    }

    @Test
    void t31SanitizeStreamNamePrefixesEmptyName() {
        assertEquals("_s", UIHelpers.sanitizeStreamName(""));
    }

    @Test
    void t32SanitizeTransferredStatsTransformsInnerKeysWithoutMutatingInput() {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("good.stream", 10L);
        inner.put("bad stream", 20L);
        Map<String, Map<String, Object>> input = new HashMap<>();
        input.put(":all-time", inner);

        Map<String, Map<String, Object>> result = UIHelpers.sanitizeTransferredStats(input);

        assertAll(
            () -> assertEquals(Map.of("good.stream", 10L, "bad_stream", 20L), result.get(":all-time")),
            () -> assertTrue(input.get(":all-time").containsKey("bad stream")),
            () -> assertFalse(input.get(":all-time").containsKey("bad_stream"))
        );
    }

    @Test
    void t33SecureLogviewerIsFalseWhenHttpsPortIsAbsent() {
        Map<String, Object> config = Map.of(DaemonConfig.LOGVIEWER_PORT, 8000);
        assertFalse(UIHelpers.isSecureLogviewer(config));
    }

    @Test
    void t34SecureLogviewerAcceptsZeroAndSelectsHttpsPort() {
        Map<String, Object> config = Map.of(
            DaemonConfig.LOGVIEWER_PORT, 8000,
            DaemonConfig.LOGVIEWER_HTTPS_PORT, 0
        );

        assertAll(
            () -> assertTrue(UIHelpers.isSecureLogviewer(config)),
            () -> assertEquals(0, UIHelpers.getLogviewerPort(config))
        );
    }

    @Test
    void t35TopologyHistoryInfoExposesTopologyIds() {
        TopologyHistoryInfo history = mock(TopologyHistoryInfo.class);
        List<String> ids = List.of("topology-1", "topology-2");
        when(history.get_topo_ids()).thenReturn(ids);

        assertEquals(ids, UIHelpers.getTopologyHistoryInfo(history).get("topo-history"));
    }
}

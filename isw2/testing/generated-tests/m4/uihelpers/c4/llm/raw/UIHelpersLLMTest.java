package org.apache.storm.daemon.ui;

import java.util.HashMap;
import java.util.Map;
import org.apache.storm.generated.ExecutorInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersLLMTest {

    @Test
    void t01PrettyUptimeSecStringZeroProducesEmptyString() {
        assertEquals("", UIHelpers.prettyUptimeSec("0"));
    }

    @Test
    void t02PrettyUptimeSecIntFormatsSecondsOnly() {
        assertEquals("59s", UIHelpers.prettyUptimeSec(59));
    }

    @Test
    void t03PrettyUptimeSecFormatsMinutesAndSeconds() {
        assertEquals("1m 1s", UIHelpers.prettyUptimeSec("61"));
    }

    @Test
    void t04PrettyUptimeSecFormatsHoursMinutesAndSeconds() {
        assertEquals("1h 1m 1s", UIHelpers.prettyUptimeSec(3661));
    }

    @Test
    void t05PrettyUptimeSecFormatsDaysAndRemainder() {
        assertEquals("1d 1h 1m 1s", UIHelpers.prettyUptimeSec(90061));
    }

    @Test
    void t06PrettyUptimeMsFormatsMillisecondsOnly() {
        assertEquals("999ms", UIHelpers.prettyUptimeMs("999"));
    }

    @Test
    void t07PrettyUptimeMsFormatsAllUnits() {
        assertEquals("1d 1h 1m 1s 1ms", UIHelpers.prettyUptimeMs(90061001));
    }

    @Test
    void t08PrettyUptimeSecRejectsNonNumericInput() {
        assertThrows(NumberFormatException.class, () -> UIHelpers.prettyUptimeSec("not-a-number"));
    }

    @Test
    void t09UrlFormatEncodesSpaces() {
        assertEquals("/log/worker+one", UIHelpers.urlFormat("/log/%s", "worker one"));
    }

    @Test
    void t10UrlFormatEncodesReservedCharactersInEveryArgument() {
        assertEquals("/a%2Fb?q=x%3Dy%26z", UIHelpers.urlFormat("/%s?q=%s", "a/b", "x=y&z"));
    }

    @Test
    void t11UrlFormatConvertsNullArgumentToLiteralNull() {
        assertEquals("/null", UIHelpers.urlFormat("/%s", (Object) null));
    }

    @Test
    void t12PrettyExecutorInfoUsesInclusiveTaskRangeNotation() {
        ExecutorInfo info = new ExecutorInfo();
        info.set_task_start(3);
        info.set_task_end(7);
        assertEquals("[3-7]", UIHelpers.prettyExecutorInfo(info));
    }

    @Test
    void t13UnauthorizedUserJsonContainsStableErrorAndUserMessage() {
        Map<String, Object> result = UIHelpers.unauthorizedUserJson("alice");
        assertEquals(2, result.size());
        assertEquals("No Authorization", result.get("error"));
        assertEquals("User alice is not authorized.", result.get("errorMessage"));
    }

    @Test
    void t14JsonHeadersWithoutCallbackUseJsonContentType() {
        Map<?, ?> headers = UIHelpers.getJsonResponseHeaders(null, null);
        assertEquals("application/json;charset=utf-8", headers.get("Content-Type"));
    }

    @Test
    void t15JsonHeadersWithValidCallbackUseJavascriptContentType() {
        Map<?, ?> headers = UIHelpers.getJsonResponseHeaders("app.callback_1", null);
        assertEquals("application/javascript;charset=utf-8", headers.get("Content-Type"));
    }

    @Test
    void t16JsonHeadersWithInvalidCallbackFallBackToJson() {
        Map<?, ?> headers = UIHelpers.getJsonResponseHeaders("alert(1)", null);
        assertEquals("application/json;charset=utf-8", headers.get("Content-Type"));
    }

    @Test
    void t17JsonHeadersContainSecurityAndCacheDefaults() {
        Map<?, ?> headers = UIHelpers.getJsonResponseHeaders(null, null);
        assertEquals("no-cache, no-store", headers.get("Cache-Control"));
        assertEquals("*", headers.get("Access-Control-Allow-Origin"));
        assertEquals("nosniff", headers.get("X-Content-Type-Options"));
    }

    @Test
    void t18JsonHeadersLetCallerOverrideDefaultsAndAddValues() {
        Map<String, String> supplied = new HashMap<>();
        supplied.put("Content-Type", "application/problem+json");
        supplied.put("X-Test", "present");
        Map<?, ?> headers = UIHelpers.getJsonResponseHeaders("callback", supplied);
        assertEquals("application/problem+json", headers.get("Content-Type"));
        assertEquals("present", headers.get("X-Test"));
    }

    @Test
    void t19JsonBodySerializesMapWithoutCallback() {
        Map<String, Object> data = new HashMap<>();
        data.put("answer", 42);
        assertEquals("{\"answer\":42}", UIHelpers.getJsonResponseBody(data, null, true));
    }

    @Test
    void t20JsonBodyWrapsSerializedDataInValidCallback() {
        assertEquals("cb({\"ok\":true});",
            UIHelpers.getJsonResponseBody(Map.of("ok", true), "cb", true));
    }

    @Test
    void t21JsonBodyLeavesPreSerializedStringUnchanged() {
        assertEquals("{\"x\":1}", UIHelpers.getJsonResponseBody("{\"x\":1}", null, false));
    }

    @Test
    void t22JsonBodyWrapsPreSerializedStringWithoutReserializing() {
        assertEquals("ns.cb([1,2]);", UIHelpers.getJsonResponseBody("[1,2]", "ns.cb", false));
    }

    @Test
    void t23JsonBodyIgnoresOverlongCallback() {
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
    void t25ExceptionToJsonUsesClassNameForNullMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(new NullPointerException(), 500);
        assertEquals("500 Server Error", result.get("error"));
        assertEquals(NullPointerException.class.getName(), result.get("errorMessage"));
    }

    @Test
    void t26ExceptionToJsonUsesClassNameForEmptyMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(new IllegalStateException(""), 500);
        assertEquals(IllegalStateException.class.getName(), result.get("errorMessage"));
    }

    @Test
    void t27WindowHintMapsAllTimeToken() {
        assertEquals("All time", UIHelpers.getWindowHint(":all-time"));
    }

    @Test
    void t28WindowHintFormatsNumericSeconds() {
        assertEquals("1h", UIHelpers.getWindowHint("3600"));
    }

    @Test
    void t29StatDisplayMapRekeysWindowsWithPrettyLabels() {
        Map<String, Object> raw = new HashMap<>();
        raw.put(":all-time", 10L);
        raw.put("60", 20L);
        Map<String, Object> result = UIHelpers.getStatDisplayMap(raw);
        assertEquals(Map.of("All time", 10L, "1m", 20L), result);
    }

    @Test
    void t30SanitizeStreamNameLeavesLetterLedSafeNameUnchanged() {
        assertEquals("orders.v1-test_stream", UIHelpers.sanitizeStreamName("orders.v1-test_stream"));
    }

    @Test
    void t31SanitizeStreamNameReplacesSpacesAndSlashes() {
        assertEquals("orders_eu_west", UIHelpers.sanitizeStreamName("orders eu/west"));
    }

    @Test
    void t32SanitizeStreamNamePrefixesDigitLedName() {
        assertEquals("_s9stream", UIHelpers.sanitizeStreamName("9stream"));
    }

    @Test
    void t33SanitizeStreamNamePrefixesUnderscoreLedName() {
        assertEquals("_s_internal", UIHelpers.sanitizeStreamName("_internal"));
    }

    @Test
    void t34SanitizeStreamNamePrefixesEmptyName() {
        assertEquals("_s", UIHelpers.sanitizeStreamName(""));
    }

    @Test
    void t35SanitizeTransferredStatsCreatesSanitizedNestedCopy() {
        Map<String, Map<String, Long>> input = new HashMap<>();
        input.put("600", new HashMap<>(Map.of("stream one", 4L, "2nd", 7L)));
        Map<String, Map<String, Long>> result = UIHelpers.sanitizeTransferredStats(input);
        assertEquals(Map.of("stream_one", 4L, "_s2nd", 7L), result.get("600"));
        assertTrue(input.get("600").containsKey("stream one"));
        assertFalse(input.get("600").containsKey("stream_one"));
        assertNull(result.get("missing"));
    }
}

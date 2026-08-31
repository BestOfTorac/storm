package org.apache.storm.daemon.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.storm.DaemonConfig;
import org.apache.storm.generated.ExecutorInfo;
import org.junit.jupiter.api.Test;

class UIHelpersLLMTest {

    @Test
    void T01_prettyUptimeSecFormatsZeroAsEmptyString() {
        assertEquals("", UIHelpers.prettyUptimeSec(0));
    }

    @Test
    void T02_prettyUptimeSecFormatsSecondsOnly() {
        assertEquals("59s", UIHelpers.prettyUptimeSec(59));
    }

    @Test
    void T03_prettyUptimeSecFormatsExactMinute() {
        assertEquals("1m 0s", UIHelpers.prettyUptimeSec(60));
    }

    @Test
    void T04_prettyUptimeSecFormatsCompositeDuration() {
        assertEquals("1d 1h 1m 1s", UIHelpers.prettyUptimeSec(90_061));
    }

    @Test
    void T05_prettyUptimeSecStringDelegatesToSameFormatting() {
        assertEquals("2m 5s", UIHelpers.prettyUptimeSec("125"));
    }

    @Test
    void T06_prettyUptimeMsFormatsMillisecondsOnly() {
        assertEquals("999ms", UIHelpers.prettyUptimeMs(999));
    }

    @Test
    void T07_prettyUptimeMsFormatsExactSecond() {
        assertEquals("1s 0ms", UIHelpers.prettyUptimeMs(1_000));
    }

    @Test
    void T08_prettyUptimeMsFormatsCompositeDuration() {
        assertEquals("1m 1s 1ms", UIHelpers.prettyUptimeMs(61_001));
    }

    @Test
    void T09_prettyUptimeMsStringDelegatesToSameFormatting() {
        assertEquals("2s 50ms", UIHelpers.prettyUptimeMs("2050"));
    }

    @Test
    void T10_prettyUptimeRejectsNonNumericInput() {
        assertThrows(NumberFormatException.class, () -> UIHelpers.prettyUptimeSec("abc"));
    }

    @Test
    void T11_prettyUptimeRejectsOverflowingInteger() {
        assertThrows(NumberFormatException.class, () -> UIHelpers.prettyUptimeMs("2147483648"));
    }

    @Test
    void T12_prettyUptimeStrUsesCallerSuppliedDividers() {
        Object[][] dividers = {{"u", 10}, {"t", null}};
        assertEquals("1t 2u", UIHelpers.prettyUptimeStr("12", dividers));
    }

    @Test
    void T13_urlFormatLeavesSafeArgumentsReadable() {
        assertEquals("/node/alpha/42", UIHelpers.urlFormat("/node/%s/%s", "alpha", 42));
    }

    @Test
    void T14_urlFormatPercentEncodesReservedCharacters() {
        assertEquals("/component/a%2Fb%20c", UIHelpers.urlFormat("/component/%s", "a/b c"));
    }

    @Test
    void T15_urlFormatConvertsNullArgumentToLiteralNull() {
        assertEquals("/value/null", UIHelpers.urlFormat("/value/%s", (Object) null));
    }

    @Test
    void T16_prettyExecutorInfoFormatsTaskRange() {
        ExecutorInfo info = mock(ExecutorInfo.class);
        when(info.get_task_start()).thenReturn(3);
        when(info.get_task_end()).thenReturn(7);
        assertEquals("[3-7]", UIHelpers.prettyExecutorInfo(info));
    }

    @Test
    void T17_unauthorizedUserJsonContainsStableErrorCode() {
        assertEquals("No Authorization", UIHelpers.unauthorizedUserJson("alice").get("error"));
    }

    @Test
    void T18_unauthorizedUserJsonInterpolatesUserVerbatim() {
        assertEquals("User a b is not authorized.",
                UIHelpers.unauthorizedUserJson("a b").get("errorMessage"));
    }

    @Test
    void T19_jsonHeadersUseJsonContentTypeWithoutCallback() {
        assertEquals("application/json;charset=utf-8",
                UIHelpers.getJsonResponseHeaders(null, null).get("Content-Type"));
    }

    @Test
    void T20_jsonHeadersUseJavascriptContentTypeForValidCallback() {
        assertEquals("application/javascript;charset=utf-8",
                UIHelpers.getJsonResponseHeaders("app.cb_1", null).get("Content-Type"));
    }

    @Test
    void T21_jsonHeadersRejectInvalidCallback() {
        assertEquals("application/json;charset=utf-8",
                UIHelpers.getJsonResponseHeaders("alert(1)", null).get("Content-Type"));
    }

    @Test
    void T22_jsonHeadersRejectCallbackLongerThan128Characters() {
        String callback = "a".repeat(129);
        assertEquals("application/json;charset=utf-8",
                UIHelpers.getJsonResponseHeaders(callback, null).get("Content-Type"));
    }

    @Test
    void T23_jsonHeadersAllowCallerOverridesAndAdditions() {
        Map<String, String> custom = Map.of("Content-Type", "application/problem+json", "X-Test", "yes");
        Map<String, String> result = UIHelpers.getJsonResponseHeaders(null, custom);
        assertEquals("application/problem+json", result.get("Content-Type"));
        assertEquals("yes", result.get("X-Test"));
        assertEquals("no-cache, no-store", result.get("Cache-Control"));
    }

    @Test
    void T24_jsonBodySerializesMapWithoutCallback() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("answer", 42);
        assertEquals("{\"answer\":42}", UIHelpers.getJsonResponseBody(data, null, true));
    }

    @Test
    void T25_jsonBodyWrapsSerializedDataInValidCallback() {
        assertEquals("cb({\"ok\":true});",
                UIHelpers.getJsonResponseBody(Map.of("ok", true), "cb", true));
    }

    @Test
    void T26_jsonBodyReturnsRawStringWhenSerializationDisabled() {
        assertEquals("{\"x\":1}", UIHelpers.getJsonResponseBody("{\"x\":1}", null, false));
    }

    @Test
    void T27_jsonBodyWrapsRawStringWhenSerializationDisabled() {
        assertEquals("ns.cb({\"x\":1});",
                UIHelpers.getJsonResponseBody("{\"x\":1}", "ns.cb", false));
    }

    @Test
    void T28_jsonBodyIgnoresInvalidCallback() {
        assertEquals("{}", UIHelpers.getJsonResponseBody("{}", "x-y", false));
    }

    @Test
    void T29_exceptionToJsonUsesExceptionMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(new IllegalArgumentException("bad input"), 400);
        assertEquals("400 Bad Request", result.get("error"));
        assertEquals("bad input", result.get("errorMessage"));
    }

    @Test
    void T30_exceptionToJsonFallsBackToClassNameForNullMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(new IllegalStateException(), 500);
        assertEquals(IllegalStateException.class.getName(), result.get("errorMessage"));
    }

    @Test
    void T31_makeStandardResponseCarriesStatusEntityAndHeaders() {
        try (Response response = UIHelpers.makeStandardResponse("{}", null, false, Response.Status.CREATED)) {
            assertEquals(201, response.getStatus());
            assertEquals("{}", response.getEntity());
            assertEquals("application/json;charset=utf-8", response.getHeaderString("Content-Type"));
        }
    }

    @Test
    void T32_secureLogviewerIsFalseWhenHttpsPortIsAbsent() {
        assertFalse(UIHelpers.isSecureLogviewer(Map.of(DaemonConfig.LOGVIEWER_PORT, 8000)));
    }

    @Test
    void T33_secureLogviewerAcceptsZeroHttpsPort() {
        assertTrue(UIHelpers.isSecureLogviewer(Map.of(DaemonConfig.LOGVIEWER_HTTPS_PORT, 0)));
    }

    @Test
    void T34_logviewerPortUsesHttpsPortWhenSecure() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_PORT, 8000);
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, 8443);
        assertEquals(8443, UIHelpers.getLogviewerPort(config));
    }

    @Test
    void T35_logviewerPortUsesHttpPortWhenHttpsPortIsNegative() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_PORT, 8000);
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, -1);
        assertEquals(8000, UIHelpers.getLogviewerPort(config));
    }
}

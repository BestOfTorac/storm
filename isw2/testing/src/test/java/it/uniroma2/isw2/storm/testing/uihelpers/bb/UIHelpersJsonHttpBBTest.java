package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.ws.rs.core.Response;

import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UIHelpersJsonHttpBBTest {

    private static final String JSON_CONTENT_TYPE =
            "application/json;charset=utf-8";

    private static final String JAVASCRIPT_CONTENT_TYPE =
            "application/javascript;charset=utf-8";

    /*
     * TBB-014 / F3.1
     * Unauthorized-user representation.
     *
     * 3 logical cases.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("unauthorizedUserCases")
    void testF3_1UnauthorizedUserRepresentation(
            String caseId,
            String user,
            String expectedMessage) {

        Map<String, Object> result =
                UIHelpers.unauthorizedUserJson(user);

        assertEquals(
                "No Authorization",
                result.get("error")
        );

        assertEquals(
                expectedMessage,
                result.get("errorMessage")
        );

        assertEquals(
                2,
                result.size()
        );
    }

    static Stream<Arguments> unauthorizedUserCases() {
        return Stream.of(
                Arguments.of(
                        "UNAUTH-REGULAR",
                        "alice",
                        "User alice is not authorized."
                ),
                Arguments.of(
                        "UNAUTH-EMPTY",
                        "",
                        "User  is not authorized."
                ),
                Arguments.of(
                        "UNAUTH-NULL",
                        null,
                        "User null is not authorized."
                )
        );
    }

    /*
     * TBB-015 / F3.2
     * Callback validation.
     *
     * Validation is observed through the externally visible response
     * Content-Type. A valid callback enables JSONP; an absent or invalid
     * callback results in regular JSON.
     *
     * 7 logical cases.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("callbackValidationCases")
    void testF3_2CallbackValidation(
            String caseId,
            String callback,
            String expectedContentType) {

        Map<?, ?> headers =
                UIHelpers.getJsonResponseHeaders(
                        callback,
                        null
                );

        assertEquals(
                expectedContentType,
                headers.get("Content-Type")
        );
    }

    static Stream<Arguments> callbackValidationCases() {

        String callback128 =
                "a".repeat(128);

        String callback129 =
                "a".repeat(129);

        return Stream.of(
                Arguments.of(
                        "CB-01",
                        null,
                        JSON_CONTENT_TYPE
                ),
                Arguments.of(
                        "CB-02",
                        "callback",
                        JAVASCRIPT_CONTENT_TYPE
                ),
                Arguments.of(
                        "CB-03",
                        "app.callback",
                        JAVASCRIPT_CONTENT_TYPE
                ),
                Arguments.of(
                        "CB-04",
                        "1callback",
                        JSON_CONTENT_TYPE
                ),
                Arguments.of(
                        "CB-05",
                        "callback()",
                        JSON_CONTENT_TYPE
                ),
                Arguments.of(
                        "CB-06",
                        callback128,
                        JAVASCRIPT_CONTENT_TYPE
                ),
                Arguments.of(
                        "CB-07",
                        callback129,
                        JSON_CONTENT_TYPE
                )
        );
    }

    /*
     * TBB-016 / F3.3
     * JSON response headers.
     *
     * 5 logical cases.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("responseHeaderCases")
    void testF3_3JsonResponseHeaders(
            String caseId,
            String callback,
            Map<String, String> customHeaders,
            String expectedContentType,
            String expectedCustomKey,
            String expectedCustomValue) {

        Map<?, ?> headers =
                UIHelpers.getJsonResponseHeaders(
                        callback,
                        customHeaders
                );

        assertEquals(
                "no-cache, no-store",
                headers.get("Cache-Control")
        );

        assertEquals(
                "*",
                headers.get("Access-Control-Allow-Origin")
        );

        assertEquals(
                "nosniff",
                headers.get("X-Content-Type-Options")
        );

        assertTrue(
                headers.containsKey(
                        "Access-Control-Allow-Headers"
                )
        );

        assertEquals(
                expectedContentType,
                headers.get("Content-Type")
        );

        if (expectedCustomKey != null) {

            assertEquals(
                    expectedCustomValue,
                    headers.get(expectedCustomKey)
            );
        }
    }

    static Stream<Arguments> responseHeaderCases() {
        return Stream.of(
                Arguments.of(
                        "HDR-01",
                        null,
                        null,
                        JSON_CONTENT_TYPE,
                        null,
                        null
                ),
                Arguments.of(
                        "HDR-02",
                        "callback",
                        null,
                        JAVASCRIPT_CONTENT_TYPE,
                        null,
                        null
                ),
                Arguments.of(
                        "HDR-03",
                        "1callback",
                        null,
                        JSON_CONTENT_TYPE,
                        null,
                        null
                ),
                Arguments.of(
                        "HDR-04",
                        null,
                        Map.of(
                                "X-Test",
                                "custom"
                        ),
                        JSON_CONTENT_TYPE,
                        "X-Test",
                        "custom"
                ),
                Arguments.of(
                        "HDR-05",
                        null,
                        Map.of(
                                "Content-Type",
                                "text/plain"
                        ),
                        "text/plain",
                        null,
                        null
                )
        );
    }

    /*
     * TBB-017 / F3.4
     * JSON response body.
     *
     * 6 logical cases.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("responseBodyCases")
    void testF3_4JsonResponseBody(
            String caseId,
            Object data,
            String callback,
            boolean needSerialize,
            String expected) {

        assertEquals(
                expected,
                UIHelpers.getJsonResponseBody(
                        data,
                        callback,
                        needSerialize
                )
        );
    }

    static Stream<Arguments> responseBodyCases() {

        Map<String, Object> structuredData =
                singleValueMap();

        return Stream.of(
                Arguments.of(
                        "BODY-01",
                        structuredData,
                        null,
                        true,
                        "{\"value\":1}"
                ),
                Arguments.of(
                        "BODY-02",
                        structuredData,
                        "callback",
                        true,
                        "callback({\"value\":1});"
                ),
                Arguments.of(
                        "BODY-03",
                        structuredData,
                        "1callback",
                        true,
                        "{\"value\":1}"
                ),
                Arguments.of(
                        "BODY-04",
                        "raw-body",
                        null,
                        false,
                        "raw-body"
                ),
                Arguments.of(
                        "BODY-05",
                        "raw-body",
                        "callback",
                        false,
                        "callback(raw-body);"
                ),
                Arguments.of(
                        "BODY-06",
                        "raw-body",
                        "1callback",
                        false,
                        "raw-body"
                )
        );
    }

    /*
     * TBB-018 / F3.5
     * Direct callback wrapping.
     *
     * 1 logical case.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("callbackWrappingCases")
    void testF3_5CallbackWrapping(
            String caseId,
            String callback,
            String response,
            String expected) {

        assertEquals(
                expected,
                UIHelpers.wrapJsonInCallback(
                        callback,
                        response
                )
        );
    }

    static Stream<Arguments> callbackWrappingCases() {
        return Stream.of(
                Arguments.of(
                        "WRAP-01",
                        "callback",
                        "{\"value\":1}",
                        "callback({\"value\":1});"
                )
        );
    }

    /*
     * TBB-019 / F3.6
     * Standard HTTP Response.
     *
     * 6 logical cases.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("standardResponseCases")
    void testF3_6StandardResponse(
            String caseId,
            String invocation,
            Object data,
            String callback,
            boolean needSerialize,
            Response.Status status,
            int expectedStatus,
            String expectedEntity,
            String expectedContentType) {

        Response response;

        switch (invocation) {

            case "DEFAULT" ->
                    response =
                            UIHelpers.makeStandardResponse(
                                    data,
                                    callback
                            );

            case "STATUS" ->
                    response =
                            UIHelpers.makeStandardResponse(
                                    data,
                                    callback,
                                    status
                            );

            case "FULL" ->
                    response =
                            UIHelpers.makeStandardResponse(
                                    data,
                                    callback,
                                    needSerialize,
                                    status
                            );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown invocation: "
                                    + invocation
                    );
        }

        assertEquals(
                expectedStatus,
                response.getStatus()
        );

        assertEquals(
                expectedEntity,
                response.getEntity()
        );

        assertEquals(
                expectedContentType,
                response.getHeaderString(
                        "Content-Type"
                )
        );

        assertEquals(
                "no-cache, no-store",
                response.getHeaderString(
                        "Cache-Control"
                )
        );

        assertEquals(
                "*",
                response.getHeaderString(
                        "Access-Control-Allow-Origin"
                )
        );

        assertEquals(
                "nosniff",
                response.getHeaderString(
                        "X-Content-Type-Options"
                )
        );

        assertTrue(
                response.getHeaders().containsKey(
                        "Access-Control-Allow-Headers"
                )
        );
    }

    static Stream<Arguments> standardResponseCases() {

        Map<String, Object> structuredData =
                singleValueMap();

        return Stream.of(
                Arguments.of(
                        "RESP-01",
                        "DEFAULT",
                        structuredData,
                        null,
                        true,
                        Response.Status.OK,
                        200,
                        "{\"value\":1}",
                        JSON_CONTENT_TYPE
                ),
                Arguments.of(
                        "RESP-02",
                        "STATUS",
                        structuredData,
                        "callback",
                        true,
                        Response.Status.OK,
                        200,
                        "callback({\"value\":1});",
                        JAVASCRIPT_CONTENT_TYPE
                ),
                Arguments.of(
                        "RESP-03",
                        "STATUS",
                        structuredData,
                        null,
                        true,
                        Response.Status.BAD_REQUEST,
                        400,
                        "{\"value\":1}",
                        JSON_CONTENT_TYPE
                ),
                Arguments.of(
                        "RESP-04",
                        "FULL",
                        "raw-body",
                        null,
                        false,
                        Response.Status.OK,
                        200,
                        "raw-body",
                        JSON_CONTENT_TYPE
                ),
                Arguments.of(
                        "RESP-05",
                        "FULL",
                        "raw-body",
                        "callback",
                        false,
                        Response.Status.OK,
                        200,
                        "callback(raw-body);",
                        JAVASCRIPT_CONTENT_TYPE
                ),
                Arguments.of(
                        "RESP-06",
                        "FULL",
                        structuredData,
                        "1callback",
                        true,
                        Response.Status.OK,
                        200,
                        "{\"value\":1}",
                        JSON_CONTENT_TYPE
                )
        );
    }

    private static Map<String, Object> singleValueMap() {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "value",
                1
        );

        return data;
    }
}

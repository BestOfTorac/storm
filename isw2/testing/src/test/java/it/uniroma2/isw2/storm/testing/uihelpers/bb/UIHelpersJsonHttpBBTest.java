package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.Map;

import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersJsonHttpBBTest {

    /*
     * F3 strict black-box suite.
     *
     * The suite verifies externally documented JSON and JSONP properties.
     * No implementation-derived expectation is used.
     */

    @Test
    void hdr01UsesJsonContentTypeWithoutCallback() {

        Map<?, ?> headers =
                UIHelpers.getJsonResponseHeaders(
                        null,
                        null
                );

        Object contentType =
                headers.get(
                        "Content-Type"
                );

        assertNotNull(
                contentType
        );

        assertTrue(
                String.valueOf(
                        contentType
                ).startsWith(
                        "application/json"
                )
        );
    }

    @Test
    void wrap01WrapsJsonInsideNamedCallback() {

        String json =
                "{\"value\":1}";

        String result =
                UIHelpers.wrapJsonInCallback(
                        "callback",
                        json
                );

        assertNotNull(
                result
        );

        assertTrue(
                result.startsWith(
                        "callback("
                )
        );

        assertTrue(
                result.contains(
                        json
                )
        );
    }
}

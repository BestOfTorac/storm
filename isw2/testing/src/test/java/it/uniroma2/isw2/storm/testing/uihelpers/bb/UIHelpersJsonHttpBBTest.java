package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.Map;

import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersJsonHttpBBTest {

    /*
     * Final minimal F3 strict black-box suite.
     *
     * Only externally documented JSON / JSONP properties are asserted.
     * Detailed callback grammar, complete header sets and exact
     * serialization rules are deliberately outside final T_BB.
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

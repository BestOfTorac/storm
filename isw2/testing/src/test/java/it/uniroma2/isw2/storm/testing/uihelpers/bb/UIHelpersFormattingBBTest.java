package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UIHelpersFormattingBBTest {

    /*
     * F1 strict black-box suite.
     *
     * Test oracles are derived from Storm's external REST documentation.
     * No implementation-derived expectation is used.
     */

    @Test
    void wh01RepresentsAllTimeWindow() {

        assertEquals(
                "All time",
                UIHelpers.getWindowHint(
                        ":all-time"
                )
        );
    }

    @Test
    void wh04RepresentsOneMinuteWindow() {

        assertEquals(
                "1m 0s",
                UIHelpers.getWindowHint(
                        "60"
                )
        );
    }
}

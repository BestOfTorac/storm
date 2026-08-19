package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UIHelpersFormattingBBTest {

    /*
     * Reduced F1 strict black-box suite.
     *
     * The original Category Partition remains documented separately.
     * Only two representative cases whose final oracle can be justified
     * without inspecting UIHelpers are retained in executable T_BB.
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

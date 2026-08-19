package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UIHelpersStatsComponentsBBTest {

    /*
     * F8 minimal strict black-box suite.
     *
     * These tests verify only externally documented
     * statistical-window formatting.
     *
     * No UIHelpers implementation was inspected.
     */

    @Test
    void statwin02FormatsAllTimeWindow() {

        Map<String, Double> input =
                new LinkedHashMap<>();

        input.put(":all-time", 11.0);

        Map<String, Double> result =
                UIHelpers.getStatDisplayMap(input);

        assertEquals(1, result.size());

        assertFalse(
                result.containsKey(":all-time")
        );

        assertEquals(
                11.0,
                result.get("All time")
        );
    }

    @Test
    void statwin03FormatsNumericWindow() {

        Map<String, Double> input =
                new LinkedHashMap<>();

        input.put("600", 22.0);

        Map<String, Double> result =
                UIHelpers.getStatDisplayMap(input);

        assertEquals(1, result.size());

        assertFalse(
                result.containsKey("600")
        );

        assertEquals(
                22.0,
                result.get("10m 0s")
        );
    }
}

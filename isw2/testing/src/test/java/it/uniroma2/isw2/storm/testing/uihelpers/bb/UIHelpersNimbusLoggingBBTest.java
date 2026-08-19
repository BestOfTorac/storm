package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.daemon.ui.UIHelpers;
import org.apache.storm.generated.ClusterSummary;
import org.apache.storm.generated.NimbusSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class UIHelpersNimbusLoggingBBTest {

    /*
     * F13 minimal strict black-box suite.
     *
     * Storm REST API /api/v1/nimbus/summary documents:
     * - Nimbus host
     * - Nimbus port
     * - status, including "Leader"
     *
     * It does not externally specify how active Nimbus entries and
     * nimbus.seeds are merged/deduplicated inside the helper response.
     *
     * Therefore the oracle checks only that the supplied leader Nimbus
     * is represented with its documented host, port and Leader status.
     *
     * UIHelpers implementation was not inspected.
     */

    @Test
    void nimbus02RepresentsLeaderNimbusAsLeader() {

        NimbusSummary nimbus =
                new NimbusSummary(
                        "nimbus.example",
                        6627,
                        120,
                        true,
                        "3.0.0"
                );

        ClusterSummary cluster =
                new ClusterSummary(
                        List.of(),
                        List.of(),
                        List.of(nimbus)
                );

        Map<String, Object> config =
                new HashMap<>();

        config.put(
                "nimbus.seeds",
                List.of("nimbus.example")
        );

        Map<String, Object> result =
                UIHelpers.getNimbusSummary(
                        cluster,
                        config
                );

        Object rawNimbuses =
                result.get("nimbuses");

        List<?> nimbuses =
                assertInstanceOf(
                        List.class,
                        rawNimbuses
                );

        boolean documentedLeaderFound =
                nimbuses.stream()
                        .filter(Map.class::isInstance)
                        .map(Map.class::cast)
                        .anyMatch(summary ->
                                "nimbus.example".equals(
                                        summary.get("host")
                                )
                                &&
                                Integer.valueOf(6627).equals(
                                        summary.get("port")
                                )
                                &&
                                "Leader".equals(
                                        summary.get("status")
                                )
                        );

        assertTrue(
                documentedLeaderFound,
                "Expected the supplied Nimbus to be represented as Leader"
        );
    }
}

package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.DaemonConfig;
import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersUrlLogviewerBBTest {

    /*
     * Reduced F2 strict black-box suite.
     *
     * Exact internal endpoint paths are deliberately not asserted because
     * Storm's REST documentation contains different logviewer endpoint forms.
     *
     * The final oracle checks only externally documented properties.
     */

    @Test
    void daemon01BuildsDocumentedNimbusLogviewerLink() {

        Map<String, Object> config =
                logviewerConfig();

        String link =
                UIHelpers.getNimbusLogLink(
                        "192.168.202.1",
                        config
                );

        assertTrue(
                link.startsWith(
                        "http://192.168.202.1:8000/"
                )
        );

        assertTrue(
                link.contains(
                        "nimbus.log"
                )
        );
    }

    @Test
    void workerlogHttpBuildsDocumentedWorkerLogviewerLink() {

        Map<String, Object> config =
                logviewerConfig();

        String link =
                UIHelpers.getWorkerLogLink(
                        "host",
                        6707,
                        config,
                        "ras-4-1460229987"
                );

        assertTrue(
                link.startsWith(
                        "http://host:8000/"
                )
        );

        assertTrue(
                link.contains(
                        "ras-4-1460229987"
                )
        );

        assertTrue(
                link.contains(
                        "6707"
                )
        );

        assertTrue(
                link.contains(
                        "worker.log"
                )
        );
    }

    private static Map<String, Object> logviewerConfig() {

        Map<String, Object> config =
                new HashMap<>();

        config.put(
                DaemonConfig.LOGVIEWER_PORT,
                8000
        );

        return config;
    }
}

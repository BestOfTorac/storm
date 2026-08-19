package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.Map;

import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UIHelpersTopologyOperationBBTest {

    /*
     * F12 strict black-box suite.
     *
     * The suite verifies the externally documented response contract
     * for topology activation.
     * No implementation-derived expectation is used.
     */

    @Test
    void oprsp01BuildsDocumentedActivateResponse() {

        Map<String, Object> result =
                UIHelpers.getTopologyOpResponse(
                        "topology-activate",
                        "activate"
                );

        assertEquals(
                "activate",
                result.get("topologyOperation")
        );

        assertEquals(
                "topology-activate",
                result.get("topologyId")
        );

        assertEquals(
                "success",
                result.get("status")
        );
    }
}

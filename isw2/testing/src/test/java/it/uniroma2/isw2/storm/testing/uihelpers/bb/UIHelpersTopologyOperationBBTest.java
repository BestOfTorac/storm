package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.Map;

import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UIHelpersTopologyOperationBBTest {

    /*
     * F12 minimal strict black-box suite.
     *
     * Storm's REST API explicitly documents an activate response as:
     * topologyOperation=activate
     * topologyId=<requested topology>
     * status=success
     *
     * No UIHelpers implementation was inspected.
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

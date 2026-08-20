package it.uniroma2.isw2.storm.testing.uihelpers.cf;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.Constants;
import org.apache.storm.DaemonConfig;
import org.apache.storm.daemon.ui.UIHelpers;
import org.apache.storm.generated.ClusterSummary;
import org.apache.storm.generated.OwnerResourceSummary;
import org.apache.storm.generated.SupervisorSummary;
import org.apache.storm.generated.TopologySummary;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersControlFlowTest {

    private static final char[] STORE_PASSWORD =
            "changeit".toCharArray();

    /*
     * Manual control-flow-guided evolution of T_BB.
     *
     * These tests target uncovered or partially covered paths identified
     * through the frozen T_BB JaCoCo report.
     */

    /*
     * ============================================================
     * Batch 1
     * ============================================================
     */

    @Test
    void cf01ValidJsonpCallbackAndAdditionalHeaders()
            throws Exception {

        Map<String, String> additionalHeaders =
                Map.of(
                        "X-CF",
                        "enabled"
                );

        Map<?, ?> result =
                UIHelpers.getJsonResponseHeaders(
                        "callback",
                        additionalHeaders
                );

        assertEquals(
                "application/javascript;charset=utf-8",
                result.get("Content-Type")
        );

        assertEquals(
                "enabled",
                result.get("X-CF")
        );
    }

    @Test
    void cf02SslTrustStoreWantAuthAndHeaderBuffer()
            throws Exception {

        Path store =
                createEmptyStore();

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
                8443,
                store.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                new String(STORE_PASSWORD),
                store.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                false,
                true,
                32768,
                false
        );

        ServerConnector connector =
                findSslConnector(server);

        SslContextFactory.Server ssl =
                getSslContextFactory(connector);

        HttpConnectionFactory http =
                connector.getConnectionFactory(
                        HttpConnectionFactory.class
                );

        assertNotNull(http);

        assertEquals(
                8443,
                connector.getPort()
        );

        assertFalse(
                ssl.getNeedClientAuth()
        );

        assertTrue(
                ssl.getWantClientAuth()
        );

        assertEquals(
                32768,
                http.getHttpConfiguration()
                        .getRequestHeaderSize()
        );
    }

    @Test
    void cf03SecureLogviewerUsesHttpsBranches() {

        Map<String, Object> config =
                logviewerConfig(
                        8000,
                        8443
                );

        assertEquals(
                8443,
                UIHelpers.getLogviewerPort(config)
        );

        assertEquals(
                "https://worker.example:8443/api/v1/log?file=worker.log",
                UIHelpers.getLogviewerLink(
                        "worker.example",
                        "worker.log",
                        config,
                        6700
                )
        );

        assertEquals(
                "https://nimbus.example:8443/api/v1/daemonlog?file=nimbus.log",
                UIHelpers.getNimbusLogLink(
                        "nimbus.example",
                        config
                )
        );

        String dump =
                UIHelpers.getWorkerDumpLink(
                        "worker.example",
                        6700L,
                        "topology-1",
                        config
                );

        assertTrue(
                dump.startsWith(
                        "https://worker.example:8443/api/v1/dumps/"
                )
        );

        assertTrue(
                dump.contains(
                        "topology-1"
                )
        );

        assertTrue(
                dump.contains(
                        "6700"
                )
        );
    }

    @Test
    void cf04DisabledHttpsUsesHttpBranches() {

        Map<String, Object> config =
                logviewerConfig(
                        8000,
                        -1
                );

        assertFalse(
                UIHelpers.isSecureLogviewer(config)
        );

        assertEquals(
                8000,
                UIHelpers.getLogviewerPort(config)
        );

        assertEquals(
                "http://supervisor.example:8000/api/v1/daemonlog?file=supervisor.log",
                UIHelpers.getSupervisorLogLink(
                        "supervisor.example",
                        config
                )
        );

        String dump =
                UIHelpers.getWorkerDumpLink(
                        "worker.example",
                        6700L,
                        "topology-1",
                        config
                );

        assertTrue(
                dump.startsWith(
                        "http://worker.example:8000/api/v1/dumps/"
                )
        );

        assertTrue(
                dump.contains(
                        "topology-1"
                )
        );

        assertTrue(
                dump.contains(
                        "6700"
                )
        );
    }

    /*
     * ============================================================
     * Batch 2
     * ============================================================
     */

    @Test
    void cf05InvalidJsonpCallbacksUsePlainJsonPath() {

        String json =
                "{\"value\":1}";

        String malformed =
                UIHelpers.getJsonResponseBody(
                        json,
                        "invalid-callback!",
                        false
                );

        assertEquals(
                json,
                malformed
        );

        String tooLongCallback =
                "a".repeat(129);

        String tooLong =
                UIHelpers.getJsonResponseBody(
                        json,
                        tooLongCallback,
                        false
                );

        assertEquals(
                json,
                tooLong
        );
    }

    @Test
    void cf06UnsetOwnerGuaranteesUseFallbackBranches() {

        OwnerResourceSummary owner =
                new OwnerResourceSummary(
                        "owner-defaults"
                );

        Map<String, Object> result =
                UIHelpers.unpackOwnerResourceSummary(
                        owner
                );

        assertEquals(
                "owner-defaults",
                result.get("owner")
        );

        assertEquals(
                "N/A",
                result.get("memoryGuarantee")
        );

        assertEquals(
                "N/A",
                result.get("cpuGuarantee")
        );

        assertEquals(
                -1,
                ((Number) result.get("isolatedNodes"))
                        .intValue()
        );

        assertEquals(
                "N/A",
                result.get("memoryGuaranteeRemaining")
        );

        assertEquals(
                "N/A",
                result.get("cpuGuaranteeRemaining")
        );
    }

    @Test
    void cf07NonEmptyClusterExercisesPositiveResourceBranches() {

        SupervisorSummary supervisor =
                new SupervisorSummary();

        supervisor.set_num_workers(4);
        supervisor.set_num_used_workers(1);

        Map<String, Double> totalResources =
                new HashMap<>();

        totalResources.put(
                Constants.COMMON_TOTAL_MEMORY_RESOURCE_NAME,
                4096.0
        );

        totalResources.put(
                Constants.COMMON_CPU_RESOURCE_NAME,
                400.0
        );

        supervisor.set_total_resources(
                totalResources
        );

        supervisor.set_used_mem(
                1024.0
        );

        supervisor.set_used_cpu(
                100.0
        );

        supervisor.set_used_generic_resources(
                new HashMap<>()
        );

        TopologySummary topology =
                new TopologySummary();

        topology.set_num_tasks(
                10
        );

        topology.set_num_executors(
                7
        );

        ClusterSummary cluster =
                new ClusterSummary();

        cluster.set_supervisors(
                List.of(supervisor)
        );

        cluster.set_topologies(
                List.of(topology)
        );

        cluster.set_nimbuses(
                List.of()
        );

        Map<String, Object> config =
                new HashMap<>();

        config.put(
                DaemonConfig.SCHEDULER_DISPLAY_RESOURCE,
                false
        );

        Map<String, Object> result =
                UIHelpers.getClusterSummary(
                        cluster,
                        "cf-user",
                        config
                );

        assertEquals(
                1,
                ((Number) result.get("supervisors"))
                        .intValue()
        );

        assertEquals(
                1,
                ((Number) result.get("topologies"))
                        .intValue()
        );

        assertEquals(
                1,
                ((Number) result.get("slotsUsed"))
                        .intValue()
        );

        assertEquals(
                4,
                ((Number) result.get("slotsTotal"))
                        .intValue()
        );

        assertEquals(
                3,
                ((Number) result.get("slotsFree"))
                        .intValue()
        );

        assertEquals(
                10,
                ((Number) result.get("tasksTotal"))
                        .intValue()
        );

        assertEquals(
                7,
                ((Number) result.get("executorsTotal"))
                        .intValue()
        );

        assertEquals(
                4096.0,
                ((Number) result.get("totalMem"))
                        .doubleValue()
        );

        assertEquals(
                400.0,
                ((Number) result.get("totalCpu"))
                        .doubleValue()
        );

        assertEquals(
                3072.0,
                ((Number) result.get("availMem"))
                        .doubleValue()
        );

        assertEquals(
                300.0,
                ((Number) result.get("availCpu"))
                        .doubleValue()
        );
    }

    /*
     * ============================================================
     * Batch 3
     * ============================================================
     */

    @Test
    void cf08ExceptionToJsonExercisesMessageFallbacks() {

        Map<?, ?> explicitMessage =
                UIHelpers.exceptionToJson(
                        new IllegalArgumentException(
                                "boom"
                        ),
                        400
                );

        assertEquals(
                "boom",
                explicitMessage.get(
                        "errorMessage"
                )
        );

        assertEquals(
                "400 Bad Request",
                explicitMessage.get(
                        "error"
                )
        );

        Map<?, ?> emptyMessage =
                UIHelpers.exceptionToJson(
                        new IllegalStateException(
                                ""
                        ),
                        500
                );

        assertEquals(
                IllegalStateException.class.getName(),
                emptyMessage.get(
                        "errorMessage"
                )
        );

        Map<?, ?> nullMessage =
                UIHelpers.exceptionToJson(
                        new RuntimeException(),
                        500
                );

        assertEquals(
                RuntimeException.class.getName(),
                nullMessage.get(
                        "errorMessage"
                )
        );
    }

    @Test
    void cf09SanitizeStreamNameExercisesStartCharacterBranches() {

        assertEquals(
                "valid_name",
                UIHelpers.sanitizeStreamName(
                        "valid$name"
                )
        );

        assertEquals(
                "_s",
                UIHelpers.sanitizeStreamName(
                        ""
                )
        );

        assertEquals(
                "_s_bad_name",
                UIHelpers.sanitizeStreamName(
                        "1bad$name"
                )
        );
    }

    @Test
    void cf10SanitizeTransferredStatsTransformsNestedStreams() {

        Map<String, Long> streams =
                new HashMap<>();

        streams.put(
                "$stream",
                7L
        );

        streams.put(
                "ok",
                9L
        );

        Map<String, Map<String, Long>> input =
                new HashMap<>();

        input.put(
                "600",
                streams
        );

        Map<String, Map<String, Long>> result =
                UIHelpers.sanitizeTransferredStats(
                        input
                );

        assertEquals(
                1,
                result.size()
        );

        Map<String, Long> sanitized =
                result.get(
                        "600"
                );

        assertNotNull(
                sanitized
        );

        assertEquals(
                7L,
                sanitized.get(
                        "_s_stream"
                )
        );

        assertEquals(
                9L,
                sanitized.get(
                        "ok"
                )
        );

        assertFalse(
                sanitized.containsKey(
                        "$stream"
                )
        );
    }

    private static Map<String, Object> logviewerConfig(
            int httpPort,
            int httpsPort) {

        Map<String, Object> config =
                new HashMap<>();

        config.put(
                DaemonConfig.LOGVIEWER_PORT,
                httpPort
        );

        config.put(
                DaemonConfig.LOGVIEWER_HTTPS_PORT,
                httpsPort
        );

        return config;
    }

    private static ServerConnector findSslConnector(
            Server server) {

        for (Connector connector :
                server.getConnectors()) {

            if (
                connector instanceof ServerConnector serverConnector
                &&
                serverConnector.getConnectionFactory(
                        SslConnectionFactory.class
                ) != null
            ) {
                return serverConnector;
            }
        }

        throw new AssertionError(
                "No SSL connector found."
        );
    }

    private static SslContextFactory.Server getSslContextFactory(
            ServerConnector connector) {

        SslConnectionFactory sslConnectionFactory =
                connector.getConnectionFactory(
                        SslConnectionFactory.class
                );

        assertNotNull(
                sslConnectionFactory
        );

        return sslConnectionFactory
                .getSslContextFactory();
    }

    private static Path createEmptyStore()
            throws Exception {

        Path path =
                Files.createTempFile(
                        "storm-isw2-cf-",
                        ".jks"
                );

        path.toFile()
                .deleteOnExit();

        KeyStore store =
                KeyStore.getInstance(
                        "JKS"
                );

        store.load(
                null,
                STORE_PASSWORD
        );

        try (
            OutputStream output =
                    Files.newOutputStream(path)
        ) {

            store.store(
                    output,
                    STORE_PASSWORD
            );
        }

        return path;
    }
}

package it.uniroma2.isw2.storm.testing.uihelpers.mt;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.Constants;
import org.apache.storm.DaemonConfig;
import org.apache.storm.daemon.ui.UIHelpers;
import org.apache.storm.generated.ClusterSummary;
import org.apache.storm.generated.SupervisorSummary;
import org.apache.storm.generated.TopologySummary;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlets.CrossOriginFilter;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersMutationTest {

    private static final char[] STORE_PASSWORD =
            "changeit".toCharArray();

    /*
     * Mutation-guided evolution of the frozen T_CF suite.
     *
     * Each test targets one or more surviving PIT mutants observed in
     * the T_CF mutation baseline.
     */

    /*
     * ============================================================
     * Batch 1
     * ============================================================
     */

    @Test
    void mt01JsonpBoundaryAndSerializationAreObservable() {

        String callback =
                "a".repeat(128);

        String json =
                "{\"value\":1}";

        assertEquals(
                callback + "(" + json + ");",
                UIHelpers.getJsonResponseBody(
                        json,
                        callback,
                        false
                )
        );

        Map<String, Integer> data =
                Map.of(
                        "value",
                        1
                );

        assertEquals(
                callback + "({\"value\":1});",
                UIHelpers.getJsonResponseBody(
                        data,
                        callback,
                        true
                )
        );
    }

    @Test
    void mt02SupervisorHttpsAndZeroPortBoundaryAreObservable() {

        Map<String, Object> secure =
                logviewerConfig(
                        8000,
                        8443
                );

        assertEquals(
                "https://supervisor.example:8443/api/v1/daemonlog?file=supervisor.log",
                UIHelpers.getSupervisorLogLink(
                        "supervisor.example",
                        secure
                )
        );

        Map<String, Object> zeroHttpsPort =
                logviewerConfig(
                        8000,
                        0
                );

        assertTrue(
                UIHelpers.isSecureLogviewer(
                        zeroHttpsPort
                )
        );

        assertEquals(
                0,
                UIHelpers.getLogviewerPort(
                        zeroHttpsPort
                )
        );
    }

    @Test
    void mt03JettyHttpConnectorConfigurationIsObservable() {

        Server server =
                UIHelpers.jettyCreateServer(
                        8080,
                        "127.0.0.1",
                        8443,
                        null,
                        false
                );

        assertEquals(
                1,
                server.getConnectors().length
        );

        ServerConnector connector =
                findHttpConnector(
                        server
                );

        assertEquals(
                8080,
                connector.getPort()
        );

        assertEquals(
                "127.0.0.1",
                connector.getHost()
        );

        assertEquals(
                200000L,
                connector.getIdleTimeout()
        );

        HttpConnectionFactory http =
                connector.getConnectionFactory(
                        HttpConnectionFactory.class
                );

        assertNotNull(
                http
        );

        assertTrue(
                http.getHttpConfiguration()
                        .getSendDateHeader()
        );
    }

    @Test
    void mt04ClusterResourcePercentagesAreObservable() {

        SupervisorSummary supervisor =
                new SupervisorSummary();

        supervisor.set_num_workers(
                4
        );

        supervisor.set_num_used_workers(
                1
        );

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
                        "mutation-user",
                        config
                );

        assertEquals(
                "25.000",
                ((String) result.get(
                        "memAssignedPercentUtil"
                )).replace(',', '.')
        );

        assertEquals(
                "25.000",
                ((String) result.get(
                        "cpuAssignedPercentUtil"
                )).replace(',', '.')
        );
    }

    /*
     * ============================================================
     * Batch 2
     * ============================================================
     */

    @Test
    void mt05SslFactoryConfigurationIsObservable()
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
                true,
                false,
                16384,
                false
        );

        ServerConnector connector =
                findSslConnector(
                        server
                );

        SslContextFactory.Server ssl =
                getSslContextFactory(
                        connector
                );

        assertTrue(
                Arrays.equals(
                        new String[]{
                                "SSL_RSA_WITH_RC4_128_MD5",
                                "SSL_RSA_WITH_RC4_128_SHA"
                        },
                        ssl.getExcludeCipherSuites()
                )
        );

        assertTrue(
                Arrays.equals(
                        new String[]{
                                "SSLv3"
                        },
                        ssl.getExcludeProtocols()
                )
        );

        assertFalse(
                ssl.isRenegotiationAllowed()
        );

        assertNotNull(
                ssl.getKeyStorePath()
        );

        assertTrue(
                ssl.getKeyStorePath()
                        .contains(
                                store.getFileName()
                                        .toString()
                        )
        );

        assertEquals(
                "JKS",
                ssl.getKeyStoreType()
        );

        assertNotNull(
                ssl.getTrustStorePath()
        );

        assertTrue(
                ssl.getTrustStorePath()
                        .contains(
                                store.getFileName()
                                        .toString()
                        )
        );

        assertEquals(
                "JKS",
                ssl.getTrustStoreType()
        );

        assertTrue(
                ssl.getNeedClientAuth()
        );

        assertFalse(
                ssl.getWantClientAuth()
        );

        HttpConnectionFactory http =
                connector.getConnectionFactory(
                        HttpConnectionFactory.class
                );

        assertNotNull(
                http
        );

        assertEquals(
                16384,
                http.getHttpConfiguration()
                        .getRequestHeaderSize()
        );

        assertNotNull(
                http.getHttpConfiguration()
                        .getCustomizer(
                                SecureRequestCustomizer.class
                        )
        );
    }

    @Test
    void mt06CorsConfigurationParametersAreObservable() {

        FilterHolder holder =
                UIHelpers.corsFilterHandle();

        assertEquals(
                "*",
                holder.getInitParameter(
                        CrossOriginFilter.ALLOWED_ORIGINS_PARAM
                )
        );

        assertEquals(
                "GET, POST, PUT",
                holder.getInitParameter(
                        CrossOriginFilter.ALLOWED_METHODS_PARAM
                )
        );

        assertEquals(
                "X-Requested-With, X-Requested-By, Access-Control-Allow-Origin,"
                        + " Content-Type, Content-Length, Accept, Origin",
                holder.getInitParameter(
                        CrossOriginFilter.ALLOWED_HEADERS_PARAM
                )
        );

        assertEquals(
                "*",
                holder.getInitParameter(
                        CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER
                )
        );
    }

    @Test
    void mt07SupervisorArithmeticAndGenericResourcesAreObservable() {

        SupervisorSummary supervisor =
                new SupervisorSummary();

        supervisor.set_supervisor_id(
                "sup-mt"
        );

        supervisor.set_host(
                "supervisor.example"
        );

        supervisor.set_uptime_secs(
                120
        );

        supervisor.set_num_workers(
                5
        );

        supervisor.set_num_used_workers(
                2
        );

        Map<String, Double> totalResources =
                new HashMap<>();

        totalResources.put(
                Constants.COMMON_TOTAL_MEMORY_RESOURCE_NAME,
                1000.0
        );

        totalResources.put(
                Constants.COMMON_CPU_RESOURCE_NAME,
                100.0
        );

        totalResources.put(
                "gpu",
                8.0
        );

        supervisor.set_total_resources(
                totalResources
        );

        supervisor.set_used_mem(
                250.0
        );

        supervisor.set_used_cpu(
                25.0
        );

        Map<String, Double> usedGeneric =
                new HashMap<>();

        usedGeneric.put(
                "gpu",
                3.0
        );

        supervisor.set_used_generic_resources(
                usedGeneric
        );

        supervisor.set_version(
                "3.0.0"
        );

        Map<String, Object> config =
                logviewerConfig(
                        8000,
                        -1
                );

        Map<String, Object> result =
                UIHelpers.getPrettifiedSupervisorMap(
                        supervisor,
                        config
                );

        assertEquals(
                3,
                ((Number) result.get(
                        "slotsFree"
                )).intValue()
        );

        assertEquals(
                750.0,
                ((Number) result.get(
                        "availMem"
                )).doubleValue()
        );

        assertEquals(
                75.0,
                ((Number) result.get(
                        "availCpu"
                )).doubleValue()
        );

        assertEquals(
                "gpu=8.0",
                result.get(
                        "totalGenericResources"
                )
        );

        assertEquals(
                "gpu=3.0",
                result.get(
                        "usedGenericResources"
                )
        );

        assertEquals(
                "gpu=5.0",
                result.get(
                        "availGenericResources"
                )
        );
    }

    @Test
    void mt08JettyAndSslPortBoundariesAreObservable() {

        Server sslServer =
                new Server();

        UIHelpers.configSsl(
                sslServer,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                false
        );

        assertEquals(
                0,
                sslServer.getConnectors()
                        .length
        );

        Server httpDisabled =
                UIHelpers.jettyCreateServer(
                        8080,
                        "127.0.0.1",
                        8443,
                        null,
                        true
                );

        assertEquals(
                0,
                httpDisabled.getConnectors()
                        .length
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

    private static ServerConnector findHttpConnector(
            Server server) {

        for (Connector connector :
                server.getConnectors()) {

            if (
                connector instanceof ServerConnector serverConnector
                &&
                serverConnector.getConnectionFactory(
                        HttpConnectionFactory.class
                ) != null
            ) {

                return serverConnector;
            }
        }

        throw new AssertionError(
                "No HTTP connector found."
        );
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
                        "storm-isw2-mt-",
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

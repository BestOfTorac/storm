package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;

import org.apache.storm.daemon.ui.FilterConfiguration;
import org.apache.storm.daemon.ui.UIHelpers;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;

class UIHelpersJettySslFilterBBTest {

    private static final char[] STORE_PASSWORD =
            "changeit".toCharArray();

    /*
     * F4.1 - Documentation-derived Jetty properties.
     */

    @Test
    void jetty01UsesConfiguredUiPort() {

        Server server =
                UIHelpers.jettyCreateServer(
                        8080,
                        null,
                        null,
                        false
                );

        ServerConnector connector =
                findHttpConnector(server);

        assertEquals(
                8080,
                connector.getPort()
        );
    }

    @Test
    void jetty07PreservesConfiguredUiHost() {

        Server server =
                UIHelpers.jettyCreateServer(
                        8080,
                        "localhost",
                        null,
                        false
                );

        ServerConnector connector =
                findHttpConnector(server);

        assertEquals(
                "localhost",
                connector.getHost()
        );
    }

    @Test
    void jetty08PreservesConfiguredHeaderBuffer() {

        Server server =
                UIHelpers.jettyCreateServer(
                        8080,
                        null,
                        null,
                        16384,
                        false
                );

        ServerConnector connector =
                findHttpConnector(server);

        HttpConnectionFactory http =
                connector.getConnectionFactory(
                        HttpConnectionFactory.class
                );

        assertNotNull(http);

        assertEquals(
                16384,
                http.getHttpConfiguration()
                        .getRequestHeaderSize()
        );
    }

    /*
     * F4.2 - Documentation-derived HTTPS properties.
     */

    @Test
    void ssl03UsesConfiguredHttpsPort() throws Exception {

        Path keyStore =
                createEmptyStore();

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
                8443,
                keyStore.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                new String(STORE_PASSWORD),
                null,
                null,
                null,
                false,
                false,
                false
        );

        ServerConnector connector =
                findSslConnector(server);

        assertEquals(
                8443,
                connector.getPort()
        );
    }

    @Test
    void ssl04RequiresClientAuthentication() throws Exception {

        Path keyStore =
                createEmptyStore();

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
                8443,
                keyStore.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                new String(STORE_PASSWORD),
                null,
                null,
                null,
                true,
                false,
                false
        );

        SslContextFactory.Server ssl =
                getSslContextFactory(
                        findSslConnector(server)
                );

        assertTrue(
                ssl.getNeedClientAuth()
        );
    }

    @Test
    void ssl05RequestsButDoesNotRequireClientAuthentication()
            throws Exception {

        Path keyStore =
                createEmptyStore();

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
                8443,
                keyStore.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                new String(STORE_PASSWORD),
                null,
                null,
                null,
                false,
                true,
                false
        );

        SslContextFactory.Server ssl =
                getSslContextFactory(
                        findSslConnector(server)
                );

        assertTrue(
                ssl.getWantClientAuth()
        );

        assertFalse(
                ssl.getNeedClientAuth()
        );
    }

    @Test
    void ssl06PreservesConfiguredHeaderBuffer()
            throws Exception {

        Path keyStore =
                createEmptyStore();

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
                8443,
                keyStore.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                new String(STORE_PASSWORD),
                null,
                null,
                null,
                false,
                false,
                16384,
                false
        );

        ServerConnector connector =
                findSslConnector(server);

        HttpConnectionFactory http =
                connector.getConnectionFactory(
                        HttpConnectionFactory.class
                );

        assertNotNull(http);

        assertEquals(
                16384,
                http.getHttpConfiguration()
                        .getRequestHeaderSize()
        );
    }

    @Test
    void ts01PreservesExplicitTrustStoreConfiguration()
            throws Exception {

        Path keyStore =
                createEmptyStore();

        Path trustStore =
                createEmptyStore();

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
                8443,
                keyStore.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                new String(STORE_PASSWORD),
                trustStore.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                false,
                false,
                false
        );

        SslContextFactory.Server ssl =
                getSslContextFactory(
                        findSslConnector(server)
                );

        String configuredTrustStore =
                ssl.getTrustStorePath();

        assertNotNull(
                configuredTrustStore
        );

        assertEquals(
                trustStore
                        .toAbsolutePath()
                        .normalize(),
                pathFromJettyLocation(
                        configuredTrustStore
                )
        );

        assertEquals(
                "JKS",
                ssl.getTrustStoreType()
        );
    }

    @Test
    void ts02AllowsHttpsWithoutExplicitTrustStore()
            throws Exception {

        Path keyStore =
                createEmptyStore();

        Server server =
                new Server();

        assertDoesNotThrow(
                () ->
                        UIHelpers.configSsl(
                                server,
                                8443,
                                keyStore.toString(),
                                new String(STORE_PASSWORD),
                                "JKS",
                                new String(STORE_PASSWORD),
                                null,
                                null,
                                null,
                                false,
                                false,
                                false
                        )
        );

        assertNotNull(
                findSslConnector(server)
        );
    }

    /*
     * F4.5/F4.6 - Documentation-derived custom filter properties.
     */

    @Test
    void fil02InstallsConfiguredCustomFilter() {

        ServletContextHandler context =
                new ServletContextHandler();

        FilterConfiguration configuration =
                customFilterConfiguration(
                        "documented-filter",
                        Map.of()
                );

        UIHelpers.configFilters(
                context,
                List.of(configuration)
        );

        FilterHolder holder =
                findFilter(
                        context,
                        "documented-filter"
                );

        assertNotNull(holder);
    }

    @Test
    void fil04PreservesConfiguredFilterParameters() {

        ServletContextHandler context =
                new ServletContextHandler();

        FilterConfiguration configuration =
                customFilterConfiguration(
                        "parameterized-filter",
                        Map.of(
                                "param1",
                                "value1"
                        )
                );

        UIHelpers.configFilters(
                context,
                List.of(configuration)
        );

        FilterHolder holder =
                findFilter(
                        context,
                        "parameterized-filter"
                );

        assertEquals(
                "value1",
                holder.getInitParameter(
                        "param1"
                )
        );
    }

    @Test
    void cfg03AttachesCustomFilterToUiServletContext() {

        Server server =
                new Server();

        HttpServlet servlet =
                new HttpServlet() {
                };

        FilterConfiguration configuration =
                customFilterConfiguration(
                        "configured-filter",
                        Map.of()
                );

        UIHelpers.configFilter(
                server,
                servlet,
                List.of(configuration)
        );

        assertTrue(
                server.getHandler()
                        instanceof ServletContextHandler
        );

        ServletContextHandler context =
                (ServletContextHandler)
                        server.getHandler();

        assertNotNull(
                findFilter(
                        context,
                        "configured-filter"
                )
        );
    }

    /*
     * Remaining F4 frames.
     *
     * These cases are characterization tests because the available Storm
     * documentation did not provide a sufficiently precise external oracle.
     * Their expected behaviour was frozen before implementation.
     */

    @Test
    void jetty02UsesFallbackPortWhenPortIsNull() {

        Server server =
                UIHelpers.jettyCreateServer(
                        null,
                        null,
                        null,
                        false
                );

        assertEquals(
                80,
                findHttpConnector(server).getPort()
        );
    }

    @Test
    void jetty03PreservesDynamicPortZero() {

        Server server =
                UIHelpers.jettyCreateServer(
                        0,
                        null,
                        null,
                        false
                );

        assertEquals(
                0,
                findHttpConnector(server).getPort()
        );
    }

    @Test
    void jetty04KeepsHttpWhenHttpsExistsAndHttpIsEnabled() {

        Server server =
                UIHelpers.jettyCreateServer(
                        8080,
                        null,
                        8443,
                        false
                );

        assertTrue(
                hasHttpConnector(server)
        );
    }

    @Test
    void jetty05OmitsHttpWhenHttpsExistsAndHttpIsDisabled() {

        Server server =
                UIHelpers.jettyCreateServer(
                        8080,
                        null,
                        8443,
                        true
                );

        assertFalse(
                hasHttpConnector(server)
        );
    }

    @Test
    void jetty06KeepsHttpWhenDisableFlagIsNull() {

        Server server =
                UIHelpers.jettyCreateServer(
                        8080,
                        null,
                        8443,
                        null
                );

        assertTrue(
                hasHttpConnector(server)
        );
    }

    @Test
    void ssl01DoesNotAddConnectorForNegativePort() {

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
                -1,
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

        assertFalse(
                hasSslConnector(server)
        );
    }

    @Test
    void ssl02DoesNotAddConnectorForZeroPort() {

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
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

        assertFalse(
                hasSslConnector(server)
        );
    }

    @Test
    void ssl07NeedClientAuthTakesPrecedenceOverWantClientAuth()
            throws Exception {

        Path keyStore =
                createEmptyStore();

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
                8443,
                keyStore.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                new String(STORE_PASSWORD),
                null,
                null,
                null,
                true,
                true,
                false
        );

        SslContextFactory.Server ssl =
                getSslContextFactory(
                        findSslConnector(server)
                );

        assertTrue(
                ssl.getNeedClientAuth()
        );

        assertFalse(
                ssl.getWantClientAuth()
        );
    }

    @Test
    void ts03DoesNotApplyPartialTrustStoreConfiguration()
            throws Exception {

        Path keyStore =
                createEmptyStore();

        Path partialTrustStore =
                createEmptyStore();

        Server server =
                new Server();

        UIHelpers.configSsl(
                server,
                8443,
                keyStore.toString(),
                new String(STORE_PASSWORD),
                "JKS",
                new String(STORE_PASSWORD),
                partialTrustStore.toString(),
                new String(STORE_PASSWORD),
                null,
                false,
                false,
                false
        );

        SslContextFactory.Server ssl =
                getSslContextFactory(
                        findSslConnector(server)
                );

        assertNull(
                ssl.getTrustStorePath()
        );
    }

    @Test
    void cors01CreatesStandardCorsFilterHolder() {

        FilterHolder holder =
                UIHelpers.corsFilterHandle();

        assertNotNull(
                holder.getRegistration()
        );

        assertEquals(
                "org.eclipse.jetty.ee10.servlets.CrossOriginFilter",
                holder.getRegistration()
                        .getClassName()
        );

        Map<String, String> params =
                holder.getInitParameters();

        assertEquals(
                "*",
                params.get("allowedOrigins")
        );

        assertEquals(
                "GET, POST, PUT",
                params.get("allowedMethods")
        );

        assertEquals(
                "X-Requested-With, X-Requested-By, Access-Control-Allow-Origin, Content-Type, Content-Length, Accept, Origin",
                params.get("allowedHeaders")
        );

        assertEquals(
                "*",
                params.get("Access-Control-Allow-Origin")
        );
    }

    @Test
    void access01CreatesAccessLoggingFilterHolder() {

        FilterHolder holder =
                UIHelpers.mkAccessLoggingFilterHandle();

        assertNotNull(
                holder.getRegistration()
        );

        assertEquals(
                "org.apache.storm.logging.filters.AccessLoggingFilter",
                holder.getRegistration()
                        .getClassName()
        );
    }

    @Test
    void fil01AddsStandardFiltersForEmptyCustomConfiguration() {

        ServletContextHandler context =
                new ServletContextHandler();

        UIHelpers.configFilters(
                context,
                List.of()
        );

        FilterHolder[] holders =
                context.getServletHandler()
                        .getFilters();

        assertNotNull(holders);
        assertEquals(2, holders.length);

        assertNotNull(
                holders[0].getRegistration()
        );

        assertNotNull(
                holders[1].getRegistration()
        );

        assertEquals(
                "org.eclipse.jetty.ee10.servlets.CrossOriginFilter",
                holders[0]
                        .getRegistration()
                        .getClassName()
        );

        assertEquals(
                "org.apache.storm.logging.filters.AccessLoggingFilter",
                holders[1]
                        .getRegistration()
                        .getClassName()
        );
    }

    @Test
    void fil03UsesFilterClassAsNameWhenNameIsAbsent() {

        ServletContextHandler context =
                new ServletContextHandler();

        FilterConfiguration configuration =
                customFilterConfiguration(
                        "temporary-name",
                        Map.of()
                );

        configuration.setFilterName(null);

        UIHelpers.configFilters(
                context,
                List.of(configuration)
        );

        FilterHolder holder =
                findFilter(
                        context,
                        TestFilter.class.getName()
                );

        assertEquals(
                TestFilter.class.getName(),
                holder.getName()
        );
    }

    @Test
    void fil05OmitsConfigurationWithoutFilterClass() {

        ServletContextHandler context =
                new ServletContextHandler();

        FilterConfiguration configuration =
                customFilterConfiguration(
                        "invalid-filter",
                        Map.of(
                                "param1",
                                "value1"
                        )
                );

        configuration.setFilterClass(null);

        UIHelpers.configFilters(
                context,
                List.of(configuration)
        );

        assertNull(
                findFilterOrNull(
                        context,
                        "invalid-filter"
                )
        );

        assertEquals(
                2,
                context.getServletHandler()
                        .getFilters()
                        .length
        );
    }

    @Test
    void fil06ConfiguresEachValidEntryFromMixedConfiguration() {

        ServletContextHandler context =
                new ServletContextHandler();

        FilterConfiguration named =
                customFilterConfiguration(
                        "named-filter",
                        Map.of(
                                "a",
                                "1"
                        )
                );

        FilterConfiguration unnamed =
                customFilterConfiguration(
                        "temporary",
                        Map.of(
                                "b",
                                "2"
                        )
                );

        unnamed.setFilterName(null);

        FilterConfiguration invalid =
                customFilterConfiguration(
                        "invalid-filter",
                        Map.of(
                                "c",
                                "3"
                        )
                );

        invalid.setFilterClass(null);

        UIHelpers.configFilters(
                context,
                List.of(
                        named,
                        unnamed,
                        invalid
                )
        );

        assertNotNull(
                findFilter(
                        context,
                        "named-filter"
                )
        );

        assertNotNull(
                findFilter(
                        context,
                        TestFilter.class.getName()
                )
        );

        assertNull(
                findFilterOrNull(
                        context,
                        "invalid-filter"
                )
        );

        assertEquals(
                4,
                context.getServletHandler()
                        .getFilters()
                        .length
        );
    }

    @Test
    void cfg01LeavesServerUnconfiguredForNullFilterList() {

        Server server =
                new Server();

        HttpServlet servlet =
                new HttpServlet() {
                };

        UIHelpers.configFilter(
                server,
                servlet,
                null
        );

        assertNull(
                server.getHandler()
        );
    }

    @Test
    void cfg02CreatesContextForEmptyFilterList() {

        Server server =
                new Server();

        HttpServlet servlet =
                new HttpServlet() {
                };

        UIHelpers.configFilter(
                server,
                servlet,
                List.of()
        );

        assertTrue(
                server.getHandler()
                        instanceof ServletContextHandler
        );

        ServletContextHandler context =
                (ServletContextHandler)
                        server.getHandler();

        assertEquals(
                1,
                context.getServletHandler()
                        .getServlets()
                        .length
        );

        assertEquals(
                2,
                context.getServletHandler()
                        .getFilters()
                        .length
        );
    }

    @Test
    void cfg04PreservesServletParametersAndCustomFilter() {

        Server server =
                new Server();

        HttpServlet servlet =
                new HttpServlet() {
                };

        FilterConfiguration configuration =
                customFilterConfiguration(
                        "cfg04-filter",
                        Map.of()
                );

        UIHelpers.configFilter(
                server,
                servlet,
                List.of(configuration),
                Map.of(
                        "servlet-param",
                        "servlet-value"
                )
        );

        assertTrue(
                server.getHandler()
                        instanceof ServletContextHandler
        );

        ServletContextHandler context =
                (ServletContextHandler)
                        server.getHandler();

        assertNotNull(
                findFilter(
                        context,
                        "cfg04-filter"
                )
        );

        assertEquals(
                1,
                context.getServletHandler()
                        .getServlets()
                        .length
        );

        assertEquals(
                "servlet-value",
                context.getServletHandler()
                        .getServlets()[0]
                        .getInitParameter(
                                "servlet-param"
                        )
        );
    }

    @Test
    void run01StartsOnDynamicPortWithoutConfigurator()
            throws Exception {

        String javaExecutable =
                Path.of(
                        System.getProperty("java.home"),
                        "bin",
                        "java"
                ).toString();

        String classPath =
                System.getProperty(
                        "surefire.test.class.path"
                );

        if (
            classPath == null
            || classPath.isBlank()
        ) {
            classPath =
                    System.getProperty(
                            "java.class.path"
                    );
        }

        Process process =
                new ProcessBuilder(
                        javaExecutable,
                        "-cp",
                        classPath,
                        RunJettyNoConfiguratorProbe.class
                                .getName()
                )
                        .redirectErrorStream(true)
                        .start();

        boolean finished =
                process.waitFor(
                        15,
                        java.util.concurrent.TimeUnit.SECONDS
                );

        if (!finished) {

            process.destroyForcibly();

            process.waitFor(
                    5,
                    java.util.concurrent.TimeUnit.SECONDS
            );

            throw new AssertionError(
                    "RUN-01 Jetty probe did not terminate."
            );
        }

        String output =
                new String(
                        process.getInputStream()
                                .readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8
                );

        assertEquals(
                0,
                process.exitValue(),
                output
        );
    }

    @Test
    void run02InvokesConfiguratorAndStartsDynamicPortServer()
            throws Exception {

        java.util.concurrent.atomic.AtomicReference<Server> captured =
                new java.util.concurrent.atomic.AtomicReference<>();

        Server startedServer =
                null;

        try {

            UIHelpers.stormRunJetty(
                    0,
                    (Integer) null,
                    captured::set
            );

            startedServer =
                    captured.get();

            assertNotNull(
                    startedServer
            );

            assertTrue(
                    startedServer.isStarted()
            );

            ServerConnector connector =
                    findHttpConnector(
                            startedServer
                    );

            assertTrue(
                    connector.getLocalPort() > 0
            );

        } finally {

            if (startedServer != null) {
                startedServer.stop();
            }
        }
    }

    private static boolean hasHttpConnector(
            Server server) {

        for (Connector connector :
                server.getConnectors()) {

            if (connector
                    instanceof ServerConnector serverConnector
                    && serverConnector.getConnectionFactory(
                            SslConnectionFactory.class
                    ) == null) {

                return true;
            }
        }

        return false;
    }

    private static boolean hasSslConnector(
            Server server) {

        for (Connector connector :
                server.getConnectors()) {

            if (connector
                    instanceof ServerConnector serverConnector
                    && serverConnector.getConnectionFactory(
                            SslConnectionFactory.class
                    ) != null) {

                return true;
            }
        }

        return false;
    }

    private static FilterHolder findFilterOrNull(
            ServletContextHandler context,
            String name) {

        FilterHolder[] holders =
                context.getServletHandler()
                        .getFilters();

        if (holders == null) {
            return null;
        }

        for (FilterHolder holder : holders) {

            if (name.equals(holder.getName())) {
                return holder;
            }
        }

        return null;
    }

    public static class RunJettyNoConfiguratorProbe {

        public static void main(
                String[] args) {

            try {

                UIHelpers.stormRunJetty(
                        0,
                        (Integer) null,
                        null
                );

                System.exit(0);

            } catch (Throwable failure) {

                failure.printStackTrace(
                        System.err
                );

                System.exit(1);
            }
        }
    }
    private static FilterConfiguration customFilterConfiguration(
            String name,
            Map<String, String> parameters) {

        FilterConfiguration configuration =
                new FilterConfiguration(
                        TestFilter.class.getName(),
                        parameters
                );

        configuration.setFilterClass(
                TestFilter.class.getName()
        );

        configuration.setFilterName(name);

        configuration.setFilterParams(parameters);

        return configuration;
    }

    private static ServerConnector findHttpConnector(
            Server server) {

        for (Connector connector :
                server.getConnectors()) {

            if (connector
                    instanceof ServerConnector serverConnector
                    && serverConnector.getConnectionFactory(
                            SslConnectionFactory.class
                    ) == null) {

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

            if (connector
                    instanceof ServerConnector serverConnector
                    && serverConnector.getConnectionFactory(
                            SslConnectionFactory.class
                    ) != null) {

                return serverConnector;
            }
        }

        throw new AssertionError(
                "No SSL connector found."
        );
    }

    private static SslContextFactory.Server
            getSslContextFactory(
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

    private static FilterHolder findFilter(
            ServletContextHandler context,
            String name) {

        FilterHolder[] holders =
                context.getServletHandler()
                        .getFilters();

        if (holders == null) {
            throw new AssertionError(
                    "No filters configured."
            );
        }

        for (FilterHolder holder : holders) {

            if (name.equals(holder.getName())) {
                return holder;
            }
        }

        throw new AssertionError(
                "Filter not found: " + name
        );
    }

    private static Path pathFromJettyLocation(
            String location) {

        if (location.startsWith("file:")) {

            return Path.of(
                    java.net.URI.create(location)
            )
                    .toAbsolutePath()
                    .normalize();
        }

        return Path.of(location)
                .toAbsolutePath()
                .normalize();
    }
    private static Path createEmptyStore()
            throws Exception {

        Path path =
                Files.createTempFile(
                        "storm-isw2-",
                        ".jks"
                );

        path.toFile()
                .deleteOnExit();

        KeyStore store =
                KeyStore.getInstance("JKS");

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

    public static class TestFilter
            implements Filter {

        @Override
        public void init(
                FilterConfig filterConfig) {
        }

        @Override
        public void doFilter(
                ServletRequest request,
                ServletResponse response,
                FilterChain chain)
                throws IOException,
                       ServletException {

            chain.doFilter(
                    request,
                    response
            );
        }

        @Override
        public void destroy() {
        }
    }
}

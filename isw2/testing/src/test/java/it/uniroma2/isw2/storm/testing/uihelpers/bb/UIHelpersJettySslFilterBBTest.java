package it.uniroma2.isw2.storm.testing.uihelpers.bb;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersJettySslFilterBBTest {

    private static final char[] STORE_PASSWORD =
            "changeit".toCharArray();

    /*
     * Final minimal F4 strict black-box suite.
     *
     * Only representative properties directly supported by Storm's
     * external UI/security configuration documentation are retained.
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

        assertEquals(
                8080,
                findHttpConnector(server).getPort()
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

        HttpConnectionFactory http =
                findHttpConnector(server)
                        .getConnectionFactory(
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
    void ssl03UsesConfiguredHttpsPort()
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
                false
        );

        assertEquals(
                8443,
                findSslConnector(server).getPort()
        );
    }

    @Test
    void ssl04RequiresClientAuthentication()
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

        assertNotNull(
                findFilter(
                        context,
                        "documented-filter"
                )
        );
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

            if (
                connector instanceof ServerConnector serverConnector
                &&
                serverConnector.getConnectionFactory(
                        SslConnectionFactory.class
                ) == null
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

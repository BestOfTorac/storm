package it.uniroma2.isw2.storm.testing.uihelpers.integration;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;

import org.apache.storm.daemon.ui.UIHelpers;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test between UIHelpers and the real Jetty HTTP stack.
 *
 * <p>No mocks are used. UIHelpers creates and configures a real Jetty
 * server, the server is started on an ephemeral local TCP port, and
 * a real HTTP request is sent through the JDK HTTP client.</p>
 */
class UIHelpersJettyIT {

    @Test
    void shouldServeHttpRequestThroughJettyConfiguredByUIHelpers() throws Exception {

        /*
         * Port 0 asks the operating system to allocate an available
         * ephemeral port. This avoids hard-coded-port conflicts in CI.
         */
        Server server =
                UIHelpers.jettyCreateServer(
                        0,
                        "127.0.0.1",
                        null,
                        false
                );

        try {

            HttpServlet servlet = new HttpServlet() {

                @Override
                protected void doGet(
                        HttpServletRequest request,
                        HttpServletResponse response
                ) throws IOException {

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/plain");
                    response.getWriter().write("uihelpers-it-ok");
                }
            };

            /*
             * An empty, non-null filter configuration is intentional.
             * UIHelpers therefore installs its ServletContextHandler,
             * CORS filter and access-logging filter around the servlet.
             */
            UIHelpers.configFilter(
                    server,
                    servlet,
                    Collections.emptyList()
            );

            server.start();

            assertEquals(
                    1,
                    server.getConnectors().length,
                    "Exactly one HTTP connector should be configured"
            );

            ServerConnector connector =
                    (ServerConnector) server.getConnectors()[0];

            int localPort =
                    connector.getLocalPort();

            assertTrue(
                    localPort > 0,
                    "Jetty should bind to an ephemeral local port"
            );

            HttpClient client =
                    HttpClient.newHttpClient();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            "http://127.0.0.1:"
                                                    + localPort
                                                    + "/"
                                    )
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            assertEquals(
                    HttpServletResponse.SC_OK,
                    response.statusCode()
            );

            assertEquals(
                    "uihelpers-it-ok",
                    response.body()
            );

        } finally {

            /*
             * Always release sockets/resources, also if an assertion
             * or the HTTP request fails.
             */
            if (server.isStarted() || server.isStarting()) {
                server.stop();
            }

            server.destroy();
        }
    }
}
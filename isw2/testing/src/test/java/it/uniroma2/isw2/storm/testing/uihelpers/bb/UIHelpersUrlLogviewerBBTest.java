package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.storm.DaemonConfig;
import org.apache.storm.daemon.ui.UIHelpers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UIHelpersUrlLogviewerBBTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("urlFormattingCases")
    void testF2_1URLFormatting(
            String caseId,
            String format,
            Object[] args,
            String expected) {

        assertEquals(
                expected,
                UIHelpers.urlFormat(format, args)
        );
    }

    static Stream<Arguments> urlFormattingCases() {
        return Stream.of(
                Arguments.of(
                        "URL-01",
                        "/x/%s",
                        new Object[]{"abc"},
                        "/x/abc"
                ),
                Arguments.of(
                        "URL-02",
                        "/x/%s/%s",
                        new Object[]{"host", 8080},
                        "/x/host/8080"
                ),
                Arguments.of(
                        "URL-03",
                        "/x/%s",
                        new Object[]{"a b"},
                        "/x/a+b"
                ),
                Arguments.of(
                        "URL-04",
                        "/x/%s",
                        new Object[]{"a/b?c=d&e"},
                        "/x/a%2Fb%3Fc%3Dd%26e"
                ),
                Arguments.of(
                        "URL-05",
                        "/x/%s",
                        new Object[]{null},
                        "/x/null"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("securityCases")
    void testF2_2LogviewerSecurityMode(
            String caseId,
            Integer httpsPort,
            boolean expected) {

        Map<String, Object> config =
                logviewerConfig(8000, httpsPort);

        assertEquals(
                expected,
                UIHelpers.isSecureLogviewer(config)
        );
    }

    static Stream<Arguments> securityCases() {
        return Stream.of(
                Arguments.of("SEC-01", null, false),
                Arguments.of("SEC-02", -1, false),
                Arguments.of("SEC-03", 0, true),
                Arguments.of("SEC-04", 8443, true)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("portCases")
    void testF2_3LogviewerPortSelection(
            String caseId,
            Integer httpsPort,
            int expected) {

        Map<String, Object> config =
                logviewerConfig(8000, httpsPort);

        assertEquals(
                expected,
                UIHelpers.getLogviewerPort(config)
        );
    }

    static Stream<Arguments> portCases() {
        return Stream.of(
                Arguments.of("PORT-01", null, 8000),
                Arguments.of("PORT-02", -1, 8000),
                Arguments.of("PORT-03", 8443, 8443)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("genericLogviewerLinkCases")
    void testF2_4GenericLogviewerLinks(
            String caseId,
            Integer httpsPort,
            String filename,
            String expected) {

        Map<String, Object> config =
                logviewerConfig(8000, httpsPort);

        assertEquals(
                expected,
                UIHelpers.getLogviewerLink(
                        "worker.example",
                        filename,
                        config,
                        6700
                )
        );
    }

    static Stream<Arguments> genericLogviewerLinkCases() {
        return Stream.of(
                Arguments.of(
                        "LOG-01",
                        null,
                        "worker.log",
                        "http://worker.example:8000/api/v1/log?file=worker.log"
                ),
                Arguments.of(
                        "LOG-02",
                        8443,
                        "worker.log",
                        "https://worker.example:8443/api/v1/log?file=worker.log"
                ),
                Arguments.of(
                        "LOG-03",
                        null,
                        "worker log",
                        "http://worker.example:8000/api/v1/log?file=worker+log"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("daemonLogLinkCases")
    void testF2_5NimbusAndSupervisorDaemonLogLinks(
            String caseId,
            boolean nimbus,
            Integer httpsPort,
            String host,
            String expected) {

        Map<String, Object> config =
                logviewerConfig(8000, httpsPort);

        String actual;

        if (nimbus) {
            actual =
                    UIHelpers.getNimbusLogLink(
                            host,
                            config
                    );
        } else {
            actual =
                    UIHelpers.getSupervisorLogLink(
                            host,
                            config
                    );
        }

        assertEquals(expected, actual);
    }

    static Stream<Arguments> daemonLogLinkCases() {
        return Stream.of(
                Arguments.of(
                        "DAEMON-01",
                        true,
                        null,
                        "nimbus.example",
                        "http://nimbus.example:8000/api/v1/daemonlog?file=nimbus.log"
                ),
                Arguments.of(
                        "DAEMON-02",
                        true,
                        8443,
                        "nimbus.example",
                        "https://nimbus.example:8443/api/v1/daemonlog?file=nimbus.log"
                ),
                Arguments.of(
                        "DAEMON-03",
                        false,
                        null,
                        "supervisor.example",
                        "http://supervisor.example:8000/api/v1/daemonlog?file=supervisor.log"
                ),
                Arguments.of(
                        "DAEMON-04",
                        false,
                        8443,
                        "supervisor.example",
                        "https://supervisor.example:8443/api/v1/daemonlog?file=supervisor.log"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("workerLogLinkCases")
    void testF2_6WorkerLogLinks(
            String caseId,
            Integer httpsPort,
            String topologyId) {

        Map<String, Object> config =
                logviewerConfig(8000, httpsPort);

        int workerPort =
                6700;

        String host =
                "worker.example";

        String expected =
                expectedWorkerLogLink(
                        host,
                        workerPort,
                        topologyId,
                        8000,
                        httpsPort
                );

        assertEquals(
                expected,
                UIHelpers.getWorkerLogLink(
                        host,
                        workerPort,
                        config,
                        topologyId
                )
        );
    }

    static Stream<Arguments> workerLogLinkCases() {
        return Stream.of(
                Arguments.of(
                        "WORKERLOG-HTTP",
                        null,
                        "topology-1"
                ),
                Arguments.of(
                        "WORKERLOG-HTTPS",
                        8443,
                        "topology-1"
                ),
                Arguments.of(
                        "WORKERLOG-ENCODED-ID",
                        null,
                        "topology 1"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("workerDumpLinkCases")
    void testF2_7WorkerDumpLinks(
            String caseId,
            Integer httpsPort,
            String expected) {

        Map<String, Object> config =
                logviewerConfig(8000, httpsPort);

        assertEquals(
                expected,
                UIHelpers.getWorkerDumpLink(
                        "worker.example",
                        6700L,
                        "topology-1",
                        config
                )
        );
    }

    static Stream<Arguments> workerDumpLinkCases() {
        return Stream.of(
                Arguments.of(
                        "DUMP-01",
                        null,
                        "http://worker.example:8000/api/v1/dumps/topology-1/worker.example%3A6700"
                ),
                Arguments.of(
                        "DUMP-02",
                        8443,
                        "https://worker.example:8443/api/v1/dumps/topology-1/worker.example%3A6700"
                )
        );
    }

    private static Map<String, Object> logviewerConfig(
            int httpPort,
            Integer httpsPort) {

        Map<String, Object> config =
                new HashMap<>();

        config.put(
                DaemonConfig.LOGVIEWER_PORT,
                httpPort
        );

        if (httpsPort != null) {
            config.put(
                    DaemonConfig.LOGVIEWER_HTTPS_PORT,
                    httpsPort
            );
        }

        return config;
    }

    private static String expectedWorkerLogLink(
            String host,
            int workerPort,
            String topologyId,
            int httpPort,
            Integer httpsPort) {

        boolean secure =
                httpsPort != null
                        && httpsPort >= 0;

        String scheme =
                secure
                        ? "https"
                        : "http";

        int logviewerPort =
                secure
                        ? httpsPort
                        : httpPort;

        String filename =
                topologyId
                        + File.separator
                        + workerPort
                        + File.separator
                        + "worker.log";

        return scheme
                + "://"
                + encode(host)
                + ":"
                + logviewerPort
                + "/api/v1/log?file="
                + encode(filename);
    }

    private static String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}

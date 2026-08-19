package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.SecurityContext;
import org.apache.storm.Constants;
import org.apache.storm.daemon.ui.UIHelpers;
import org.apache.storm.generated.SupervisorPageInfo;
import org.apache.storm.generated.SupervisorSummary;
import org.apache.storm.generated.WorkerSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UIHelpersSupervisorWorkerBBTest {

    private static final double EPSILON = 0.000001;

    /*
     * F6 strict black-box suite.
     *
     * Only frames whose fixtures and expected properties are derivable
     * from Storm's public REST/configuration contracts are implemented.
     *
     * No UIHelpers implementation is used to derive these tests.
     */

    @Test
    void workers03RepresentsOneSuppliedWorker() {

        SupervisorPageInfo page =
                new SupervisorPageInfo();

        page.set_worker_summaries(
                List.of(
                        worker(
                                "sup-1",
                                "worker-host",
                                6701,
                                "topology-1",
                                "Topology One",
                                4,
                                120
                        )
                )
        );

        List<Map<String, Object>> workers =
                workerMaps(
                        UIHelpers.getWorkerSummaries(
                                page,
                                httpConfig()
                        )
                );

        assertEquals(1, workers.size());

        assertEquals(
                "sup-1",
                workers.get(0).get("supervisorId")
        );

        assertEquals(
                "worker-host",
                workers.get(0).get("host")
        );
    }

    @Test
    void workers04RepresentsAllSuppliedWorkers() {

        SupervisorPageInfo page =
                new SupervisorPageInfo();

        page.set_worker_summaries(
                List.of(
                        worker(
                                "sup-1",
                                "host-a",
                                6701,
                                "topology-a",
                                "Topology A",
                                3,
                                100
                        ),
                        worker(
                                "sup-2",
                                "host-b",
                                6702,
                                "topology-b",
                                "Topology B",
                                5,
                                200
                        )
                )
        );

        List<Map<String, Object>> workers =
                workerMaps(
                        UIHelpers.getWorkerSummaries(
                                page,
                                httpConfig()
                        )
                );

        assertEquals(2, workers.size());

        List<String> hosts =
                workers.stream()
                        .map(worker ->
                                String.valueOf(
                                        worker.get("host")
                                )
                        )
                        .toList();

        assertTrue(hosts.contains("host-a"));
        assertTrue(hosts.contains("host-b"));
    }

    @Test
    void worker02PreservesDocumentedPositiveValues() {

        WorkerSummary source =
                worker(
                        "sup-positive",
                        "worker-positive",
                        6707,
                        "topology-positive",
                        "Positive Topology",
                        8,
                        167
                );

        source.set_assigned_memonheap(704.0);
        source.set_assigned_memoffheap(80.0);
        source.set_assigned_cpu(130.0);

        Map<String, Long> tasks =
                new HashMap<>();

        tasks.put("word", 5L);

        source.set_component_to_num_tasks(tasks);

        SupervisorPageInfo page =
                new SupervisorPageInfo();

        page.set_worker_summaries(
                List.of(source)
        );

        List<Map<String, Object>> workers =
                workerMaps(
                        UIHelpers.getWorkerSummaries(
                                page,
                                httpConfig()
                        )
                );

        assertEquals(1, workers.size());

        Map<String, Object> actual =
                workers.get(0);

        assertEquals(
                "sup-positive",
                actual.get("supervisorId")
        );

        assertEquals(
                "worker-positive",
                actual.get("host")
        );

        assertInt(actual, "port", 6707);

        assertEquals(
                "topology-positive",
                actual.get("topologyId")
        );

        assertEquals(
                "Positive Topology",
                actual.get("topologyName")
        );

        assertInt(
                actual,
                "executorsTotal",
                8
        );

        assertDouble(
                actual,
                "assignedMemOnHeap",
                704.0
        );

        assertDouble(
                actual,
                "assignedMemOffHeap",
                80.0
        );

        assertDouble(
                actual,
                "assignedCpu",
                130.0
        );

        assertInt(
                actual,
                "uptimeSeconds",
                167
        );

        Object componentTasks =
                actual.get("componentNumTasks");

        assertNotNull(componentTasks);
        assertTrue(componentTasks instanceof Map<?, ?>);

        Map<?, ?> componentMap =
                (Map<?, ?>) componentTasks;

        assertEquals(
                5L,
                ((Number) componentMap.get("word")).longValue()
        );
    }

    @Test
    void suplist02RepresentsOneSuppliedSupervisor() {

        SupervisorSummary source =
                supervisor(
                        "sup-one",
                        "supervisor-one",
                        4,
                        1,
                        358
                );

        Map<String, Object> result =
                UIHelpers.getSupervisorSummary(
                        List.of(source),
                        httpSecurityContext(),
                        httpConfig()
                );

        List<Map<String, Object>> supervisors =
                mapList(
                        result,
                        "supervisors"
                );

        assertEquals(1, supervisors.size());

        assertEquals(
                "sup-one",
                supervisors.get(0).get("id")
        );

        assertEquals(
                "supervisor-one",
                supervisors.get(0).get("host")
        );
    }

    @Test
    void suplist03RepresentsAllSuppliedSupervisors() {

        SupervisorSummary supervisorA =
                supervisor(
                        "sup-a",
                        "host-a",
                        4,
                        1,
                        100
                );

        SupervisorSummary supervisorB =
                supervisor(
                        "sup-b",
                        "host-b",
                        6,
                        2,
                        200
                );

        Map<String, Object> result =
                UIHelpers.getSupervisorSummary(
                        List.of(
                                supervisorA,
                                supervisorB
                        ),
                        httpSecurityContext(),
                        httpConfig()
                );

        List<Map<String, Object>> supervisors =
                mapList(
                        result,
                        "supervisors"
                );

        assertEquals(2, supervisors.size());

        List<String> ids =
                supervisors.stream()
                        .map(supervisor ->
                                String.valueOf(
                                        supervisor.get("id")
                                )
                        )
                        .toList();

        assertTrue(ids.contains("sup-a"));
        assertTrue(ids.contains("sup-b"));
    }

    @Test
    void suppage03ExposesSupervisorAndWorkerCollections() {

        SupervisorSummary supervisor =
                supervisor(
                        "sup-page",
                        "page-host",
                        4,
                        1,
                        400
                );

        WorkerSummary worker =
                worker(
                        "sup-page",
                        "page-host",
                        6701,
                        "page-topology",
                        "Page Topology",
                        3,
                        120
                );

        SupervisorPageInfo page =
                new SupervisorPageInfo();

        page.set_supervisor_summaries(
                List.of(supervisor)
        );

        page.set_worker_summaries(
                List.of(worker)
        );

        Map<String, Object> result =
                UIHelpers.getSupervisorPageInfo(
                        page,
                        httpConfig()
                );

        List<Map<String, Object>> supervisors =
                mapList(
                        result,
                        "supervisors"
                );

        List<Map<String, Object>> workers =
                mapList(
                        result,
                        "workers"
                );

        assertEquals(1, supervisors.size());
        assertEquals(1, workers.size());

        assertEquals(
                "sup-page",
                supervisors.get(0).get("id")
        );

        assertEquals(
                "page-topology",
                workers.get(0).get("topologyId")
        );
    }

    private static SupervisorSummary supervisor(
            String id,
            String host,
            int slotsTotal,
            int slotsUsed,
            int uptimeSeconds) {

        SupervisorSummary summary =
                new SupervisorSummary();

        summary.set_supervisor_id(id);
        summary.set_host(host);
        summary.set_uptime_secs(uptimeSeconds);
        summary.set_num_workers(slotsTotal);
        summary.set_num_used_workers(slotsUsed);
        summary.set_version("3.0.0");

        Map<String, Double> resources =
                new HashMap<>();

        resources.put(
                Constants.COMMON_TOTAL_MEMORY_RESOURCE_NAME,
                4096.0
        );

        resources.put(
                Constants.COMMON_CPU_RESOURCE_NAME,
                400.0
        );

        summary.set_total_resources(resources);
        summary.set_used_mem(1024.0);
        summary.set_used_cpu(100.0);

        summary.set_used_generic_resources(
                new HashMap<>()
        );

        return summary;
    }

    private static WorkerSummary worker(
            String supervisorId,
            String host,
            int port,
            String topologyId,
            String topologyName,
            int executors,
            int uptimeSeconds) {

        WorkerSummary summary =
                new WorkerSummary();

        summary.set_supervisor_id(supervisorId);
        summary.set_host(host);
        summary.set_port(port);
        summary.set_topology_id(topologyId);
        summary.set_topology_name(topologyName);
        summary.set_num_executors(executors);
        summary.set_uptime_secs(uptimeSeconds);

        summary.set_assigned_memonheap(512.0);
        summary.set_assigned_memoffheap(64.0);
        summary.set_assigned_cpu(100.0);

        summary.set_component_to_num_tasks(
                new HashMap<>()
        );

        return summary;
    }

    private static Map<String, Object> httpConfig() {

        Map<String, Object> config =
                new HashMap<>();

        /*
         * External Storm configuration / REST examples use
         * the HTTP logviewer on port 8000.
         */

        config.put(
                "logviewer.port",
                8000
        );

        config.put(
                "logviewer.https.port",
                0
        );

        config.put(
                "scheduler.display.resource",
                true
        );

        return config;
    }

    private static SecurityContext httpSecurityContext() {

        SecurityContext context =
                mock(SecurityContext.class);

        when(
                context.isSecure()
        ).thenReturn(false);

        return context;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> workerMaps(
            List<Map> input) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Map value : input) {
            result.add(
                    (Map<String, Object>) value
            );
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> mapList(
            Map<String, Object> result,
            String key) {

        Object value =
                result.get(key);

        assertNotNull(
                value,
                key
        );

        assertTrue(
                value instanceof List<?>,
                key
        );

        List<?> list =
                (List<?>) value;

        List<Map<String, Object>> maps =
                new ArrayList<>();

        for (Object item : list) {

            assertTrue(
                    item instanceof Map<?, ?>,
                    key
            );

            maps.add(
                    (Map<String, Object>) item
            );
        }

        return maps;
    }

    private static void assertInt(
            Map<String, Object> result,
            String key,
            int expected) {

        Object value =
                result.get(key);

        assertNotNull(value, key);
        assertTrue(value instanceof Number, key);

        assertEquals(
                expected,
                ((Number) value).intValue(),
                key
        );
    }

    private static void assertDouble(
            Map<String, Object> result,
            String key,
            double expected) {

        Object value =
                result.get(key);

        assertNotNull(value, key);
        assertTrue(value instanceof Number, key);

        assertEquals(
                expected,
                ((Number) value).doubleValue(),
                EPSILON,
                key
        );
    }
}

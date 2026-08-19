package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.daemon.ui.UIHelpers;
import org.apache.storm.generated.BoltAggregateStats;
import org.apache.storm.generated.CommonAggregateStats;
import org.apache.storm.generated.ComponentAggregateStats;
import org.apache.storm.generated.DebugOptions;
import org.apache.storm.generated.ExecutorInfo;
import org.apache.storm.generated.ExecutorSummary;
import org.apache.storm.generated.SpecificAggregateStats;
import org.apache.storm.generated.SpoutAggregateStats;
import org.apache.storm.generated.TopologyHistoryInfo;
import org.apache.storm.generated.TopologyInfo;
import org.apache.storm.generated.TopologyPageInfo;
import org.apache.storm.generated.TopologyStats;
import org.apache.storm.generated.TopologySummary;
import org.apache.storm.generated.WorkerSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersTopologyBBTest {

    private static final double EPSILON = 0.000001;

    /*
     * F7 strict black-box suite.
     *
     * Every implemented oracle is derived from Storm's public REST
     * documentation and public generated DTO contracts.
     *
     * No UIHelpers implementation was inspected to derive these tests.
     */

    @Test
    void top02PreservesDocumentedPositiveTopologyValues() {

        TopologySummary source =
                topologySummary(
                        "topology-1",
                        "Topology One",
                        28,
                        28,
                        3
                );

        source.set_requested_memonheap(640.0);
        source.set_requested_memoffheap(128.0);
        source.set_requested_cpu(80.0);

        source.set_assigned_memonheap(640.0);
        source.set_assigned_memoffheap(128.0);
        source.set_assigned_cpu(80.0);

        Map<String, Object> actual =
                UIHelpers.getTopologyMap(source);

        assertEquals(
                "topology-1",
                actual.get("id")
        );

        assertEquals(
                "Topology One",
                actual.get("name")
        );

        assertInt(
                actual,
                "tasksTotal",
                28
        );

        assertInt(
                actual,
                "executorsTotal",
                28
        );

        assertInt(
                actual,
                "workersTotal",
                3
        );

        assertDouble(
                actual,
                "requestedMemOnHeap",
                640.0
        );

        assertDouble(
                actual,
                "requestedMemOffHeap",
                128.0
        );

        assertDouble(
                actual,
                "requestedTotalMem",
                768.0
        );

        assertDouble(
                actual,
                "requestedCpu",
                80.0
        );

        assertDouble(
                actual,
                "assignedMemOnHeap",
                640.0
        );

        assertDouble(
                actual,
                "assignedMemOffHeap",
                128.0
        );

        assertDouble(
                actual,
                "assignedTotalMem",
                768.0
        );

        assertDouble(
                actual,
                "assignedCpu",
                80.0
        );
    }

    @Test
    void toplist02RepresentsOneSuppliedTopology() {

        TopologySummary source =
                topologySummary(
                        "topology-one",
                        "Topology One",
                        10,
                        8,
                        2
                );

        Map<String, Object> result =
                UIHelpers.getAllTopologiesSummary(
                        List.of(source),
                        uiConfig()
                );

        List<Map<String, Object>> topologies =
                mapList(
                        result,
                        "topologies"
                );

        assertEquals(1, topologies.size());

        assertEquals(
                "topology-one",
                topologies.get(0).get("id")
        );
    }

    @Test
    void toplist03RepresentsAllSuppliedTopologies() {

        TopologySummary first =
                topologySummary(
                        "topology-a",
                        "Topology A",
                        10,
                        8,
                        2
                );

        TopologySummary second =
                topologySummary(
                        "topology-b",
                        "Topology B",
                        20,
                        15,
                        3
                );

        Map<String, Object> result =
                UIHelpers.getAllTopologiesSummary(
                        List.of(first, second),
                        uiConfig()
                );

        List<Map<String, Object>> topologies =
                mapList(
                        result,
                        "topologies"
                );

        assertEquals(2, topologies.size());

        List<String> ids =
                topologies.stream()
                        .map(topology ->
                                String.valueOf(
                                        topology.get("id")
                                )
                        )
                        .toList();

        assertTrue(ids.contains("topology-a"));
        assertTrue(ids.contains("topology-b"));
    }
    @Test
    void topwork02RepresentsOneWorkerLocation() {

        TopologyInfo topology =
                topologyInfo(
                        List.of(
                                executor(
                                        1,
                                        1,
                                        "spout",
                                        "worker-a",
                                        6701
                                )
                        )
                );

        Map<String, Object> result =
                UIHelpers.getTopologyWorkers(
                        topology,
                        uiConfig()
                );

        List<Map<String, Object>> locations =
                mapList(
                        result,
                        "hostPortList"
                );

        assertEquals(1, locations.size());

        assertEquals(
                "worker-a",
                locations.get(0).get("host")
        );

        assertInt(
                locations.get(0),
                "port",
                6701
        );
    }

    @Test
    void topwork03RepresentsAllDistinctWorkerLocations() {

        TopologyInfo topology =
                topologyInfo(
                        List.of(
                                executor(
                                        1,
                                        1,
                                        "spout",
                                        "worker-a",
                                        6701
                                ),
                                executor(
                                        2,
                                        2,
                                        "bolt",
                                        "worker-b",
                                        6702
                                )
                        )
                );

        Map<String, Object> result =
                UIHelpers.getTopologyWorkers(
                        topology,
                        uiConfig()
                );

        List<Map<String, Object>> locations =
                mapList(
                        result,
                        "hostPortList"
                );

        assertEquals(2, locations.size());

        List<String> hosts =
                locations.stream()
                        .map(location ->
                                String.valueOf(
                                        location.get("host")
                                )
                        )
                        .toList();

        assertTrue(hosts.contains("worker-a"));
        assertTrue(hosts.contains("worker-b"));
    }

    @Test
    void history02ExposesOneTopologyIdentifier() {

        TopologyHistoryInfo history =
                new TopologyHistoryInfo(
                        List.of(
                                "topology-history-one"
                        )
                );

        Map<String, Object> result =
                UIHelpers.getTopologyHistoryInfo(history);

        List<?> ids =
                rawList(
                        result,
                        "topo-history"
                );

        assertEquals(1, ids.size());

        assertEquals(
                "topology-history-one",
                ids.get(0)
        );
    }

    @Test
    void history03ExposesMultipleTopologyIdentifiers() {

        TopologyHistoryInfo history =
                new TopologyHistoryInfo(
                        List.of(
                                "topology-history-a",
                                "topology-history-b",
                                "topology-history-c"
                        )
                );

        Map<String, Object> result =
                UIHelpers.getTopologyHistoryInfo(history);

        List<?> ids =
                rawList(
                        result,
                        "topo-history"
                );

        assertEquals(3, ids.size());

        assertTrue(
                ids.contains(
                        "topology-history-a"
                )
        );

        assertTrue(
                ids.contains(
                        "topology-history-b"
                )
        );

        assertTrue(
                ids.contains(
                        "topology-history-c"
                )
        );
    }

    private static TopologySummary topologySummary(
            String id,
            String name,
            int tasks,
            int executors,
            int workers) {

        TopologySummary summary =
                new TopologySummary();

        summary.set_id(id);
        summary.set_name(name);
        summary.set_num_tasks(tasks);
        summary.set_num_executors(executors);
        summary.set_num_workers(workers);
        summary.set_uptime_secs(365);
        summary.set_status("ACTIVE");

        summary.set_owner("owner");
        summary.set_replication_count(1);

        summary.set_storm_version("3.0.0");
        summary.set_topology_version("1");

        summary.set_requested_memonheap(640.0);
        summary.set_requested_memoffheap(128.0);
        summary.set_requested_cpu(80.0);

        summary.set_assigned_memonheap(640.0);
        summary.set_assigned_memoffheap(128.0);
        summary.set_assigned_cpu(80.0);

        summary.set_requested_generic_resources(
                new HashMap<>()
        );

        summary.set_assigned_generic_resources(
                new HashMap<>()
        );

        return summary;
    }

    private static TopologyPageInfo populatedTopologyPage() {

        TopologyPageInfo page =
                new TopologyPageInfo(
                        "topology-page"
                );

        page.set_name("Topology Page");
        page.set_uptime_secs(600);
        page.set_status("ACTIVE");

        page.set_num_tasks(8);
        page.set_num_workers(1);
        page.set_num_executors(4);

        page.set_owner("owner");
        page.set_replication_count(1);

        page.set_storm_version("3.0.0");
        page.set_topology_version("1");

        page.set_sched_status("");

        page.set_topology_conf("{}");

        page.set_requested_memonheap(640.0);
        page.set_requested_memoffheap(128.0);
        page.set_requested_cpu(80.0);

        page.set_assigned_memonheap(640.0);
        page.set_assigned_memoffheap(128.0);
        page.set_assigned_cpu(80.0);

        page.set_requested_generic_resources(
                new HashMap<>()
        );

        page.set_assigned_generic_resources(
                new HashMap<>()
        );

        page.set_workers(
                List.of(
                        worker()
                )
        );

        page.set_topology_stats(
                emptyTopologyStats()
        );

        Map<String, ComponentAggregateStats> spouts =
                new HashMap<>();

        spouts.put(
                "spout-component",
                componentStats(true)
        );

        page.set_id_to_spout_agg_stats(
                spouts
        );

        Map<String, ComponentAggregateStats> bolts =
                new HashMap<>();

        bolts.put(
                "bolt-component",
                componentStats(false)
        );

        page.set_id_to_bolt_agg_stats(
                bolts
        );

        DebugOptions debug =
                new DebugOptions();

        debug.set_enable(false);
        debug.set_samplingpct(10.0);

        page.set_debug_options(debug);

        return page;
    }

    private static WorkerSummary worker() {

        WorkerSummary worker =
                new WorkerSummary();

        worker.set_supervisor_id("supervisor-1");
        worker.set_host("worker-host");
        worker.set_port(6701);

        worker.set_topology_id(
                "topology-page"
        );

        worker.set_topology_name(
                "Topology Page"
        );

        worker.set_num_executors(4);
        worker.set_uptime_secs(500);

        worker.set_assigned_memonheap(640.0);
        worker.set_assigned_memoffheap(128.0);
        worker.set_assigned_cpu(80.0);

        worker.set_component_to_num_tasks(
                Map.of(
                        "spout-component",
                        4L,
                        "bolt-component",
                        4L
                )
        );

        return worker;
    }

    private static TopologyStats emptyTopologyStats() {

        TopologyStats stats =
                new TopologyStats();

        stats.set_window_to_emitted(
                new HashMap<>()
        );

        stats.set_window_to_transferred(
                new HashMap<>()
        );

        stats.set_window_to_acked(
                new HashMap<>()
        );

        stats.set_window_to_failed(
                new HashMap<>()
        );

        return stats;
    }

    private static ComponentAggregateStats componentStats(
            boolean spout) {

        CommonAggregateStats common =
                new CommonAggregateStats();

        common.set_num_executors(1);
        common.set_num_tasks(1);

        SpecificAggregateStats specific =
                new SpecificAggregateStats();

        if (spout) {

            specific.set_spout(
                    new SpoutAggregateStats()
            );

        } else {

            specific.set_bolt(
                    new BoltAggregateStats()
            );
        }

        ComponentAggregateStats stats =
                new ComponentAggregateStats();

        stats.set_common_stats(common);
        stats.set_specific_stats(specific);

        return stats;
    }

    private static TopologyInfo topologyInfo(
            List<ExecutorSummary> executors) {

        TopologyInfo topology =
                new TopologyInfo();

        topology.set_id("topology-workers");
        topology.set_name("Topology Workers");
        topology.set_uptime_secs(300);
        topology.set_status("ACTIVE");
        topology.set_executors(executors);

        return topology;
    }

    private static ExecutorSummary executor(
            int taskStart,
            int taskEnd,
            String component,
            String host,
            int port) {

        ExecutorInfo info =
                new ExecutorInfo();

        info.set_task_start(taskStart);
        info.set_task_end(taskEnd);

        ExecutorSummary summary =
                new ExecutorSummary();

        summary.set_executor_info(info);
        summary.set_component_id(component);
        summary.set_host(host);
        summary.set_port(port);
        summary.set_uptime_secs(100);

        return summary;
    }

    private static Map<String, Object> uiConfig() {

        Map<String, Object> config =
                new HashMap<>();

        config.put(
                "scheduler.display.resource",
                true
        );

        config.put(
                "logviewer.port",
                8000
        );

        config.put(
                "logviewer.https.port",
                0
        );

        return config;
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

        List<Map<String, Object>> maps =
                new ArrayList<>();

        for (Object element : (List<?>) value) {

            assertTrue(
                    element instanceof Map<?, ?>,
                    key
            );

            maps.add(
                    (Map<String, Object>) element
            );
        }

        return maps;
    }

    private static List<?> rawList(
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

        return (List<?>) value;
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

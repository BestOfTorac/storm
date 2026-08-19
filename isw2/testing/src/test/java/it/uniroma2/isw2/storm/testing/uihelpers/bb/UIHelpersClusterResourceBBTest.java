package it.uniroma2.isw2.storm.testing.uihelpers.bb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.daemon.ui.UIHelpers;
import org.apache.storm.generated.ClusterSummary;
import org.apache.storm.generated.OwnerResourceSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersClusterResourceBBTest {

    private static final double EPSILON = 0.000001;

    /*
     * Strict black-box F5 suite.
     *
     * Only frames whose fixtures and expected behaviour can be derived
     * completely from external documentation/public contracts are retained.
     * No UIHelpers implementation was inspected to derive these tests.
     */

    @Test
    void cluster01ReportsZeroCountsForEmptyCluster() {

        ClusterSummary cluster =
                new ClusterSummary();

        cluster.set_supervisors(new ArrayList<>());
        cluster.set_topologies(new ArrayList<>());
        cluster.set_nimbuses(new ArrayList<>());

        Map<String, Object> result =
                UIHelpers.getClusterSummary(
                        cluster,
                        "3.0.0",
                        uiConfig(false)
                );

        assertInt(result, "supervisors", 0);
        assertInt(result, "topologies", 0);
        assertInt(result, "slotsTotal", 0);
        assertInt(result, "slotsUsed", 0);
        assertInt(result, "slotsFree", 0);
        assertInt(result, "executorsTotal", 0);
        assertInt(result, "tasksTotal", 0);
    }

    @Test
    void owner01ExposesAllSuppliedDocumentedValues() {

        OwnerResourceSummary owner =
                new OwnerResourceSummary("owner-a");

        owner.set_total_topologies(3);
        owner.set_total_executors(12);
        owner.set_total_workers(4);
        owner.set_total_tasks(15);

        owner.set_memory_usage(1024.0);
        owner.set_cpu_usage(250.0);

        owner.set_memory_guarantee(4096.0);
        owner.set_cpu_guarantee(500.0);

        owner.set_memory_guarantee_remaining(3072.0);
        owner.set_cpu_guarantee_remaining(250.0);

        owner.set_isolated_node_guarantee(2);

        owner.set_requested_on_heap_memory(700.0);
        owner.set_requested_off_heap_memory(300.0);
        owner.set_requested_total_memory(1000.0);
        owner.set_requested_cpu(220.0);

        owner.set_assigned_on_heap_memory(650.0);
        owner.set_assigned_off_heap_memory(250.0);

        Map<String, Object> result =
                UIHelpers.unpackOwnerResourceSummary(owner);

        assertEquals("owner-a", result.get("owner"));

        assertInt(result, "totalTopologies", 3);
        assertInt(result, "totalExecutors", 12);
        assertInt(result, "totalWorkers", 4);
        assertInt(result, "totalTasks", 15);

        assertDouble(result, "totalMemoryUsage", 1024.0);
        assertDouble(result, "totalCpuUsage", 250.0);

        assertDouble(result, "memoryGuarantee", 4096.0);
        assertDouble(result, "cpuGuarantee", 500.0);

        assertDouble(
                result,
                "memoryGuaranteeRemaining",
                3072.0
        );

        assertDouble(
                result,
                "cpuGuaranteeRemaining",
                250.0
        );

        assertInt(result, "isolatedNodes", 2);

        assertDouble(result, "totalReqOnHeapMem", 700.0);
        assertDouble(result, "totalReqOffHeapMem", 300.0);
        assertDouble(result, "totalReqMem", 1000.0);
        assertDouble(result, "totalReqCpu", 220.0);

        assertDouble(
                result,
                "totalAssignedOnHeapMem",
                650.0
        );

        assertDouble(
                result,
                "totalAssignedOffHeapMem",
                250.0
        );
    }

    @Test
    void owner04ExposesSuppliedIsolatedNodeGuarantee() {

        OwnerResourceSummary owner =
                new OwnerResourceSummary("owner-isolated");

        owner.set_isolated_node_guarantee(3);

        Map<String, Object> result =
                UIHelpers.unpackOwnerResourceSummary(owner);

        assertInt(result, "isolatedNodes", 3);
    }

    @Test
    void owners01ReturnsEmptyOwnersCollection() {

        Map<String, Object> result =
                UIHelpers.getOwnerResourceSummaries(
                        List.of(),
                        uiConfig(true)
                );

        assertTrue(
                owners(result).isEmpty()
        );
    }

    @Test
    void owners02ReturnsOneOwnerEntry() {

        OwnerResourceSummary owner =
                new OwnerResourceSummary("owner-one");

        owner.set_total_topologies(1);

        Map<String, Object> result =
                UIHelpers.getOwnerResourceSummaries(
                        List.of(owner),
                        uiConfig(true)
                );

        List<Map<String, Object>> owners =
                owners(result);

        assertEquals(1, owners.size());
        assertEquals(
                "owner-one",
                owners.get(0).get("owner")
        );
    }

    @Test
    void owners03RepresentsEverySuppliedOwner() {

        OwnerResourceSummary ownerA =
                new OwnerResourceSummary("owner-a");

        OwnerResourceSummary ownerB =
                new OwnerResourceSummary("owner-b");

        OwnerResourceSummary ownerC =
                new OwnerResourceSummary("owner-c");

        Map<String, Object> result =
                UIHelpers.getOwnerResourceSummaries(
                        List.of(ownerA, ownerB, ownerC),
                        uiConfig(true)
                );

        List<Map<String, Object>> owners =
                owners(result);

        assertEquals(3, owners.size());

        List<String> names =
                owners.stream()
                        .map(owner ->
                                String.valueOf(
                                        owner.get("owner")
                                )
                        )
                        .toList();

        assertTrue(names.contains("owner-a"));
        assertTrue(names.contains("owner-b"));
        assertTrue(names.contains("owner-c"));
    }

    private static Map<String, Object> uiConfig(
            boolean schedulerDisplayResource) {

        Map<String, Object> config =
                new HashMap<>();

        config.put(
                "scheduler.display.resource",
                schedulerDisplayResource
        );

        return config;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> owners(
            Map<String, Object> result) {

        Object value =
                result.get("owners");

        assertNotNull(value);
        assertTrue(value instanceof List<?>);

        return (List<Map<String, Object>>) value;
    }

    private static void assertInt(
            Map<String, Object> result,
            String key,
            int expected) {

        Object value =
                result.get(key);

        assertNotNull(value);
        assertTrue(value instanceof Number);

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

        assertNotNull(value);
        assertTrue(value instanceof Number);

        assertEquals(
                expected,
                ((Number) value).doubleValue(),
                EPSILON,
                key
        );
    }
}

package it.uniroma2.isw2.storm.testing.m4.uihelpers.c1.rnd.generated;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UIHelpersC1RandomRegressionTest0 {

    public static boolean debug = false;

    public void assertBooleanArrayEquals(boolean[] expectedArray, boolean[] actualArray) {
        if (expectedArray.length != actualArray.length) {
            throw new AssertionError("Array lengths differ: " + expectedArray.length + " != " + actualArray.length);
        }
        for (int i = 0; i < expectedArray.length; i++) {
            if (expectedArray[i] != actualArray[i]) {
                throw new AssertionError("Arrays differ at index " + i + ": " + expectedArray[i] + " != " + actualArray[i]);
            }
        }
    }

    @Test
    public void test01() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test01");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map<java.lang.String, java.util.Map> strMap1 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap3 = org.apache.storm.daemon.ui.UIHelpers.putTopologyLogLevel(iface0, strMap1, "hi!");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.Map.entrySet()\" because \"<local3>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test02() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test02");
        org.apache.storm.generated.SupervisorPageInfo supervisorPageInfo0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap1 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.List<java.util.Map> mapList2 = org.apache.storm.daemon.ui.UIHelpers.getWorkerSummaries(supervisorPageInfo0, strMap1);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.SupervisorPageInfo.is_set_worker_summaries()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test03");
        org.eclipse.jetty.server.Server server0 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.storm.daemon.ui.UIHelpers.configSsl(server0, (java.lang.Integer) 1, "", "", "", "hi!", "hi!", "", "hi!", (java.lang.Boolean) true, (java.lang.Boolean) true, false);
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalArgumentException; message: TrustStore Path not accessible: hi!");
        } catch (java.lang.IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test04");
        java.lang.Object[] objArray7 = new java.lang.Object[] { 0.0f, 0L, (short) 1, (-1.0d), "", 0.0d };
        java.lang.String str8 = org.apache.storm.daemon.ui.UIHelpers.urlFormat("hi!", objArray7);
        org.junit.Assert.assertNotNull(objArray7);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray7), "[0.0, 0, 1, -1.0, , 0.0]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray7), "[0.0, 0, 1, -1.0, , 0.0]");
        org.junit.Assert.assertEquals("'" + str8 + "' != '" + "hi!" + "'", str8, "hi!");
    }

    @Test
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test05");
        org.apache.storm.generated.ComponentPageInfo componentPageInfo0 = null;
        java.util.Map map4 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap5 = org.apache.storm.daemon.ui.UIHelpers.unpackBoltPageInfo(componentPageInfo0, "hi!", "", false, map4);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.ComponentPageInfo.get_window_to_stats()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test06");
        jakarta.ws.rs.core.Response response2 = org.apache.storm.daemon.ui.UIHelpers.makeStandardResponse((java.lang.Object) (byte) 10, "hi!");
        org.junit.Assert.assertNotNull(response2);
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test07");
        org.apache.storm.generated.OwnerResourceSummary[] ownerResourceSummaryArray0 = new org.apache.storm.generated.OwnerResourceSummary[] {};
        java.util.ArrayList<org.apache.storm.generated.OwnerResourceSummary> ownerResourceSummaryList1 = new java.util.ArrayList<org.apache.storm.generated.OwnerResourceSummary>();
        boolean boolean2 = java.util.Collections.addAll((java.util.Collection<org.apache.storm.generated.OwnerResourceSummary>) ownerResourceSummaryList1, ownerResourceSummaryArray0);
        org.apache.storm.generated.Nimbus.Iface iface3 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap5 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap6 = org.apache.storm.daemon.ui.UIHelpers.getOwnerResourceSummary((java.util.List<org.apache.storm.generated.OwnerResourceSummary>) ownerResourceSummaryList1, iface3, "hi!", strMap5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.Map.get(Object)\" because \"<parameter4>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(ownerResourceSummaryArray0);
        org.junit.Assert.assertArrayEquals(ownerResourceSummaryArray0, new org.apache.storm.generated.OwnerResourceSummary[] {});
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test08");
        jakarta.ws.rs.core.Response.Status status3 = null;
        // The following exception was thrown during execution in test generation
        try {
            jakarta.ws.rs.core.Response response4 = org.apache.storm.daemon.ui.UIHelpers.makeStandardResponse((java.lang.Object) (short) 10, "hi!", true, status3);
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalArgumentException; message: Response status must not be 'null'");
        } catch (java.lang.IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test09");
        org.eclipse.jetty.ee10.servlet.ServletContextHandler servletContextHandler0 = null;
        org.apache.storm.daemon.ui.FilterConfiguration[] filterConfigurationArray1 = new org.apache.storm.daemon.ui.FilterConfiguration[] {};
        java.util.ArrayList<org.apache.storm.daemon.ui.FilterConfiguration> filterConfigurationList2 = new java.util.ArrayList<org.apache.storm.daemon.ui.FilterConfiguration>();
        boolean boolean3 = java.util.Collections.addAll((java.util.Collection<org.apache.storm.daemon.ui.FilterConfiguration>) filterConfigurationList2, filterConfigurationArray1);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.storm.daemon.ui.UIHelpers.configFilters(servletContextHandler0, (java.util.List<org.apache.storm.daemon.ui.FilterConfiguration>) filterConfigurationList2);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.eclipse.jetty.ee10.servlet.ServletContextHandler.addFilter(org.eclipse.jetty.ee10.servlet.FilterHolder, String, java.util.EnumSet)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(filterConfigurationArray1);
        org.junit.Assert.assertArrayEquals(filterConfigurationArray1, new org.apache.storm.daemon.ui.FilterConfiguration[] {});
        org.junit.Assert.assertTrue("'" + boolean3 + "' != '" + false + "'", boolean3 == false);
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test10");
        org.apache.storm.daemon.ui.IConfigurator iConfigurator4 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.storm.daemon.ui.UIHelpers.stormRunJetty((java.lang.Integer) 10, "hi!", (java.lang.Integer) 0, (java.lang.Integer) 1, iConfigurator4);
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Failed to bind to hi!/<unresolved>:10");
        } catch (java.io.IOException e) {
            // Expected exception.
        }
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test11");
        jakarta.ws.rs.core.Response.Status status2 = null;
        // The following exception was thrown during execution in test generation
        try {
            jakarta.ws.rs.core.Response response3 = org.apache.storm.daemon.ui.UIHelpers.makeStandardResponse((java.lang.Object) (short) 1, "hi!", status2);
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalArgumentException; message: Response status must not be 'null'");
        } catch (java.lang.IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test12");
        java.lang.String str1 = org.apache.storm.daemon.ui.UIHelpers.prettyUptimeMs(1);
        org.junit.Assert.assertEquals("'" + str1 + "' != '" + "1ms" + "'", str1, "1ms");
    }

    @Test
    public void test13() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test13");
        org.eclipse.jetty.server.Server server0 = null;
        org.apache.storm.daemon.ui.UIHelpers.configSsl(server0, (java.lang.Integer) (-1), "", "", "hi!", "1ms", "hi!", "", "hi!", (java.lang.Boolean) true, (java.lang.Boolean) true, (java.lang.Integer) (-1), false);
    }

    @Test
    public void test14() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test14");
        org.apache.storm.generated.TopologyHistoryInfo topologyHistoryInfo0 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap1 = org.apache.storm.daemon.ui.UIHelpers.getTopologyHistoryInfo(topologyHistoryInfo0);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.TopologyHistoryInfo.get_topo_ids()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test15");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap1 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap5 = org.apache.storm.daemon.ui.UIHelpers.getBuildVisualization(iface0, strMap1, "1ms", "", false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.Nimbus$Iface.getTopologyInfoWithOpts(String, org.apache.storm.generated.GetInfoOptions)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test16() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test16");
        org.eclipse.jetty.ee10.servlet.FilterHolder filterHolder0 = org.apache.storm.daemon.ui.UIHelpers.corsFilterHandle();
        org.junit.Assert.assertNotNull(filterHolder0);
    }

    @Test
    public void test17() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test17");
        org.apache.storm.daemon.ui.IConfigurator iConfigurator2 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.storm.daemon.ui.UIHelpers.stormRunJetty((java.lang.Integer) (-1), (java.lang.Integer) 1, iConfigurator2);
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalArgumentException; message: port out of range:-1");
        } catch (java.lang.IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void test18() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test18");
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str3 = org.apache.storm.daemon.ui.UIHelpers.getJsonResponseBody((java.lang.Object) (-1L), "1ms", false);
            org.junit.Assert.fail("Expected exception of type java.lang.ClassCastException; message: class java.lang.Long cannot be cast to class java.lang.String (java.lang.Long and java.lang.String are in module java.base of loader 'bootstrap')");
        } catch (java.lang.ClassCastException e) {
            // Expected exception.
        }
    }

    @Test
    public void test19() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test19");
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap1 = org.apache.storm.daemon.ui.UIHelpers.getStreamBox((java.lang.Object) 0.0f);
            org.junit.Assert.fail("Expected exception of type java.lang.ClassCastException; message: class java.lang.Float cannot be cast to class java.util.Map (java.lang.Float and java.util.Map are in module java.base of loader 'bootstrap')");
        } catch (java.lang.ClassCastException e) {
            // Expected exception.
        }
    }

    @Test
    public void test20() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test20");
        org.apache.storm.generated.TopologySummary topologySummary0 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap1 = org.apache.storm.daemon.ui.UIHelpers.getTopologyMap(topologySummary0);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.TopologySummary.get_id()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test21() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test21");
        org.apache.storm.generated.ClusterSummary clusterSummary0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap2 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap3 = org.apache.storm.daemon.ui.UIHelpers.getClusterSummary(clusterSummary0, "", strMap2);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.Map.getOrDefault(Object, Object)\" because \"conf\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test22() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test22");
        java.lang.String str2 = org.apache.storm.daemon.ui.UIHelpers.wrapJsonInCallback("", "hi!");
        org.junit.Assert.assertEquals("'" + str2 + "' != '" + "(hi!);" + "'", str2, "(hi!);");
    }

    @Test
    public void test23() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test23");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap3 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap4 = org.apache.storm.daemon.ui.UIHelpers.getTopologyProfilingDumpHeap(iface0, "(hi!);", "", strMap3);
            org.junit.Assert.fail("Expected exception of type java.lang.ArrayIndexOutOfBoundsException; message: Index 1 out of bounds for length 1");
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            // Expected exception.
        }
    }

    @Test
    public void test24() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test24");
        org.apache.storm.generated.SupervisorSummary[] supervisorSummaryArray0 = new org.apache.storm.generated.SupervisorSummary[] {};
        java.util.ArrayList<org.apache.storm.generated.SupervisorSummary> supervisorSummaryList1 = new java.util.ArrayList<org.apache.storm.generated.SupervisorSummary>();
        boolean boolean2 = java.util.Collections.addAll((java.util.Collection<org.apache.storm.generated.SupervisorSummary>) supervisorSummaryList1, supervisorSummaryArray0);
        jakarta.ws.rs.core.SecurityContext securityContext3 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap4 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap5 = org.apache.storm.daemon.ui.UIHelpers.getSupervisorSummary((java.util.List<org.apache.storm.generated.SupervisorSummary>) supervisorSummaryList1, securityContext3, strMap4);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.Map.containsKey(Object)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(supervisorSummaryArray0);
        org.junit.Assert.assertArrayEquals(supervisorSummaryArray0, new org.apache.storm.generated.SupervisorSummary[] {});
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test25() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test25");
        org.apache.storm.generated.OwnerResourceSummary[] ownerResourceSummaryArray0 = new org.apache.storm.generated.OwnerResourceSummary[] {};
        java.util.ArrayList<org.apache.storm.generated.OwnerResourceSummary> ownerResourceSummaryList1 = new java.util.ArrayList<org.apache.storm.generated.OwnerResourceSummary>();
        boolean boolean2 = java.util.Collections.addAll((java.util.Collection<org.apache.storm.generated.OwnerResourceSummary>) ownerResourceSummaryList1, ownerResourceSummaryArray0);
        org.apache.storm.generated.Nimbus.Iface iface3 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap5 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap6 = org.apache.storm.daemon.ui.UIHelpers.getOwnerResourceSummary((java.util.List<org.apache.storm.generated.OwnerResourceSummary>) ownerResourceSummaryList1, iface3, "(hi!);", strMap5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.Map.get(Object)\" because \"<parameter4>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(ownerResourceSummaryArray0);
        org.junit.Assert.assertArrayEquals(ownerResourceSummaryArray0, new org.apache.storm.generated.OwnerResourceSummary[] {});
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test26() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test26");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap3 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap4 = org.apache.storm.daemon.ui.UIHelpers.getTopologyProfilingDumpJstack(iface0, "(hi!);", "1ms", strMap3);
            org.junit.Assert.fail("Expected exception of type java.lang.ArrayIndexOutOfBoundsException; message: Index 1 out of bounds for length 1");
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            // Expected exception.
        }
    }

    @Test
    public void test27() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test27");
        java.lang.Object[][] objArray1 = new java.lang.Object[][] {};
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str2 = org.apache.storm.daemon.ui.UIHelpers.prettyUptimeStr("hi!", objArray1);
            org.junit.Assert.fail("Expected exception of type java.lang.NumberFormatException; message: For input string: \"hi!\"");
        } catch (java.lang.NumberFormatException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(objArray1);
        org.junit.Assert.assertArrayEquals(objArray1, new java.lang.Object[][] {});
    }

    @Test
    public void test28() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test28");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map map2 = null;
        java.util.Map map3 = org.apache.storm.daemon.ui.UIHelpers.getJsonResponseHeaders("hi!", map2);
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap5 = org.apache.storm.daemon.ui.UIHelpers.putTopologyLogLevel(iface0, (java.util.Map<java.lang.String, java.util.Map>) map2, "hi!");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.Map.entrySet()\" because \"<local3>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(map3);
    }

    @Test
    public void test29() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test29");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map map4 = null;
        java.util.Map map5 = org.apache.storm.daemon.ui.UIHelpers.getJsonResponseHeaders("hi!", map4);
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap6 = org.apache.storm.daemon.ui.UIHelpers.getTopologyProfilingStop(iface0, "1ms", "hi!", (java.util.Map<java.lang.String, java.lang.Object>) map5);
            org.junit.Assert.fail("Expected exception of type java.lang.ArrayIndexOutOfBoundsException; message: Index 1 out of bounds for length 1");
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(map5);
    }

    @Test
    public void test30() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test30");
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str1 = org.apache.storm.daemon.ui.UIHelpers.prettyUptimeMs("");
            org.junit.Assert.fail("Expected exception of type java.lang.NumberFormatException; message: For input string: \"\"");
        } catch (java.lang.NumberFormatException e) {
            // Expected exception.
        }
    }

    @Test
    public void test31() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test31");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map map4 = null;
        java.util.Map map5 = org.apache.storm.daemon.ui.UIHelpers.getJsonResponseHeaders("hi!", map4);
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap6 = org.apache.storm.daemon.ui.UIHelpers.getTopologyProfilingRestartWorker(iface0, "hi!", "1ms", (java.util.Map<java.lang.String, java.lang.Object>) map4);
            org.junit.Assert.fail("Expected exception of type java.lang.ArrayIndexOutOfBoundsException; message: Index 1 out of bounds for length 1");
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(map5);
    }

    @Test
    public void test32() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test32");
        org.apache.storm.generated.TopologyInfo topologyInfo0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap1 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap2 = org.apache.storm.daemon.ui.UIHelpers.getTopologyWorkers(topologyInfo0, (java.util.Map) strMap1);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.TopologyInfo.get_executors()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strMap1);
    }

    @Test
    public void test33() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test33");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap3 = org.apache.storm.daemon.ui.UIHelpers.putTopologyKill(iface0, "", "1ms");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.Nimbus$Iface.getTopologyInfoWithOpts(String, org.apache.storm.generated.GetInfoOptions)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test34() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test34");
        org.apache.storm.daemon.ui.IConfigurator iConfigurator4 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.storm.daemon.ui.UIHelpers.stormRunJetty((java.lang.Integer) 1, "1ms", (java.lang.Integer) 1, (java.lang.Integer) 0, iConfigurator4);
            org.junit.Assert.fail("Expected exception of type java.io.IOException; message: Failed to bind to 1ms/<unresolved>:1");
        } catch (java.io.IOException e) {
            // Expected exception.
        }
    }

    @Test
    public void test35() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC1RandomRegressionTest0.test35");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap2 = org.apache.storm.daemon.ui.UIHelpers.putTopologyActivate(iface0, "");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.Nimbus$Iface.getTopologyInfoWithOpts(String, org.apache.storm.generated.GetInfoOptions)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }
}


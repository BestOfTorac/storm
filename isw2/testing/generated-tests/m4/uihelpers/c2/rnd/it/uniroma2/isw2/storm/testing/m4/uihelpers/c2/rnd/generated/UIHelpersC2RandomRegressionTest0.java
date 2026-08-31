package it.uniroma2.isw2.storm.testing.m4.uihelpers.c2.rnd.generated;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UIHelpersC2RandomRegressionTest0 {

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
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test01");
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
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test02");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap3 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap4 = org.apache.storm.daemon.ui.UIHelpers.getTopologyProfilingDumpHeap(iface0, "", "", strMap3);
            org.junit.Assert.fail("Expected exception of type java.lang.ArrayIndexOutOfBoundsException; message: Index 1 out of bounds for length 1");
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            // Expected exception.
        }
    }

    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test03");
        org.apache.storm.generated.TopologyPageInfo topologyPageInfo0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap2 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap4 = org.apache.storm.daemon.ui.UIHelpers.getTopologySummary(topologyPageInfo0, "hi!", strMap2, "hi!");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.TopologyPageInfo.get_topology_conf()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test04");
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
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test05");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map map6 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap7 = org.apache.storm.daemon.ui.UIHelpers.getComponentPage(iface0, "", "", "hi!", false, "", map6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.Nimbus$Iface.getComponentPageInfo(String, String, String, boolean)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test06");
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap1 = org.apache.storm.daemon.ui.UIHelpers.getStreamBox((java.lang.Object) "");
            org.junit.Assert.fail("Expected exception of type java.lang.ClassCastException; message: class java.lang.String cannot be cast to class java.util.Map (java.lang.String and java.util.Map are in module java.base of loader 'bootstrap')");
        } catch (java.lang.ClassCastException e) {
            // Expected exception.
        }
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test07");
        org.apache.storm.generated.LogConfig logConfig0 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap1 = org.apache.storm.daemon.ui.UIHelpers.getTopolgoyLogConfig(logConfig0);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.LogConfig.is_set_named_logger_level()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test08");
        org.apache.storm.generated.ClusterSummary clusterSummary0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap2 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap3 = org.apache.storm.daemon.ui.UIHelpers.getClusterSummary(clusterSummary0, "hi!", strMap2);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.ClusterSummary.get_supervisors()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strMap2);
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test09");
        org.eclipse.jetty.server.Server server0 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.storm.daemon.ui.UIHelpers.configSsl(server0, (java.lang.Integer) 1, "", "hi!", "hi!", "hi!", "hi!", "hi!", "", (java.lang.Boolean) true, (java.lang.Boolean) true, false);
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalArgumentException; message: TrustStore Path not accessible: hi!");
        } catch (java.lang.IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test10");
        org.apache.storm.generated.ComponentPageInfo componentPageInfo0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap4 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap5 = org.apache.storm.daemon.ui.UIHelpers.unpackSpoutPageInfo(componentPageInfo0, "hi!", "", false, (java.util.Map) strMap4);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.ComponentPageInfo.get_window_to_stats()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strMap4);
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test11");
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap1 = org.apache.storm.daemon.ui.UIHelpers.getStreamBox((java.lang.Object) (-1.0f));
            org.junit.Assert.fail("Expected exception of type java.lang.ClassCastException; message: class java.lang.Float cannot be cast to class java.util.Map (java.lang.Float and java.util.Map are in module java.base of loader 'bootstrap')");
        } catch (java.lang.ClassCastException e) {
            // Expected exception.
        }
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test12");
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
    public void test13() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test13");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap7 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        java.util.Map map8 = org.apache.storm.daemon.ui.UIHelpers.getJsonResponseHeaders("", (java.util.Map) strMap7);
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap9 = org.apache.storm.daemon.ui.UIHelpers.getComponentPage(iface0, "", "", "", false, "", map8);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.Nimbus$Iface.getComponentPageInfo(String, String, String, boolean)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strMap7);
        org.junit.Assert.assertNotNull(map8);
    }

    @Test
    public void test14() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test14");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap5 = org.apache.storm.daemon.ui.UIHelpers.putTopologyDebugActionSpct(iface0, "hi!", "hi!", "", "");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.Nimbus$Iface.getTopologyInfoWithOpts(String, org.apache.storm.generated.GetInfoOptions)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test15");
        org.eclipse.jetty.server.Server server0 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.storm.daemon.ui.UIHelpers.configSsl(server0, (java.lang.Integer) 10, "hi!", "", "hi!", "hi!", "", "", "", (java.lang.Boolean) false, (java.lang.Boolean) false, (java.lang.Integer) 0, false);
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalArgumentException; message: KeyStore Path not accessible: hi!");
        } catch (java.lang.IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void test16() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test16");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap2 = org.apache.storm.daemon.ui.UIHelpers.putTopologyDeactivate(iface0, "");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.Nimbus$Iface.getTopologyInfoWithOpts(String, org.apache.storm.generated.GetInfoOptions)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test17() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test17");
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str1 = org.apache.storm.daemon.ui.UIHelpers.getWindowHint("hi!");
            org.junit.Assert.fail("Expected exception of type java.lang.NumberFormatException; message: For input string: \"hi!\"");
        } catch (java.lang.NumberFormatException e) {
            // Expected exception.
        }
    }

    @Test
    public void test18() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test18");
        java.util.Map<java.lang.String, java.lang.Object> strMap0 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        boolean boolean1 = org.apache.storm.daemon.ui.UIHelpers.isSecureLogviewer(strMap0);
        org.junit.Assert.assertNotNull(strMap0);
        org.junit.Assert.assertTrue("'" + boolean1 + "' != '" + false + "'", boolean1 == false);
    }

    @Test
    public void test19() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test19");
        org.eclipse.jetty.server.Server server0 = null;
        jakarta.servlet.Servlet servlet1 = null;
        org.apache.storm.daemon.ui.FilterConfiguration[] filterConfigurationArray2 = new org.apache.storm.daemon.ui.FilterConfiguration[] {};
        java.util.ArrayList<org.apache.storm.daemon.ui.FilterConfiguration> filterConfigurationList3 = new java.util.ArrayList<org.apache.storm.daemon.ui.FilterConfiguration>();
        boolean boolean4 = java.util.Collections.addAll((java.util.Collection<org.apache.storm.daemon.ui.FilterConfiguration>) filterConfigurationList3, filterConfigurationArray2);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.storm.daemon.ui.UIHelpers.configFilter(server0, servlet1, (java.util.List<org.apache.storm.daemon.ui.FilterConfiguration>) filterConfigurationList3);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"Object.getClass()\" because \"instance\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(filterConfigurationArray2);
        org.junit.Assert.assertArrayEquals(filterConfigurationArray2, new org.apache.storm.daemon.ui.FilterConfiguration[] {});
        org.junit.Assert.assertTrue("'" + boolean4 + "' != '" + false + "'", boolean4 == false);
    }

    @Test
    public void test20() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test20");
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
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test21");
        java.util.Map<java.lang.String, java.lang.Object> strMap2 = org.apache.storm.daemon.ui.UIHelpers.getTopologyOpResponse("hi!", "");
        java.lang.Class<?> wildcardClass3 = strMap2.getClass();
        org.junit.Assert.assertNotNull(strMap2);
        org.junit.Assert.assertNotNull(wildcardClass3);
    }

    @Test
    public void test22() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test22");
        java.util.Map.Entry<org.apache.storm.generated.GlobalStreamId, org.apache.storm.generated.Grouping> globalStreamIdEntry0 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap1 = org.apache.storm.daemon.ui.UIHelpers.getInputMap(globalStreamIdEntry0);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.Map$Entry.getKey()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test23() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test23");
        org.apache.storm.generated.TopologyPageInfo topologyPageInfo0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap2 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap4 = org.apache.storm.daemon.ui.UIHelpers.getTopologySummary(topologyPageInfo0, "http://hi%21:null/api/v1/log?file=", strMap2, "hi!");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.TopologyPageInfo.get_topology_conf()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strMap2);
    }

    @Test
    public void test24() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test24");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap4 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        java.util.Map map5 = org.apache.storm.daemon.ui.UIHelpers.getJsonResponseHeaders("", (java.util.Map) strMap4);
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap6 = org.apache.storm.daemon.ui.UIHelpers.getTopologyProfilingStop(iface0, "hi!", "http://hi%21:null/api/v1/log?file=", strMap4);
            org.junit.Assert.fail("Expected exception of type java.lang.NumberFormatException; message: For input string: \"//hi%21\"");
        } catch (java.lang.NumberFormatException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strMap4);
        org.junit.Assert.assertNotNull(map5);
    }

    @Test
    public void test25() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test25");
        org.apache.storm.generated.ComponentPageInfo componentPageInfo0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap5 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        java.util.Map map6 = org.apache.storm.daemon.ui.UIHelpers.getJsonResponseHeaders("", (java.util.Map) strMap5);
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap7 = org.apache.storm.daemon.ui.UIHelpers.unpackSpoutPageInfo(componentPageInfo0, "", "hi!", true, map6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.ComponentPageInfo.get_window_to_stats()\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strMap5);
        org.junit.Assert.assertNotNull(map6);
    }

    @Test
    public void test26() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test26");
        org.eclipse.jetty.server.Server server5 = org.apache.storm.daemon.ui.UIHelpers.jettyCreateServer((java.lang.Integer) 10, "hi!", (java.lang.Integer) 1, (java.lang.Integer) (-1), (java.lang.Boolean) true);
        jakarta.servlet.Servlet servlet6 = null;
        org.apache.storm.daemon.ui.FilterConfiguration[] filterConfigurationArray7 = new org.apache.storm.daemon.ui.FilterConfiguration[] {};
        java.util.ArrayList<org.apache.storm.daemon.ui.FilterConfiguration> filterConfigurationList8 = new java.util.ArrayList<org.apache.storm.daemon.ui.FilterConfiguration>();
        boolean boolean9 = java.util.Collections.addAll((java.util.Collection<org.apache.storm.daemon.ui.FilterConfiguration>) filterConfigurationList8, filterConfigurationArray7);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.storm.daemon.ui.UIHelpers.configFilter(server5, servlet6, (java.util.List<org.apache.storm.daemon.ui.FilterConfiguration>) filterConfigurationList8);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"Object.getClass()\" because \"instance\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(server5);
        org.junit.Assert.assertNotNull(filterConfigurationArray7);
        org.junit.Assert.assertArrayEquals(filterConfigurationArray7, new org.apache.storm.daemon.ui.FilterConfiguration[] {});
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
    }

    @Test
    public void test27() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test27");
        java.lang.String str1 = org.apache.storm.daemon.ui.UIHelpers.prettyUptimeSec((int) (short) -1);
        org.junit.Assert.assertEquals("'" + str1 + "' != '" + "" + "'", str1, "");
    }

    @Test
    public void test28() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test28");
        org.apache.storm.generated.SupervisorSummary[] supervisorSummaryArray0 = new org.apache.storm.generated.SupervisorSummary[] {};
        java.util.ArrayList<org.apache.storm.generated.SupervisorSummary> supervisorSummaryList1 = new java.util.ArrayList<org.apache.storm.generated.SupervisorSummary>();
        boolean boolean2 = java.util.Collections.addAll((java.util.Collection<org.apache.storm.generated.SupervisorSummary>) supervisorSummaryList1, supervisorSummaryArray0);
        jakarta.ws.rs.core.SecurityContext securityContext3 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap5 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        java.util.Map map6 = org.apache.storm.daemon.ui.UIHelpers.getJsonResponseHeaders("", (java.util.Map) strMap5);
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap7 = org.apache.storm.daemon.ui.UIHelpers.getSupervisorSummary((java.util.List<org.apache.storm.generated.SupervisorSummary>) supervisorSummaryList1, securityContext3, strMap5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.lang.Integer.intValue()\" because the return value of \"java.util.Map.get(Object)\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(supervisorSummaryArray0);
        org.junit.Assert.assertArrayEquals(supervisorSummaryArray0, new org.apache.storm.generated.SupervisorSummary[] {});
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
        org.junit.Assert.assertNotNull(strMap5);
        org.junit.Assert.assertNotNull(map6);
    }

    @Test
    public void test29() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test29");
        java.util.Map<java.lang.String, java.lang.Object> strMap4 = org.apache.storm.daemon.ui.UIHelpers.getTopologyOpResponse("hi!", "");
        java.lang.String str6 = org.apache.storm.daemon.ui.UIHelpers.getLogviewerLink("hi!", "", strMap4, (int) (short) 100);
        java.lang.Class<?> wildcardClass7 = strMap4.getClass();
        org.junit.Assert.assertNotNull(strMap4);
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "http://hi%21:null/api/v1/log?file=" + "'", str6, "http://hi%21:null/api/v1/log?file=");
        org.junit.Assert.assertNotNull(wildcardClass7);
    }

    @Test
    public void test30() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test30");
        java.util.Map<java.lang.String, java.lang.Object> strMap2 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        java.lang.Object[] objArray3 = new java.lang.Object[] { (-1.0d), strMap2 };
        java.util.Map<java.lang.String, java.lang.Object> strMap5 = org.apache.storm.daemon.ui.UIHelpers.getProfilingDisabled();
        java.lang.Object[] objArray6 = new java.lang.Object[] { (-1.0d), strMap5 };
        java.lang.Object[][] objArray7 = new java.lang.Object[][] { objArray3, objArray6 };
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str8 = org.apache.storm.daemon.ui.UIHelpers.prettyUptimeStr("hi!", objArray7);
            org.junit.Assert.fail("Expected exception of type java.lang.NumberFormatException; message: For input string: \"hi!\"");
        } catch (java.lang.NumberFormatException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strMap2);
        org.junit.Assert.assertNotNull(objArray3);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray3), "[-1.0, {message=Profiling is not enabled on this server, status=disabled}]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray3), "[-1.0, {message=Profiling is not enabled on this server, status=disabled}]");
        org.junit.Assert.assertNotNull(strMap5);
        org.junit.Assert.assertNotNull(objArray6);
        org.junit.Assert.assertEquals(java.util.Arrays.deepToString(objArray6), "[-1.0, {message=Profiling is not enabled on this server, status=disabled}]");
        org.junit.Assert.assertEquals(java.util.Arrays.toString(objArray6), "[-1.0, {message=Profiling is not enabled on this server, status=disabled}]");
        org.junit.Assert.assertNotNull(objArray7);
    }

    @Test
    public void test31() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test31");
        java.lang.String str1 = org.apache.storm.daemon.ui.UIHelpers.prettyUptimeMs((int) 'a');
        org.junit.Assert.assertEquals("'" + str1 + "' != '" + "97ms" + "'", str1, "97ms");
    }

    @Test
    public void test32() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test32");
        org.eclipse.jetty.server.Server server4 = org.apache.storm.daemon.ui.UIHelpers.jettyCreateServer((java.lang.Integer) 10, "97ms", (java.lang.Integer) 10, (java.lang.Boolean) false);
        org.junit.Assert.assertNotNull(server4);
    }

    @Test
    public void test33() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test33");
        org.apache.storm.generated.Nimbus.Iface iface0 = null;
        java.util.Map<java.lang.String, java.lang.Object> strMap3 = org.apache.storm.daemon.ui.UIHelpers.getTopologyOpResponse("hi!", "");
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap7 = org.apache.storm.daemon.ui.UIHelpers.getBuildVisualization(iface0, strMap3, "hi!", "hi!", true);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.generated.Nimbus$Iface.getTopologyInfoWithOpts(String, org.apache.storm.generated.GetInfoOptions)\" because \"<parameter1>\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strMap3);
    }

    @Test
    public void test34() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test34");
        // The following exception was thrown during execution in test generation
        try {
            java.util.Map<java.lang.String, java.lang.Object> strMap1 = org.apache.storm.daemon.ui.UIHelpers.getStreamBox((java.lang.Object) 1L);
            org.junit.Assert.fail("Expected exception of type java.lang.ClassCastException; message: class java.lang.Long cannot be cast to class java.util.Map (java.lang.Long and java.util.Map are in module java.base of loader 'bootstrap')");
        } catch (java.lang.ClassCastException e) {
            // Expected exception.
        }
    }

    @Test
    public void test35() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "UIHelpersC2RandomRegressionTest0.test35");
        java.lang.String str1 = org.apache.storm.daemon.ui.UIHelpers.prettyUptimeSec((-1));
        org.junit.Assert.assertEquals("'" + str1 + "' != '" + "" + "'", str1, "");
    }
}


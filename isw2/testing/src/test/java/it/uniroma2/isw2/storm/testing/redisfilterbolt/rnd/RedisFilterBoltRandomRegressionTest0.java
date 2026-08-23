package it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisFilterBoltRandomRegressionTest0 {

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
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test01");
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig0 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType1 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport2 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType1);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt3 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport2);
        org.apache.storm.tuple.ITuple iTuple4 = null;
        java.lang.String str5 = redisFilterMapperRandoopSupport2.getValueFromTuple(iTuple4);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "randoop-value" + "'", str5, "randoop-value");
    }

    @Test
    public void test02() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test02");
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig0 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType1 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport2 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType1);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt3 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport2);
        org.apache.storm.tuple.Tuple tuple4 = null;
        // The following exception was thrown during execution in test generation
        try {
            redisFilterBolt3.execute(tuple4);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.task.OutputCollector.reportError(java.lang.Throwable)\" because \"this.collector\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test03");
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig0 = null;
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig1 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType2 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport3 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType2);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt4 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig1, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport3);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt5 = it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterBoltRandoopProcessFactory.withPool(jedisPoolConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport3);
        java.util.Map<java.lang.String, java.lang.Object> strMap6 = redisFilterBolt5.getComponentConfiguration();
        org.junit.Assert.assertNotNull(redisFilterBolt5);
        org.junit.Assert.assertNull(strMap6);
    }

    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test04");
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig0 = null;
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig1 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType2 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport3 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType2);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt4 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig1, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport3);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt5 = it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterBoltRandoopProcessFactory.withPool(jedisPoolConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport3);
        org.apache.storm.tuple.Tuple tuple6 = null;
        redisFilterBolt5.process(tuple6);
        org.junit.Assert.assertNotNull(redisFilterBolt5);
    }

    @Test
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test05");
        org.apache.storm.redis.common.config.JedisClusterConfig jedisClusterConfig0 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType1 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport3 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType1, "randoop-value");
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt4 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisClusterConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport3);
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test06");
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig0 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType1 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport2 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType1);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt3 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport2);
        org.apache.storm.topology.OutputFieldsDeclarer outputFieldsDeclarer4 = null;
        redisFilterBolt3.declareOutputFields(outputFieldsDeclarer4);
        org.apache.storm.tuple.Tuple tuple6 = null;
        // The following exception was thrown during execution in test generation
        try {
            redisFilterBolt3.execute(tuple6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.storm.task.OutputCollector.reportError(java.lang.Throwable)\" because \"this.collector\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test07");
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig0 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType1 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport3 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType1, "randoop-value");
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription redisDataTypeDescription4 = redisFilterMapperRandoopSupport3.getDataTypeDescription();
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt5 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport3);
        org.junit.Assert.assertNotNull(redisDataTypeDescription4);
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test08");
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig0 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType1 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport3 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType1, "randoop-value");
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt4 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport3);
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription redisDataTypeDescription5 = redisFilterMapperRandoopSupport3.getDataTypeDescription();
        org.apache.storm.tuple.ITuple iTuple6 = null;
        java.lang.String str7 = redisFilterMapperRandoopSupport3.getKeyFromTuple(iTuple6);
        org.junit.Assert.assertNotNull(redisDataTypeDescription5);
        org.junit.Assert.assertEquals("'" + str7 + "' != '" + "randoop-key" + "'", str7, "randoop-key");
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test09");
        org.apache.storm.redis.common.config.JedisClusterConfig jedisClusterConfig0 = null;
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig1 = null;
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig2 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType3 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport5 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType3, "randoop-value");
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt6 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig2, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport5);
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription redisDataTypeDescription7 = redisFilterMapperRandoopSupport5.getDataTypeDescription();
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt8 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig1, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport5);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt9 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisClusterConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport5);
        org.junit.Assert.assertNotNull(redisDataTypeDescription7);
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test10");
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig0 = null;
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig1 = null;
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig2 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType3 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport4 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType3);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt5 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig2, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport4);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt6 = it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterBoltRandoopProcessFactory.withPool(jedisPoolConfig1, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport4);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt7 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport4);
        org.junit.Assert.assertNotNull(redisFilterBolt6);
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RedisFilterBoltRandomRegressionTest0.test11");
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig0 = null;
        org.apache.storm.redis.common.config.JedisPoolConfig jedisPoolConfig1 = null;
        org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType redisDataType2 = null;
        it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport redisFilterMapperRandoopSupport4 = new it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterMapperRandoopSupport(redisDataType2, "randoop-value");
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt5 = new org.apache.storm.redis.bolt.RedisFilterBolt(jedisPoolConfig1, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport4);
        org.apache.storm.redis.bolt.RedisFilterBolt redisFilterBolt6 = it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support.RedisFilterBoltRandoopProcessFactory.withPool(jedisPoolConfig0, (org.apache.storm.redis.common.mapper.RedisFilterMapper) redisFilterMapperRandoopSupport4);
        org.junit.Assert.assertNotNull(redisFilterBolt6);
    }
}


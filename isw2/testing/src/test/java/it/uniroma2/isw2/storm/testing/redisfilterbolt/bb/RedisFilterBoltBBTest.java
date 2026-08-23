package it.uniroma2.isw2.storm.testing.redisfilterbolt.bb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.storm.redis.bolt.RedisFilterBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainer;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType;
import org.apache.storm.redis.common.mapper.RedisFilterMapper;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.GeoCoordinate;

class RedisFilterBoltBBTest {

    private static final String TUPLE_KEY = "tuple-key";

    private static final String HASH_KEY = "hash-key";
    private static final String SET_KEY = "set-key";
    private static final String SORTED_SET_KEY = "sorted-set-key";
    private static final String GEO_KEY = "geo-key";

    @Test
    @DisplayName("RFB-BB-01 STRING present on JedisPool forwards and acknowledges")
    void rfbBb01_stringPresentPool_forwardsAndAcknowledges() {
        Fixture fixture = poolFixture(RedisDataType.STRING, null);

        when(fixture.redis.exists(TUPLE_KEY)).thenReturn(true);

        assertForwardedAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-02 STRING absent on JedisCluster is filtered and acknowledged")
    void rfbBb02_stringAbsentCluster_filtersAndAcknowledges() {
        Fixture fixture = clusterFixture(RedisDataType.STRING, null);

        when(fixture.redis.exists(TUPLE_KEY)).thenReturn(false);

        assertFilteredAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-03 HASH field present forwards and acknowledges")
    void rfbBb03_hashFieldPresent_forwardsAndAcknowledges() {
        Fixture fixture = poolFixture(RedisDataType.HASH, HASH_KEY);

        when(fixture.redis.hexists(HASH_KEY, TUPLE_KEY)).thenReturn(true);

        assertForwardedAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-04 SET value absent is filtered and acknowledged")
    void rfbBb04_setValueAbsent_filtersAndAcknowledges() {
        Fixture fixture = clusterFixture(RedisDataType.SET, SET_KEY);

        when(fixture.redis.sismember(SET_KEY, TUPLE_KEY)).thenReturn(false);

        assertFilteredAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-05 SORTED_SET member present forwards and acknowledges")
    void rfbBb05_sortedSetMemberPresent_forwardsAndAcknowledges() {
        Fixture fixture = poolFixture(RedisDataType.SORTED_SET, SORTED_SET_KEY);

        when(fixture.redis.zrank(SORTED_SET_KEY, TUPLE_KEY)).thenReturn(2L);

        assertForwardedAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-06 SORTED_SET member absent is filtered and acknowledged")
    void rfbBb06_sortedSetMemberAbsent_filtersAndAcknowledges() {
        Fixture fixture = clusterFixture(RedisDataType.SORTED_SET, SORTED_SET_KEY);

        when(fixture.redis.zrank(SORTED_SET_KEY, TUPLE_KEY)).thenReturn(null);

        assertFilteredAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-07 HYPER_LOG_LOG count zero is filtered and acknowledged")
    void rfbBb07_hllCountZero_filtersAndAcknowledges() {
        Fixture fixture = poolFixture(RedisDataType.HYPER_LOG_LOG, null);

        when(fixture.redis.pfcount(TUPLE_KEY)).thenReturn(0L);

        assertFilteredAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-08 HYPER_LOG_LOG count one forwards and acknowledges")
    void rfbBb08_hllCountOne_forwardsAndAcknowledges() {
        Fixture fixture = clusterFixture(RedisDataType.HYPER_LOG_LOG, null);

        when(fixture.redis.pfcount(TUPLE_KEY)).thenReturn(1L);

        assertForwardedAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-09 GEO absent is filtered and acknowledged")
    void rfbBb09_geoAbsent_filtersAndAcknowledges() {
        Fixture fixture = poolFixture(RedisDataType.GEO, GEO_KEY);

        when(fixture.redis.geopos(GEO_KEY, TUPLE_KEY))
                .thenReturn(List.of());

        assertFilteredAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-10 GEO present forwards and acknowledges")
    void rfbBb10_geoPresent_forwardsAndAcknowledges() {
        Fixture fixture = clusterFixture(RedisDataType.GEO, GEO_KEY);

        GeoCoordinate coordinate = mock(GeoCoordinate.class);

        when(fixture.redis.geopos(GEO_KEY, TUPLE_KEY))
                .thenReturn(List.of(coordinate));

        assertForwardedAndAcknowledged(fixture);
    }

    @Test
    @DisplayName("RFB-BB-11 declareOutputFields delegates mapper-defined declaration")
    void rfbBb11_declareOutputFields_usesMapperDeclaration() {
        Fixture fixture = poolFixture(RedisDataType.STRING, null);
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);

        fixture.bolt.declareOutputFields(declarer);

        verify(fixture.mapper).declareOutputFields(declarer);
    }

    private static Fixture poolFixture(
            RedisDataType dataType,
            String additionalKey) {

        RedisFilterMapper mapper = mapper(dataType, additionalKey);
        JedisCommandsContainer redis = mock(JedisCommandsContainer.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple input = mock(Tuple.class);
        JedisPoolConfig config = mock(JedisPoolConfig.class);

        TestableRedisFilterBolt bolt =
                new TestableRedisFilterBolt(config, mapper, redis);

        bolt.installCollector(collector);

        when(mapper.getKeyFromTuple(input)).thenReturn(TUPLE_KEY);

        return new Fixture(
                bolt,
                mapper,
                redis,
                collector,
                input);
    }

    private static Fixture clusterFixture(
            RedisDataType dataType,
            String additionalKey) {

        RedisFilterMapper mapper = mapper(dataType, additionalKey);
        JedisCommandsContainer redis = mock(JedisCommandsContainer.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple input = mock(Tuple.class);
        JedisClusterConfig config = mock(JedisClusterConfig.class);

        TestableRedisFilterBolt bolt =
                new TestableRedisFilterBolt(config, mapper, redis);

        bolt.installCollector(collector);

        when(mapper.getKeyFromTuple(input)).thenReturn(TUPLE_KEY);

        return new Fixture(
                bolt,
                mapper,
                redis,
                collector,
                input);
    }

    private static RedisFilterMapper mapper(
            RedisDataType dataType,
            String additionalKey) {

        RedisFilterMapper mapper = mock(RedisFilterMapper.class);

        RedisDataTypeDescription description =
                additionalKey == null
                        ? new RedisDataTypeDescription(dataType)
                        : new RedisDataTypeDescription(dataType, additionalKey);

        when(mapper.getDataTypeDescription()).thenReturn(description);

        return mapper;
    }

    private static void assertForwardedAndAcknowledged(Fixture fixture) {
        List<Object> values = List.of("payload");

        when(fixture.input.getValues()).thenReturn(values);

        fixture.bolt.process(fixture.input);

        verify(fixture.collector).emit(fixture.input, values);
        verify(fixture.collector).ack(fixture.input);
        verify(fixture.collector, never()).fail(any(Tuple.class));
        verify(fixture.collector, never()).reportError(any(Throwable.class));
    }

    private static void assertFilteredAndAcknowledged(Fixture fixture) {
        fixture.bolt.process(fixture.input);

        verify(fixture.collector, never())
                .emit(any(Tuple.class), anyList());

        verify(fixture.collector).ack(fixture.input);
        verify(fixture.collector, never()).fail(any(Tuple.class));
        verify(fixture.collector, never()).reportError(any(Throwable.class));
    }

    private static final class Fixture {

        private final TestableRedisFilterBolt bolt;
        private final RedisFilterMapper mapper;
        private final JedisCommandsContainer redis;
        private final OutputCollector collector;
        private final Tuple input;

        private Fixture(
                TestableRedisFilterBolt bolt,
                RedisFilterMapper mapper,
                JedisCommandsContainer redis,
                OutputCollector collector,
                Tuple input) {

            this.bolt = bolt;
            this.mapper = mapper;
            this.redis = redis;
            this.collector = collector;
            this.input = input;
        }
    }

    private static final class TestableRedisFilterBolt extends RedisFilterBolt {

        private final JedisCommandsContainer testRedis;

        private TestableRedisFilterBolt(
                JedisPoolConfig config,
                RedisFilterMapper mapper,
                JedisCommandsContainer testRedis) {

            super(config, mapper);
            this.testRedis = testRedis;
        }

        private TestableRedisFilterBolt(
                JedisClusterConfig config,
                RedisFilterMapper mapper,
                JedisCommandsContainer testRedis) {

            super(config, mapper);
            this.testRedis = testRedis;
        }

        private void installCollector(OutputCollector collector) {
            this.collector = collector;
        }

        @Override
        protected JedisCommandsContainer getInstance() {
            return testRedis;
        }
    }
}

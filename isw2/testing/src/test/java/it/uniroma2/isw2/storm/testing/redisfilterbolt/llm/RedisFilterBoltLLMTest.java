package it.uniroma2.isw2.storm.testing.redisfilterbolt.llm;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.storm.redis.bolt.RedisFilterBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainer;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType;
import org.apache.storm.redis.common.mapper.RedisFilterMapper;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.GeoCoordinate;

class RedisFilterBoltLLMTest {

    private static final String KEY = "member-or-field";
    private static final String ADDITIONAL_KEY = "redis-structure";

    @Test
    void poolConstructorRejectsSetWithoutAdditionalKey() {
        RedisFilterMapper mapper = mapperFor(RedisDataType.SET, null);

        assertThrows(IllegalArgumentException.class,
                () -> new RedisFilterBolt(mock(JedisPoolConfig.class), mapper));
    }

    @Test
    void stringExistingKeyEmitsAndAcknowledgesTuple() {
        Fixture fixture = fixture(RedisDataType.STRING, null);
        when(fixture.jedis.exists(KEY)).thenReturn(true);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.jedis).exists(KEY);
        verify(fixture.collector).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
        verify(fixture.collector, never()).fail(fixture.tuple);
    }

    @Test
    void stringMissingKeyOnlyAcknowledgesTuple() {
        Fixture fixture = fixture(RedisDataType.STRING, null);
        when(fixture.jedis.exists(KEY)).thenReturn(false);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.collector, never()).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void setMemberEmitsUsingAdditionalKeyAsSetName() {
        Fixture fixture = fixture(RedisDataType.SET, ADDITIONAL_KEY);
        when(fixture.jedis.sismember(ADDITIONAL_KEY, KEY)).thenReturn(true);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.jedis).sismember(ADDITIONAL_KEY, KEY);
        verify(fixture.collector).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void existingHashFieldEmitsTuple() {
        Fixture fixture = fixture(RedisDataType.HASH, ADDITIONAL_KEY);
        when(fixture.jedis.hexists(ADDITIONAL_KEY, KEY)).thenReturn(true);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.jedis).hexists(ADDITIONAL_KEY, KEY);
        verify(fixture.collector).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void missingSortedSetMemberDoesNotEmit() {
        Fixture fixture = fixture(RedisDataType.SORTED_SET, ADDITIONAL_KEY);
        when(fixture.jedis.zrank(ADDITIONAL_KEY, KEY)).thenReturn(null);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.jedis).zrank(ADDITIONAL_KEY, KEY);
        verify(fixture.collector, never()).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void nonEmptyHyperLogLogEmitsTuple() {
        Fixture fixture = fixture(RedisDataType.HYPER_LOG_LOG, null);
        when(fixture.jedis.pfcount(KEY)).thenReturn(1L);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.jedis).pfcount(KEY);
        verify(fixture.collector).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void geoNullResultDoesNotEmit() {
        Fixture fixture = fixture(RedisDataType.GEO, ADDITIONAL_KEY);
        when(fixture.jedis.geopos(ADDITIONAL_KEY, KEY)).thenReturn(null);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.collector, never()).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void geoResultContainingCoordinateEmitsTuple() {
        Fixture fixture = fixture(RedisDataType.GEO, ADDITIONAL_KEY);
        GeoCoordinate coordinate = mock(GeoCoordinate.class);
        when(fixture.jedis.geopos(ADDITIONAL_KEY, KEY))
                .thenReturn(java.util.Arrays.asList(null, coordinate));

        fixture.bolt.process(fixture.tuple);

        verify(fixture.collector).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void redisExceptionIsReportedAndTupleIsFailed() {
        Fixture fixture = fixture(RedisDataType.STRING, null);
        RuntimeException failure = new RuntimeException("Redis unavailable");
        doThrow(failure).when(fixture.jedis).exists(KEY);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.collector).reportError(failure);
        verify(fixture.collector).fail(fixture.tuple);
        verify(fixture.collector, never()).ack(fixture.tuple);
        verify(fixture.collector, never()).emit(fixture.tuple, fixture.values);
    }

    @Test
    void declareOutputFieldsDelegatesToMapper() {
        RedisFilterMapper mapper = mapperFor(RedisDataType.STRING, null);
        TestableRedisFilterBolt bolt = new TestableRedisFilterBolt(
                mock(JedisPoolConfig.class), mapper, mock(JedisCommandsContainer.class));
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);

        bolt.declareOutputFields(declarer);

        verify(mapper).declareOutputFields(declarer);
    }

    private static Fixture fixture(RedisDataType dataType, String additionalKey) {
        RedisFilterMapper mapper = mapperFor(dataType, additionalKey);
        JedisCommandsContainer jedis = mock(JedisCommandsContainer.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple tuple = mock(Tuple.class);
        List<Object> values = List.of("payload", 42);
        when(mapper.getKeyFromTuple(tuple)).thenReturn(KEY);
        when(tuple.getValues()).thenReturn(values);

        TestableRedisFilterBolt bolt = new TestableRedisFilterBolt(
                mock(JedisPoolConfig.class), mapper, jedis);
        bolt.useCollector(collector);
        return new Fixture(bolt, mapper, jedis, collector, tuple, values);
    }

    private static RedisFilterMapper mapperFor(RedisDataType dataType, String additionalKey) {
        RedisFilterMapper mapper = mock(RedisFilterMapper.class);
        RedisDataTypeDescription description = mock(RedisDataTypeDescription.class);
        when(mapper.getDataTypeDescription()).thenReturn(description);
        when(description.getDataType()).thenReturn(dataType);
        when(description.getAdditionalKey()).thenReturn(additionalKey);
        return mapper;
    }

    private record Fixture(
            TestableRedisFilterBolt bolt,
            RedisFilterMapper mapper,
            JedisCommandsContainer jedis,
            OutputCollector collector,
            Tuple tuple,
            List<Object> values) {
    }

    private static final class TestableRedisFilterBolt extends RedisFilterBolt {
        private final JedisCommandsContainer jedis;

        private TestableRedisFilterBolt(
                JedisPoolConfig config,
                RedisFilterMapper mapper,
                JedisCommandsContainer jedis) {
            super(config, mapper);
            this.jedis = jedis;
        }

        @Override
        protected JedisCommandsContainer getInstance() {
            return jedis;
        }

        private void useCollector(OutputCollector collector) {
            this.collector = collector;
        }
    }
}

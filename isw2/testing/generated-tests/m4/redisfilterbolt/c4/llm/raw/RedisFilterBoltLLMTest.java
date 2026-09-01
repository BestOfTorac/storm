package org.apache.storm.redis.bolt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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

    @Test
    void T01_poolConstructorRejectsSetWithoutAdditionalKey() {
        RedisFilterMapper mapper = mapperFor(RedisDataType.SET, null);
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new TestableRedisFilterBolt(mock(JedisPoolConfig.class), mapper, mock(JedisCommandsContainer.class)));
        assertEquals("additionalKey should be defined", error.getMessage());
    }

    @Test
    void T02_stringPresentEmitsTupleAndAcknowledgesIt() {
        Fixture fixture = fixture(RedisDataType.STRING, null, "user:42");
        when(fixture.jedis.exists("user:42")).thenReturn(true);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.collector).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
        verify(fixture.collector, never()).fail(fixture.tuple);
    }

    @Test
    void T03_stringAbsentDoesNotEmitAndAcknowledgesTuple() {
        Fixture fixture = fixture(RedisDataType.STRING, null, "missing");
        when(fixture.jedis.exists("missing")).thenReturn(false);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.collector, never()).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void T04_setMembershipUsesAdditionalKey() {
        Fixture fixture = fixture(RedisDataType.SET, "allowed-users", "alice");
        when(fixture.jedis.sismember("allowed-users", "alice")).thenReturn(true);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.jedis).sismember("allowed-users", "alice");
        verify(fixture.collector).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void T05_hashMembershipUsesAdditionalKey() {
        Fixture fixture = fixture(RedisDataType.HASH, "profile", "email");
        when(fixture.jedis.hexists("profile", "email")).thenReturn(true);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.jedis).hexists("profile", "email");
        verify(fixture.collector).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void T06_sortedSetEmitsOnlyWhenRankIsNonNull() {
        Fixture present = fixture(RedisDataType.SORTED_SET, "leaderboard", "alice");
        when(present.jedis.zrank("leaderboard", "alice")).thenReturn(0L);
        present.bolt.process(present.tuple);
        verify(present.collector).emit(present.tuple, present.values);
        verify(present.collector).ack(present.tuple);

        Fixture absent = fixture(RedisDataType.SORTED_SET, "leaderboard", "bob");
        when(absent.jedis.zrank("leaderboard", "bob")).thenReturn(null);
        absent.bolt.process(absent.tuple);
        verify(absent.collector, never()).emit(absent.tuple, absent.values);
        verify(absent.collector).ack(absent.tuple);
    }

    @Test
    void T07_hyperLogLogPositiveCountEmitsTuple() {
        Fixture fixture = fixture(RedisDataType.HYPER_LOG_LOG, null, "visitors");
        when(fixture.jedis.pfcount("visitors")).thenReturn(1L);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.collector).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void T08_hyperLogLogZeroCountDoesNotEmitTuple() {
        Fixture fixture = fixture(RedisDataType.HYPER_LOG_LOG, null, "visitors");
        when(fixture.jedis.pfcount("visitors")).thenReturn(0L);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.collector, never()).emit(fixture.tuple, fixture.values);
        verify(fixture.collector).ack(fixture.tuple);
    }

    @Test
    void T09_geoRequiresAtLeastOneNonNullPosition() {
        Fixture present = fixture(RedisDataType.GEO, "cities", "rome");
        when(present.jedis.geopos("cities", "rome"))
                .thenReturn(List.of(new GeoCoordinate(12.4964, 41.9028)));
        present.bolt.process(present.tuple);
        verify(present.collector).emit(present.tuple, present.values);
        verify(present.collector).ack(present.tuple);

        Fixture absent = fixture(RedisDataType.GEO, "cities", "unknown");
        when(absent.jedis.geopos("cities", "unknown"))
                .thenReturn(java.util.Collections.singletonList(null));
        absent.bolt.process(absent.tuple);
        verify(absent.collector, never()).emit(absent.tuple, absent.values);
        verify(absent.collector).ack(absent.tuple);
    }

    @Test
    void T10_redisFailureIsReportedAndTupleIsFailed() {
        Fixture fixture = fixture(RedisDataType.STRING, null, "key");
        RuntimeException failure = new RuntimeException("redis unavailable");
        when(fixture.jedis.exists("key")).thenThrow(failure);

        fixture.bolt.process(fixture.tuple);

        verify(fixture.collector).reportError(failure);
        verify(fixture.collector).fail(fixture.tuple);
        verify(fixture.collector, never()).ack(fixture.tuple);
        verify(fixture.collector, never()).emit(fixture.tuple, fixture.values);
    }

    @Test
    void T11_declareOutputFieldsDelegatesToMapper() {
        RedisFilterMapper mapper = mapperFor(RedisDataType.STRING, null);
        TestableRedisFilterBolt bolt = new TestableRedisFilterBolt(
                mock(JedisPoolConfig.class), mapper, mock(JedisCommandsContainer.class));
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);

        assertDoesNotThrow(() -> bolt.declareOutputFields(declarer));

        verify(mapper).declareOutputFields(declarer);
    }

    private static Fixture fixture(RedisDataType dataType, String additionalKey, String key) {
        RedisFilterMapper mapper = mapperFor(dataType, additionalKey);
        Tuple tuple = mock(Tuple.class);
        List<Object> values = List.of("payload", 7);
        when(mapper.getKeyFromTuple(tuple)).thenReturn(key);
        when(tuple.getValues()).thenReturn(values);

        JedisCommandsContainer jedis = mock(JedisCommandsContainer.class);
        OutputCollector collector = mock(OutputCollector.class);
        TestableRedisFilterBolt bolt = new TestableRedisFilterBolt(
                mock(JedisPoolConfig.class), mapper, jedis);
        bolt.installCollector(collector);
        return new Fixture(bolt, mapper, jedis, collector, tuple, values);
    }

    private static RedisFilterMapper mapperFor(RedisDataType dataType, String additionalKey) {
        RedisDataTypeDescription description = mock(RedisDataTypeDescription.class);
        when(description.getDataType()).thenReturn(dataType);
        when(description.getAdditionalKey()).thenReturn(additionalKey);

        RedisFilterMapper mapper = mock(RedisFilterMapper.class);
        when(mapper.getDataTypeDescription()).thenReturn(description);
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

        private void installCollector(OutputCollector outputCollector) {
            this.collector = outputCollector;
        }
    }
}

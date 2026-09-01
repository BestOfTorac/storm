package org.apache.storm.redis.bolt;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    private static final String KEY = "tuple-key";
    private static final String ADDITIONAL_KEY = "redis-structure";

    @Test
    void t01_poolConstructorRejectsSetWithoutAdditionalKey() {
        RedisFilterMapper mapper = mapperFor(RedisDataType.SET, null);
        JedisPoolConfig config = mock(JedisPoolConfig.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> new RedisFilterBolt(config, mapper));
    }

    @Test
    void t02_stringPresentEmitsAndAcknowledgesTuple() {
        Fixture fixture = fixtureFor(RedisDataType.STRING, null);
        when(fixture.jedis.exists(KEY)).thenReturn(true);

        fixture.bolt.process(fixture.input);

        verify(fixture.jedis).exists(KEY);
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
        verify(fixture.collector, never()).fail(fixture.input);
    }

    @Test
    void t03_stringAbsentDoesNotEmitButAcknowledgesTuple() {
        Fixture fixture = fixtureFor(RedisDataType.STRING, null);
        when(fixture.jedis.exists(KEY)).thenReturn(false);

        fixture.bolt.process(fixture.input);

        verify(fixture.jedis).exists(KEY);
        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
        verify(fixture.collector, never()).fail(fixture.input);
    }

    @Test
    void t04_setPresentUsesAdditionalKeyAndEmitsTuple() {
        Fixture fixture = fixtureFor(RedisDataType.SET, ADDITIONAL_KEY);
        when(fixture.jedis.sismember(ADDITIONAL_KEY, KEY)).thenReturn(true);

        fixture.bolt.process(fixture.input);

        verify(fixture.jedis).sismember(ADDITIONAL_KEY, KEY);
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void t05_hashFieldPresentEmitsAndAcknowledgesTuple() {
        Fixture fixture = fixtureFor(RedisDataType.HASH, ADDITIONAL_KEY);
        when(fixture.jedis.hexists(ADDITIONAL_KEY, KEY)).thenReturn(true);

        fixture.bolt.process(fixture.input);

        verify(fixture.jedis).hexists(ADDITIONAL_KEY, KEY);
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void t06_sortedSetNullRankDoesNotEmitButAcknowledgesTuple() {
        Fixture fixture = fixtureFor(RedisDataType.SORTED_SET, ADDITIONAL_KEY);
        when(fixture.jedis.zrank(ADDITIONAL_KEY, KEY)).thenReturn(null);

        fixture.bolt.process(fixture.input);

        verify(fixture.jedis).zrank(ADDITIONAL_KEY, KEY);
        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void t07_hyperLogLogPositiveCountEmitsAndAcknowledgesTuple() {
        Fixture fixture = fixtureFor(RedisDataType.HYPER_LOG_LOG, null);
        when(fixture.jedis.pfcount(KEY)).thenReturn(1L);

        fixture.bolt.process(fixture.input);

        verify(fixture.jedis).pfcount(KEY);
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void t08_hyperLogLogZeroCountDoesNotEmitButAcknowledgesTuple() {
        Fixture fixture = fixtureFor(RedisDataType.HYPER_LOG_LOG, null);
        when(fixture.jedis.pfcount(KEY)).thenReturn(0L);

        fixture.bolt.process(fixture.input);

        verify(fixture.jedis).pfcount(KEY);
        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void t09_geoResultWithNonNullCoordinateEmitsAndAcknowledgesTuple() {
        Fixture fixture = fixtureFor(RedisDataType.GEO, ADDITIONAL_KEY);
        GeoCoordinate coordinate = mock(GeoCoordinate.class);

        when(fixture.jedis.geopos(ADDITIONAL_KEY, KEY))
                .thenReturn(List.of(coordinate));

        fixture.bolt.process(fixture.input);

        verify(fixture.jedis).geopos(ADDITIONAL_KEY, KEY);
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void t10_redisExceptionReportsErrorAndFailsTuple() {
        Fixture fixture = fixtureFor(RedisDataType.STRING, null);
        RuntimeException failure = new RuntimeException("deterministic Redis failure");

        when(fixture.jedis.exists(KEY)).thenThrow(failure);

        fixture.bolt.process(fixture.input);

        verify(fixture.collector).reportError(failure);
        verify(fixture.collector).fail(fixture.input);
        verify(fixture.collector, never()).ack(fixture.input);
        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
    }

    @Test
    void t11_declareOutputFieldsDelegatesToMapper() {
        RedisFilterMapper mapper = mapperFor(RedisDataType.STRING, null);
        TestableRedisFilterBolt bolt =
                new TestableRedisFilterBolt(mock(JedisPoolConfig.class), mapper);
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);

        bolt.declareOutputFields(declarer);

        verify(mapper).declareOutputFields(declarer);
    }

    private static Fixture fixtureFor(
            RedisDataType dataType,
            String additionalKey) {

        RedisFilterMapper mapper = mapperFor(dataType, additionalKey);
        JedisCommandsContainer jedis = mock(JedisCommandsContainer.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple input = mock(Tuple.class);
        List<Object> values = List.of("value-1", 2);

        when(mapper.getKeyFromTuple(input)).thenReturn(KEY);
        when(input.getValues()).thenReturn(values);

        TestableRedisFilterBolt bolt =
                new TestableRedisFilterBolt(
                        mock(JedisPoolConfig.class),
                        mapper);

        bolt.installTestCollaborators(collector, jedis);

        return new Fixture(bolt, jedis, collector, input, values);
    }

    private static RedisFilterMapper mapperFor(
            RedisDataType dataType,
            String additionalKey) {

        RedisFilterMapper mapper = mock(RedisFilterMapper.class);
        RedisDataTypeDescription description =
                mock(RedisDataTypeDescription.class);

        when(description.getDataType()).thenReturn(dataType);
        when(description.getAdditionalKey()).thenReturn(additionalKey);
        when(mapper.getDataTypeDescription()).thenReturn(description);

        return mapper;
    }

    private record Fixture(
            TestableRedisFilterBolt bolt,
            JedisCommandsContainer jedis,
            OutputCollector collector,
            Tuple input,
            List<Object> values) {
    }

    private static final class TestableRedisFilterBolt
            extends RedisFilterBolt {

        private JedisCommandsContainer testJedis;

        private TestableRedisFilterBolt(
                JedisPoolConfig config,
                RedisFilterMapper mapper) {

            super(config, mapper);
        }

        private void installTestCollaborators(
                OutputCollector collector,
                JedisCommandsContainer jedis) {

            this.collector = collector;
            this.testJedis = jedis;
        }

        @Override
        protected JedisCommandsContainer getInstance() {
            return testJedis;
        }
    }
}

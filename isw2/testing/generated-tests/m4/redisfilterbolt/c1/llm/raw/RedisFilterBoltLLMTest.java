package org.apache.storm.redis.bolt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainer;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType;
import org.apache.storm.redis.common.mapper.RedisFilterMapper;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.GeoCoordinate;

class RedisFilterBoltLLMTest {

    private static final String KEY = "member";
    private static final String ADDITIONAL_KEY = "container";

    @Test
    void T01_poolConstructorRejectsSetWithoutAdditionalKey() {
        RedisFilterMapper mapper = mapperFor(RedisDataType.SET, null);
        JedisPoolConfig config = mock(JedisPoolConfig.class);

        assertThrows(IllegalArgumentException.class,
                () -> new RedisFilterBolt(config, mapper));
    }

    @Test
    void T02_stringExistingKeyEmitsAndAcknowledges() {
        Fixture fixture = fixture(RedisDataType.STRING, null);
        when(fixture.commands.exists(KEY)).thenReturn(true);

        assertDoesNotThrow(() -> fixture.bolt.process(fixture.input));

        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
        verify(fixture.collector, never()).fail(fixture.input);
    }

    @Test
    void T03_stringMissingKeyOnlyAcknowledges() {
        Fixture fixture = fixture(RedisDataType.STRING, null);
        when(fixture.commands.exists(KEY)).thenReturn(false);

        fixture.bolt.process(fixture.input);

        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
        verify(fixture.collector, never()).fail(fixture.input);
    }

    @Test
    void T04_setMemberEmitsUsingAdditionalKey() {
        Fixture fixture = fixture(RedisDataType.SET, ADDITIONAL_KEY);
        when(fixture.commands.sismember(ADDITIONAL_KEY, KEY)).thenReturn(true);

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).sismember(ADDITIONAL_KEY, KEY);
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T05_hashMissingFieldDoesNotEmit() {
        Fixture fixture = fixture(RedisDataType.HASH, ADDITIONAL_KEY);
        when(fixture.commands.hexists(ADDITIONAL_KEY, KEY)).thenReturn(false);

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).hexists(ADDITIONAL_KEY, KEY);
        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T06_sortedSetRankPresentEmits() {
        Fixture fixture = fixture(RedisDataType.SORTED_SET, ADDITIONAL_KEY);
        when(fixture.commands.zrank(ADDITIONAL_KEY, KEY)).thenReturn(0L);

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).zrank(ADDITIONAL_KEY, KEY);
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T07_hyperLogLogZeroCountDoesNotEmit() {
        Fixture fixture = fixture(RedisDataType.HYPER_LOG_LOG, null);
        when(fixture.commands.pfcount(KEY)).thenReturn(0L);

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).pfcount(KEY);
        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T08_geoNullResultDoesNotEmit() {
        Fixture fixture = fixture(RedisDataType.GEO, ADDITIONAL_KEY);
        when(fixture.commands.geopos(ADDITIONAL_KEY, KEY)).thenReturn(null);

        fixture.bolt.process(fixture.input);

        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T09_geoEmptyResultDoesNotEmit() {
        Fixture fixture = fixture(RedisDataType.GEO, ADDITIONAL_KEY);
        when(fixture.commands.geopos(ADDITIONAL_KEY, KEY))
                .thenReturn(Collections.emptyList());

        fixture.bolt.process(fixture.input);

        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T10_geoResultWithAnyNonNullCoordinateEmits() {
        Fixture fixture = fixture(RedisDataType.GEO, ADDITIONAL_KEY);
        GeoCoordinate coordinate = mock(GeoCoordinate.class);
        List<GeoCoordinate> positions = Arrays.asList(null, coordinate);
        when(fixture.commands.geopos(ADDITIONAL_KEY, KEY)).thenReturn(positions);

        fixture.bolt.process(fixture.input);

        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T11_commandFailureIsReportedAndTupleIsFailed() {
        Fixture fixture = fixture(RedisDataType.STRING, null);
        RuntimeException failure = new RuntimeException("deterministic failure");
        when(fixture.commands.exists(KEY)).thenThrow(failure);

        assertDoesNotThrow(() -> fixture.bolt.process(fixture.input));

        verify(fixture.collector).reportError(failure);
        verify(fixture.collector).fail(fixture.input);
        verify(fixture.collector, never()).ack(fixture.input);
        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
    }

    private static RedisFilterMapper mapperFor(RedisDataType dataType, String additionalKey) {
        RedisFilterMapper mapper = mock(RedisFilterMapper.class);
        RedisDataTypeDescription description = mock(RedisDataTypeDescription.class);
        when(description.getDataType()).thenReturn(dataType);
        when(description.getAdditionalKey()).thenReturn(additionalKey);
        when(mapper.getDataTypeDescription()).thenReturn(description);
        return mapper;
    }

    private static Fixture fixture(RedisDataType dataType, String additionalKey) {
        JedisPoolConfig config = mock(JedisPoolConfig.class);
        RedisFilterMapper mapper = mapperFor(dataType, additionalKey);
        JedisCommandsContainer commands = mock(JedisCommandsContainer.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple input = mock(Tuple.class);
        List<Object> values = new Values("payload", 7);
        when(mapper.getKeyFromTuple(input)).thenReturn(KEY);
        when(input.getValues()).thenReturn(values);

        TestableRedisFilterBolt bolt = new TestableRedisFilterBolt(config, mapper, commands);
        bolt.setCollector(collector);
        return new Fixture(bolt, commands, collector, input, values);
    }

    private record Fixture(
            TestableRedisFilterBolt bolt,
            JedisCommandsContainer commands,
            OutputCollector collector,
            Tuple input,
            List<Object> values) {
    }

    private static final class TestableRedisFilterBolt extends RedisFilterBolt {
        private final JedisCommandsContainer commands;

        private TestableRedisFilterBolt(
                JedisPoolConfig config,
                RedisFilterMapper mapper,
                JedisCommandsContainer commands) {
            super(config, mapper);
            this.commands = commands;
        }

        @Override
        public JedisCommandsContainer getInstance() {
            return commands;
        }

        private void setCollector(OutputCollector collector) {
            this.collector = collector;
        }
    }
}

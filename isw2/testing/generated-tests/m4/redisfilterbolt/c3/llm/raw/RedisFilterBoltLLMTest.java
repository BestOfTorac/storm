package org.apache.storm.redis.bolt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainer;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisFilterMapper;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.GeoCoordinate;

class RedisFilterBoltLLMTest {

    @Test
    void T01_poolConstructorRejectsSetWithoutAdditionalKey() {
        JedisPoolConfig config = mock(JedisPoolConfig.class);
        RedisFilterMapper mapper = mapperFor(RedisDataTypeDescription.RedisDataType.SET, null);

        assertThrows(IllegalArgumentException.class, () -> new TestableRedisFilterBolt(config, mapper));
    }

    @Test
    void T02_stringMatchEmitsAndAcknowledgesTuple() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.STRING, null);
        when(fixture.mapper.getKeyFromTuple(fixture.input)).thenReturn("key");
        when(fixture.commands.exists("key")).thenReturn(true);

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).exists("key");
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
        verify(fixture.collector, never()).fail(fixture.input);
    }

    @Test
    void T03_missingStringAcknowledgesWithoutEmitting() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.STRING, null);
        when(fixture.mapper.getKeyFromTuple(fixture.input)).thenReturn("missing");
        when(fixture.commands.exists("missing")).thenReturn(false);

        fixture.bolt.process(fixture.input);

        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
        verify(fixture.collector, never()).fail(fixture.input);
    }

    @Test
    void T04_setMembershipUsesAdditionalKey() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.SET, "members");
        when(fixture.mapper.getKeyFromTuple(fixture.input)).thenReturn("alice");
        when(fixture.commands.sismember("members", "alice")).thenReturn(true);

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).sismember("members", "alice");
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T05_hashMembershipUsesAdditionalKey() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.HASH, "record");
        when(fixture.mapper.getKeyFromTuple(fixture.input)).thenReturn("field");
        when(fixture.commands.hexists("record", "field")).thenReturn(true);

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).hexists("record", "field");
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T06_missingSortedSetRankFiltersTuple() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.SORTED_SET, "scores");
        when(fixture.mapper.getKeyFromTuple(fixture.input)).thenReturn("bob");
        when(fixture.commands.zrank("scores", "bob")).thenReturn(null);

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).zrank("scores", "bob");
        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T07_positiveHyperLogLogCountEmitsTuple() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.HYPER_LOG_LOG, null);
        when(fixture.mapper.getKeyFromTuple(fixture.input)).thenReturn("visitors");
        when(fixture.commands.pfcount("visitors")).thenReturn(2L);

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).pfcount("visitors");
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T08_geoMatchAcceptsAListContainingANonNullCoordinate() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.GEO, "places");
        GeoCoordinate coordinate = mock(GeoCoordinate.class);
        when(fixture.mapper.getKeyFromTuple(fixture.input)).thenReturn("rome");
        when(fixture.commands.geopos("places", "rome"))
                .thenReturn(Arrays.asList(null, coordinate));

        fixture.bolt.process(fixture.input);

        verify(fixture.commands).geopos("places", "rome");
        verify(fixture.collector).emit(fixture.input, fixture.values);
        verify(fixture.collector).ack(fixture.input);
    }

    @Test
    void T09_redisFailureIsReportedAndTupleIsFailed() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.STRING, null);
        RuntimeException failure = new RuntimeException("redis failure");
        when(fixture.mapper.getKeyFromTuple(fixture.input)).thenReturn("key");
        when(fixture.commands.exists("key")).thenThrow(failure);

        fixture.bolt.process(fixture.input);

        verify(fixture.collector).reportError(failure);
        verify(fixture.collector).fail(fixture.input);
        verify(fixture.collector, never()).ack(fixture.input);
        verify(fixture.collector, never()).emit(fixture.input, fixture.values);
    }

    @Test
    void T10_mapperFailurePropagatesBeforeRedisErrorHandling() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.STRING, null);
        RuntimeException failure = new RuntimeException("mapping failure");
        when(fixture.mapper.getKeyFromTuple(fixture.input)).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> fixture.bolt.process(fixture.input));

        assertSame(failure, thrown);
        verifyNoInteractions(fixture.commands, fixture.collector);
    }

    @Test
    void T11_outputFieldDeclarationIsDelegatedToMapper() {
        Fixture fixture = fixture(RedisDataTypeDescription.RedisDataType.STRING, null);
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);

        assertDoesNotThrow(() -> fixture.bolt.declareOutputFields(declarer));

        verify(fixture.mapper).declareOutputFields(declarer);
    }

    private static RedisFilterMapper mapperFor(
            RedisDataTypeDescription.RedisDataType dataType, String additionalKey) {
        RedisFilterMapper mapper = mock(RedisFilterMapper.class);
        RedisDataTypeDescription description = mock(RedisDataTypeDescription.class);
        when(mapper.getDataTypeDescription()).thenReturn(description);
        when(description.getDataType()).thenReturn(dataType);
        when(description.getAdditionalKey()).thenReturn(additionalKey);
        return mapper;
    }

    private static Fixture fixture(
            RedisDataTypeDescription.RedisDataType dataType, String additionalKey) {
        JedisPoolConfig config = mock(JedisPoolConfig.class);
        RedisFilterMapper mapper = mapperFor(dataType, additionalKey);
        JedisCommandsContainer commands = mock(JedisCommandsContainer.class);
        OutputCollector collector = mock(OutputCollector.class);
        Tuple input = mock(Tuple.class);
        List<Object> values = List.of("payload");
        when(input.getValues()).thenReturn(values);

        TestableRedisFilterBolt bolt = new TestableRedisFilterBolt(config, mapper);
        bolt.use(commands, collector);
        return new Fixture(bolt, mapper, commands, collector, input, values);
    }

    private record Fixture(
            TestableRedisFilterBolt bolt,
            RedisFilterMapper mapper,
            JedisCommandsContainer commands,
            OutputCollector collector,
            Tuple input,
            List<Object> values) {
    }

    private static final class TestableRedisFilterBolt extends RedisFilterBolt {
        private JedisCommandsContainer commands;

        private TestableRedisFilterBolt(JedisPoolConfig config, RedisFilterMapper mapper) {
            super(config, mapper);
        }

        private void use(JedisCommandsContainer commands, OutputCollector collector) {
            this.commands = commands;
            this.collector = collector;
        }

        @Override
        public JedisCommandsContainer getInstance() {
            return commands;
        }
    }
}

package it.uniroma2.isw2.storm.testing.redisfilterbolt.cf;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.storm.redis.bolt.RedisFilterBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainer;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType;
import org.apache.storm.redis.common.mapper.RedisFilterMapper;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RedisFilterBoltCFTest {

    private static final String TUPLE_KEY = "tuple-key";
    private static final String SET_KEY = "set-key";
    private static final String GEO_KEY = "geo-key";

    @Test
    @DisplayName("RFB-CF-01 SET with non-null additional key passes JedisPool constructor condition")
    void rfbCf01_setWithAdditionalKey_constructorSucceeds() {
        RedisFilterMapper mapper =
                mapper(RedisDataType.SET, SET_KEY);

        JedisPoolConfig config =
                mock(JedisPoolConfig.class);

        JedisCommandsContainer redis =
                mock(JedisCommandsContainer.class);

        assertDoesNotThrow(
                () -> new TestableRedisFilterBolt(
                        config,
                        mapper,
                        redis));
    }

    @Test
    @DisplayName("RFB-CF-02 SET without additional key fails JedisPool construction")
    void rfbCf02_setWithoutAdditionalKey_constructorThrows() {
        RedisFilterMapper mapper =
                mapper(RedisDataType.SET, null);

        JedisPoolConfig config =
                mock(JedisPoolConfig.class);

        JedisCommandsContainer redis =
                mock(JedisCommandsContainer.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> new TestableRedisFilterBolt(
                        config,
                        mapper,
                        redis));
    }

    @Test
    @DisplayName("RFB-CF-03 GEO null lookup is filtered and acknowledged")
    void rfbCf03_geoNull_filtersAndAcknowledges() {
        Fixture fixture =
                fixture(RedisDataType.GEO, GEO_KEY);

        when(
                fixture.redis.geopos(
                        GEO_KEY,
                        TUPLE_KEY))
                .thenReturn(null);

        fixture.bolt.process(fixture.input);

        verify(
                fixture.collector,
                never())
                .emit(
                        any(Tuple.class),
                        anyList());

        verify(fixture.collector)
                .ack(fixture.input);

        verify(
                fixture.collector,
                never())
                .fail(any(Tuple.class));

        verify(
                fixture.collector,
                never())
                .reportError(any(Throwable.class));
    }

    @Test
    @DisplayName("RFB-CF-04 LIST reaches unsupported-type error and fails tuple")
    void rfbCf04_list_reportsErrorAndFailsTuple() {
        Fixture fixture =
                fixture(RedisDataType.LIST, null);

        fixture.bolt.process(fixture.input);

        verify(fixture.collector)
                .reportError(
                        any(IllegalArgumentException.class));

        verify(fixture.collector)
                .fail(fixture.input);

        verify(
                fixture.collector,
                never())
                .ack(any(Tuple.class));

        verify(
                fixture.collector,
                never())
                .emit(
                        any(Tuple.class),
                        anyList());
    }

    private static Fixture fixture(
            RedisDataType dataType,
            String additionalKey) {

        RedisFilterMapper mapper =
                mapper(dataType, additionalKey);

        JedisPoolConfig config =
                mock(JedisPoolConfig.class);

        JedisCommandsContainer redis =
                mock(JedisCommandsContainer.class);

        OutputCollector collector =
                mock(OutputCollector.class);

        Tuple input =
                mock(Tuple.class);

        TestableRedisFilterBolt bolt =
                new TestableRedisFilterBolt(
                        config,
                        mapper,
                        redis);

        bolt.installCollector(collector);

        when(
                mapper.getKeyFromTuple(input))
                .thenReturn(TUPLE_KEY);

        return new Fixture(
                bolt,
                redis,
                collector,
                input);
    }

    private static RedisFilterMapper mapper(
            RedisDataType dataType,
            String additionalKey) {

        RedisFilterMapper mapper =
                mock(RedisFilterMapper.class);

        RedisDataTypeDescription description =
                additionalKey == null
                        ? new RedisDataTypeDescription(dataType)
                        : new RedisDataTypeDescription(
                                dataType,
                                additionalKey);

        when(
                mapper.getDataTypeDescription())
                .thenReturn(description);

        return mapper;
    }

    private static final class Fixture {

        private final TestableRedisFilterBolt bolt;
        private final JedisCommandsContainer redis;
        private final OutputCollector collector;
        private final Tuple input;

        private Fixture(
                TestableRedisFilterBolt bolt,
                JedisCommandsContainer redis,
                OutputCollector collector,
                Tuple input) {

            this.bolt = bolt;
            this.redis = redis;
            this.collector = collector;
            this.input = input;
        }
    }

    private static final class TestableRedisFilterBolt
            extends RedisFilterBolt {

        private final JedisCommandsContainer testRedis;

        private TestableRedisFilterBolt(
                JedisPoolConfig config,
                RedisFilterMapper mapper,
                JedisCommandsContainer testRedis) {

            super(config, mapper);
            this.testRedis = testRedis;
        }

        private void installCollector(
                OutputCollector collector) {

            this.collector = collector;
        }

        @Override
        protected JedisCommandsContainer getInstance() {
            return testRedis;
        }
    }
}

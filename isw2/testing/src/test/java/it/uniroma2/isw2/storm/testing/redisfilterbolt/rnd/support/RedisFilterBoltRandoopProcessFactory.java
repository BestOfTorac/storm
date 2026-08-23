package it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support;

import org.apache.storm.redis.bolt.RedisFilterBolt;
import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainer;
import org.apache.storm.redis.common.mapper.RedisFilterMapper;
import org.apache.storm.task.OutputCollector;
import org.mockito.Mockito;

/**
 * Test-only construction support that lets Randoop execute the inherited
 * RedisFilterBolt.process method without starting a real Redis service.
 *
 * <p>The factory contains no tests and no oracle. Mockito is used only to
 * provide neutral infrastructure collaborators with their default behavior.
 * No Redis command is stubbed with a hand-selected result.</p>
 */
public final class RedisFilterBoltRandoopProcessFactory {

    private RedisFilterBoltRandoopProcessFactory() {
        // Utility class.
    }

    /**
     * Creates a RedisFilterBolt backed by neutral test-only collaborators.
     *
     * @param config pool configuration selected by the generator
     * @param mapper mapper selected by the generator
     * @return a RedisFilterBolt whose process method does not require Redis
     */
    public static RedisFilterBolt withPool(
            JedisPoolConfig config,
            RedisFilterMapper mapper) {

        return new SupportedRedisFilterBolt(config, mapper);
    }

    /**
     * Creates a RedisFilterBolt backed by neutral test-only collaborators.
     *
     * @param config cluster configuration selected by the generator
     * @param mapper mapper selected by the generator
     * @return a RedisFilterBolt whose process method does not require Redis
     */
    public static RedisFilterBolt withCluster(
            JedisClusterConfig config,
            RedisFilterMapper mapper) {

        return new SupportedRedisFilterBolt(config, mapper);
    }

    private static JedisCommandsContainer newCommands() {
        return Mockito.mock(JedisCommandsContainer.class);
    }

    private static OutputCollector newCollector() {
        return Mockito.mock(OutputCollector.class);
    }

    /**
     * Private implementation detail. The public factory return type remains
     * RedisFilterBolt, so Randoop exercises the production class API.
     */
    private static final class SupportedRedisFilterBolt
            extends RedisFilterBolt {

        private final JedisCommandsContainer commands;

        private SupportedRedisFilterBolt(
                JedisPoolConfig config,
                RedisFilterMapper mapper) {

            super(config, mapper);

            this.commands = newCommands();
            this.collector = newCollector();
        }

        private SupportedRedisFilterBolt(
                JedisClusterConfig config,
                RedisFilterMapper mapper) {

            super(config, mapper);

            this.commands = newCommands();
            this.collector = newCollector();
        }

        @Override
        protected JedisCommandsContainer getInstance() {
            return commands;
        }
    }
}

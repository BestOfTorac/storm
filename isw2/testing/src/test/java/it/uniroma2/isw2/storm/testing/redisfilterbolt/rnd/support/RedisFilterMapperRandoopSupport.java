package it.uniroma2.isw2.storm.testing.redisfilterbolt.rnd.support;

import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription.RedisDataType;
import org.apache.storm.redis.common.mapper.RedisFilterMapper;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.ITuple;

/**
 * Minimal construction support for independent Randoop generation on
 * RedisFilterBolt.
 *
 * <p>This class is not a test and contains no oracle. Its only purpose is to
 * provide a public concrete implementation of RedisFilterMapper, because the
 * production module exposes RedisFilterMapper as an interface and its example
 * implementation is private.</p>
 */
public final class RedisFilterMapperRandoopSupport implements RedisFilterMapper {

    private final RedisDataTypeDescription description;

    /**
     * Creates a simple STRING mapper.
     */
    public RedisFilterMapperRandoopSupport() {
        this.description = new RedisDataTypeDescription(RedisDataType.STRING);
    }

    /**
     * Creates a mapper for the supplied Redis data type.
     *
     * @param dataType Redis data type
     */
    public RedisFilterMapperRandoopSupport(RedisDataType dataType) {
        this.description = new RedisDataTypeDescription(dataType);
    }

    /**
     * Creates a mapper for a data type and optional additional key.
     *
     * @param dataType Redis data type
     * @param additionalKey additional Redis key
     */
    public RedisFilterMapperRandoopSupport(
            RedisDataType dataType,
            String additionalKey) {

        if (additionalKey == null) {
            this.description = new RedisDataTypeDescription(dataType);
        } else {
            this.description =
                    new RedisDataTypeDescription(dataType, additionalKey);
        }
    }

    @Override
    public RedisDataTypeDescription getDataTypeDescription() {
        return description;
    }

    @Override
    public String getKeyFromTuple(ITuple tuple) {
        return "randoop-key";
    }

    @Override
    public String getValueFromTuple(ITuple tuple) {
        return "randoop-value";
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // Deliberately no-op: generation support only.
    }
}

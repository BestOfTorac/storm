/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.storm.redis.bolt;

import java.util.List;
import java.util.Objects;

import org.apache.storm.redis.common.config.JedisClusterConfig;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.container.JedisCommandsContainer;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisFilterMapper;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

import redis.clients.jedis.GeoCoordinate;

/**
 * Basic bolt for querying from Redis and filters out if key/field doesn't exist.
 * If key/field exists on Redis, this bolt just forwards input tuple to default
 * stream.
 *
 * <p>Supported data types: STRING, HASH, SET, SORTED_SET, HYPER_LOG_LOG, GEO.</p>
 *
 * <p>For STRING it checks whether the key exists in the key space.
 * For HASH, SORTED_SET and GEO, it checks whether the field exists in the
 * corresponding data structure.
 * For SET and HYPER_LOG_LOG, it checks whether the value exists in the data
 * structure.
 * The key is obtained from the tuple through
 * {@link RedisFilterMapper#getKeyFromTuple(Tuple)}.
 * In order to perform the check for SET, an additional key must be supplied.</p>
 *
 * <p>If the intent is to query only the existence of a key, regardless of its
 * actual Redis data type, STRING should be specified as the mapper data type.</p>
 */
public class RedisFilterBolt extends AbstractRedisBolt {

    private final RedisFilterMapper filterMapper;
    private final RedisDataTypeDescription.RedisDataType dataType;
    private final String additionalKey;

    /**
     * Constructor for single Redis environment (JedisPool).
     *
     * @param config configuration for initializing JedisPool
     * @param filterMapper mapper containing which datatype and query key the
     *                     bolt uses
     */
    public RedisFilterBolt(
            JedisPoolConfig config,
            RedisFilterMapper filterMapper) {
        super(config);

        this.filterMapper = filterMapper;

        RedisDataTypeDescription dataTypeDescription =
                filterMapper.getDataTypeDescription();
        this.dataType = dataTypeDescription.getDataType();
        this.additionalKey = dataTypeDescription.getAdditionalKey();

        if (dataType == RedisDataTypeDescription.RedisDataType.SET
                && additionalKey == null) {
            throw new IllegalArgumentException("additionalKey should be defined");
        }
    }

    /**
     * Constructor for Redis Cluster environment (JedisCluster).
     *
     * @param config configuration for initializing JedisCluster
     * @param filterMapper mapper containing which datatype and query key the
     *                     bolt uses
     */
    public RedisFilterBolt(
            JedisClusterConfig config,
            RedisFilterMapper filterMapper) {
        super(config);

        this.filterMapper = filterMapper;

        RedisDataTypeDescription dataTypeDescription =
                filterMapper.getDataTypeDescription();
        this.dataType = dataTypeDescription.getDataType();
        this.additionalKey = dataTypeDescription.getAdditionalKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(Tuple input) {
        String key = filterMapper.getKeyFromTuple(input);

        try {
            JedisCommandsContainer jedisCommand = getInstance();

            if (isPresent(jedisCommand, key)) {
                collector.emit(input, input.getValues());
            }

            collector.ack(input);
        } catch (Exception e) {
            collector.reportError(e);
            collector.fail(input);
        }
    }

    /**
     * Checks whether the tuple-derived key or member exists in Redis according
     * to the data type configured for this bolt.
     *
     * @param jedisCommand Redis command container
     * @param key key or member obtained from the input tuple
     * @return {@code true} when the requested Redis entry is present
     */
    private boolean isPresent(
            JedisCommandsContainer jedisCommand,
            String key) {
        switch (dataType) {
            case STRING:
                return jedisCommand.exists(key);

            case SET:
                return jedisCommand.sismember(additionalKey, key);

            case HASH:
                return jedisCommand.hexists(additionalKey, key);

            case SORTED_SET:
                return jedisCommand.zrank(additionalKey, key) != null;

            case HYPER_LOG_LOG:
                return jedisCommand.pfcount(key) > 0;

            case GEO:
                return containsGeoPosition(
                        jedisCommand.geopos(additionalKey, key));

            default:
                throw new IllegalArgumentException(
                        "Cannot process such data type: " + dataType);
        }
    }

    /**
     * Determines whether a GEO query result contains at least one valid
     * coordinate.
     *
     * @param geoPositions positions returned by Redis
     * @return {@code true} if at least one returned position is non-null
     */
    private boolean containsGeoPosition(
            List<GeoCoordinate> geoPositions) {
        return geoPositions != null
                && geoPositions.stream().anyMatch(Objects::nonNull);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        filterMapper.declareOutputFields(declarer);
    }
}

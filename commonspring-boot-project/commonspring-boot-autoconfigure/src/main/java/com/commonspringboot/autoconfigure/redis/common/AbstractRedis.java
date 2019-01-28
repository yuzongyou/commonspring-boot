package com.commonspringboot.autoconfigure.redis.common;


import com.commonspringboot.autoconfigure.redis.common.util.ConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Slowlog;

import java.util.*;

/**
 * Redis 实现
 *
 * @author yzy
 */
public abstract class AbstractRedis implements Redis {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Jedis 提供者
     */
    protected JedisProvider provider;

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public JedisProvider getProvider() {
        return provider;
    }

    @Override
    public void setProvider(JedisProvider jedisProvider) {
        this.provider = jedisProvider;
    }

    @Override
    public Jedis getResource() {
        return provider.getResource();
    }

    @Override
    public void destroyResource(Jedis jedis) {
        provider.destroyResource(jedis);
    }

    @Override
    public void closeResource(Jedis jedis) {
        provider.closeResource(jedis);
    }

    @Override
    public <T> T execute(T defaultResult, JedisExecutor<T> executor) {
        T result = execute(executor);
        return result == null ? defaultResult : result;
    }

    @Override
    public <T> T execute(PipelineExecutor<T> executor) {
        Jedis jedis = null;
        try {
            jedis = getResource();
            Pipeline pipeline = jedis.pipelined();
            T result = executor.execute(pipeline);
            pipeline.sync();
            return result;
        } catch (Exception e) {
            logger.warn("Redis 执行错误, 销毁该连接{" + jedis + "}，ERROR=" + e.getMessage(), e);
            throw e;
        } finally {
            this.closeResource(jedis);
        }
    }

    @Override
    public <T> T execute(JedisExecutor<T> executor) {
        Jedis jedis = null;
        try {
            jedis = getResource();
            return executor.execute(jedis);
        } catch (Exception e) {
            logger.warn("Redis 执行错误, 销毁该连接{" + jedis + "}，ERROR=" + e.getMessage(), e);
            throw e;
        } finally {
            this.closeResource(jedis);
        }
    }

    @Override
    public String set(final String key, final String value, final int expireSeconds) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.setex(key, expireSeconds, value);
            }
        });
    }

    @Override
    public String set(final String key, final String value) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.set(key, value);
            }
        });
    }

    @Override
    public String get(final String key) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.get(key);
            }
        });
    }

    @Override
    public String get(final String key, final String defaultValue) {
        return execute(defaultValue, new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.get(key);
            }
        });
    }

    @Override
    public Boolean getBoolean(final String key) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                return ConvertUtil.toBoolean(jedis.get(key));
            }
        });
    }

    @Override
    public Boolean getBoolean(final String key, final Boolean defaultValue) {
        return execute(defaultValue, new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                return ConvertUtil.toBoolean(jedis.get(key));
            }
        });
    }

    @Override
    public Long getLong(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return ConvertUtil.toLong(jedis.get(key));
            }
        });
    }

    @Override
    public Long getLong(final String key, final Long defaultValue) {
        return execute(defaultValue, new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return ConvertUtil.toLong(jedis.get(key));
            }
        });
    }

    @Override
    public Integer getInteger(final String key) {
        return execute(new JedisExecutor<Integer>() {
            @Override
            public Integer execute(Jedis jedis) {
                return ConvertUtil.toInteger(jedis.get(key));
            }
        });
    }

    @Override
    public Integer getInteger(final String key, final Integer defaultValue) {
        return execute(defaultValue, new JedisExecutor<Integer>() {
            @Override
            public Integer execute(Jedis jedis) {
                return ConvertUtil.toInteger(jedis.get(key));
            }
        });
    }

    @Override
    public Date getDate(final String key) {
        return execute(new JedisExecutor<Date>() {
            @Override
            public Date execute(Jedis jedis) {
                return ConvertUtil.toDate(jedis.get(key));
            }
        });
    }

    @Override
    public Date getDate(final String key, Date defaultValue) {
        return execute(defaultValue, new JedisExecutor<Date>() {
            @Override
            public Date execute(Jedis jedis) {
                return ConvertUtil.toDate(jedis.get(key));
            }
        });
    }

    @Override
    public Long expire(final String key, final int expireSeconds) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.expire(key, expireSeconds);
            }
        });
    }

    @Override
    public Long expireAt(final String key, final long unixTimeInSeconds) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.expireAt(key, unixTimeInSeconds);
            }
        });
    }

    @Override
    public String hget(final String mapKey, final String mapField) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.hget(mapKey, mapField);
            }
        });
    }

    @Override
    public Set<String> smembers(final String key) {
        Set<String> resultSet = execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.smembers(key);
            }
        });
        return null == resultSet ? new HashSet<String>() : resultSet;
    }

    @Override
    public boolean sismember(final String key, final String value) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.sismember(key, value);
                return ret != null ? ret : false;
            }
        });
    }

    @Override
    public boolean exists(final String key) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.exists(key);
                return ret != null ? ret : false;
            }
        });
    }

    @Override
    public String set(final String key, final String value, final String nxxx, final String expx, final long time) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.set(key, value, nxxx, expx, time);
            }
        });
    }

    @Override
    public String set(final String key, final String value, final String nxxx) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.set(key, value, nxxx);
            }
        });
    }

    @Override
    public Long persist(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.persist(key);
            }
        });
    }

    @Override
    public String type(final String key) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.type(key);
            }
        });
    }

    @Override
    public Long pexpire(final String key, final long milliseconds) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pexpire(key, milliseconds);
            }
        });
    }

    @Override
    public Long pexpireAt(final String key, final long millisecondsTimestamp) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pexpireAt(key, millisecondsTimestamp);
            }
        });
    }

    @Override
    public Long ttl(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.ttl(key);
            }
        });
    }

    @Override
    public Long pttl(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pttl(key);
            }
        });
    }

    @Override
    public Boolean setbit(final String key, final long offset, final boolean value) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                return jedis.setbit(key, offset, value);
            }
        });
    }

    @Override
    public Boolean setbit(final String key, final long offset, final String value) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                return jedis.setbit(key, offset, value);
            }
        });
    }

    @Override
    public Boolean getbit(final String key, final long offset) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                return jedis.getbit(key, offset);
            }
        });
    }

    @Override
    public Long setrange(final String key, final long offset, final String value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.setrange(key, offset, value);
            }
        });
    }

    @Override
    public String getrange(final String key, final long startOffset, final long endOffset) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.getrange(key, startOffset, endOffset);
            }
        });
    }

    @Override
    public String getSet(final String key, final String value) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.getSet(key, value);
            }
        });
    }

    @Override
    public Long setnx(final String key, final String value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.setnx(key, value);
            }
        });
    }

    @Override
    public String setex(final String key, final int seconds, final String value) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.setex(key, seconds, value);
            }
        });
    }

    @Override
    public String psetex(final String key, final long milliseconds, final String value) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.psetex(key, milliseconds, value);
            }
        });
    }

    @Override
    public Long decrBy(final String key, final long integer) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.decrBy(key, integer);
            }
        });
    }

    @Override
    public Long decr(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.decr(key);
            }
        });
    }

    @Override
    public Long incrBy(final String key, final long integer) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.incrBy(key, integer);
            }
        });
    }

    @Override
    public Double incrByFloat(final String key, final double value) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.incrByFloat(key, value);
            }
        });
    }

    @Override
    public Long incr(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.incr(key);
            }
        });
    }

    @Override
    public Long append(final String key, final String value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.append(key, value);
            }
        });
    }

    @Override
    public String substr(final String key, final int start, final int end) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.substr(key, start, end);
            }
        });
    }

    @Override
    public Long hset(final String key, final String field, final String value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hset(key, field, value);
            }
        });
    }

    @Override
    public Long hsetnx(final String key, final String field, final String value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hsetnx(key, field, value);
            }
        });
    }

    @Override
    public String hmset(final String key, final Map<String, String> hash) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.hmset(key, hash);
            }
        });
    }

    @Override
    public List<String> hmget(final String key, final String... fields) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.hmget(key, fields);
            }
        });
    }

    @Override
    public Long hincrBy(final String key, final String field, final long value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hincrBy(key, field, value);
            }
        });
    }

    @Override
    public Double hincrByFloat(final String key, final String field, final double value) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.hincrByFloat(key, field, value);
            }
        });
    }

    @Override
    public Boolean hexists(final String key, final String field) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.hexists(key, field);
                return ret == null ? false : ret;
            }
        });
    }

    @Override
    public Long hdel(final String key, final String... field) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hdel(key, field);
            }
        });
    }

    @Override
    public Long hlen(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hlen(key);
            }
        });
    }

    @Override
    public Set<String> hkeys(final String key) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.hkeys(key);
            }
        });
    }

    @Override
    public List<String> hvals(final String key) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.hvals(key);
            }
        });
    }

    @Override
    public Map<String, String> hgetAll(final String key) {
        return execute(new JedisExecutor<Map<String, String>>() {
            @Override
            public Map<String, String> execute(Jedis jedis) {
                return jedis.hgetAll(key);
            }
        });
    }

    @Override
    public Long rpush(final String key, final String... string) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.rpush(key, string);
            }
        });
    }

    @Override
    public Long lpush(final String key, final String... string) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.lpush(key, string);
            }
        });
    }

    @Override
    public Long llen(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.llen(key);
            }
        });
    }

    @Override
    public List<String> lrange(final String key, final long start, final long end) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.lrange(key, start, end);
            }
        });
    }

    @Override
    public String ltrim(final String key, final long start, final long end) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.ltrim(key, start, end);
            }
        });
    }

    @Override
    public String lindex(final String key, final long index) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.lindex(key, index);
            }
        });
    }

    @Override
    public String lset(final String key, final long index, final String value) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.lset(key, index, value);
            }
        });
    }

    @Override
    public Long lrem(final String key, final long count, final String value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.lrem(key, count, value);
            }
        });
    }

    @Override
    public String lpop(final String key) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.lpop(key);
            }
        });
    }

    @Override
    public String rpop(final String key) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.rpop(key);
            }
        });
    }

    @Override
    public Long sadd(final String key, final String... member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sadd(key, member);
            }
        });
    }

    @Override
    public Long srem(final String key, final String... member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.srem(key, member);
            }
        });
    }

    @Override
    public String spop(final String key) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.spop(key);
            }
        });
    }

    @Override
    public Set<String> spop(final String key, final long count) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.spop(key, count);
            }
        });
    }

    @Override
    public Long scard(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.scard(key);
            }
        });
    }

    @Override
    public String srandmember(final String key) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.srandmember(key);
            }
        });
    }

    @Override
    public List<String> srandmember(final String key, final int count) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.srandmember(key, count);
            }
        });
    }

    @Override
    public Long strlen(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.strlen(key);
            }
        });
    }

    @Override
    public Long zadd(final String key, final double score, final String member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zadd(key, score, member);
            }
        });
    }

    @Override
    public Long zadd(final String key, final double score, final String member, final ZAddParams params) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zadd(key, score, member, params);
            }
        });
    }

    @Override
    public Long zadd(final String key, final Map<String, Double> scoreMembers) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zadd(key, scoreMembers);
            }
        });
    }

    @Override
    public Long zadd(final String key, final Map<String, Double> scoreMembers, final ZAddParams params) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zadd(key, scoreMembers, params);
            }
        });
    }

    @Override
    public Set<String> zrange(final String key, final long start, final long end) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrange(key, start, end);
            }
        });
    }

    @Override
    public Long zrem(final String key, final String... member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zrem(key, member);
            }
        });
    }

    @Override
    public Double zincrby(final String key, final double score, final String member) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.zincrby(key, score, member);
            }
        });
    }

    @Override
    public Double zincrby(final String key, final double score, final String member, final ZIncrByParams params) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.zincrby(key, score, member, params);
            }
        });
    }

    @Override
    public Long zrank(final String key, final String member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zrank(key, member);
            }
        });
    }

    @Override
    public Long zrevrank(final String key, final String member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zrevrank(key, member);
            }
        });
    }

    @Override
    public Set<String> zrevrange(final String key, final long start, final long end) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrevrange(key, start, end);
            }
        });
    }

    @Override
    public Set<Tuple> zrangeWithScores(final String key, final long start, final long end) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeWithScores(key, start, end);
            }
        });
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(final String key, final long start, final long end) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeWithScores(key, start, end);
            }
        });
    }

    @Override
    public Long zcard(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zcard(key);
            }
        });
    }

    @Override
    public Double zscore(final String key, final String member) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.zscore(key, member);
            }
        });
    }

    @Override
    public List<String> sort(final String key) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.sort(key);
            }
        });
    }

    @Override
    public List<String> sort(final String key, final SortingParams sortingParameters) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.sort(key, sortingParameters);
            }
        });
    }

    @Override
    public Long zcount(final String key, final double min, final double max) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zcount(key, min, max);
            }
        });
    }

    @Override
    public Long zcount(final String key, final String min, final String max) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zcount(key, min, max);
            }
        });
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max);
            }
        });
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max);
            }
        });
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min);
            }
        });
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max, offset, count);
            }
        });
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final String max, final String min) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min);
            }
        });
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max, offset, count);
            }
        });
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min, offset, count);
            }
        });
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max);
            }
        });
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min);
            }
        });
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        });
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final String max, final String min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min, offset, count);
            }
        });
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max);
            }
        });
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max, final String min) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min);
            }
        });
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        });
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        });
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max, final String min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        });
    }

    @Override
    public Long zremrangeByRank(final String key, final long start, final long end) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zremrangeByRank(key, start, end);
            }
        });
    }

    @Override
    public Long zremrangeByScore(final String key, final double start, final double end) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zremrangeByScore(key, start, end);
            }
        });
    }

    @Override
    public Long zremrangeByScore(final String key, final String start, final String end) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zremrangeByScore(key, start, end);
            }
        });
    }

    @Override
    public Long zlexcount(final String key, final String min, final String max) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zlexcount(key, min, max);
            }
        });
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrangeByLex(key, min, max);
            }
        });
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrangeByLex(key, min, max, offset, count);
            }
        });
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrevrangeByLex(key, max, min);
            }
        });
    }


    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.zrevrangeByLex(key, max, min, offset, count);
            }
        });
    }


    @Override
    public Long zremrangeByLex(final String key, final String min, final String max) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zremrangeByLex(key, min, max);
            }
        });
    }


    @Override
    public Long linsert(final String key, final Client.LIST_POSITION where, final String pivot, final String value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.linsert(key, where, pivot, value);
            }
        });
    }


    @Override
    public Long lpushx(final String key, final String... string) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.lpushx(key, string);
            }
        });
    }


    @Override
    public Long rpushx(final String key, final String... string) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.rpushx(key, string);
            }
        });
    }


    @Override
    public List<String> blpop(final String arg) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.blpop(arg);
            }
        });
    }


    @Override
    public List<String> blpop(final int timeout, final String key) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.blpop(timeout, key);
            }
        });
    }


    @Override
    public List<String> brpop(final String arg) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.brpop(arg);
            }
        });
    }


    @Override
    public List<String> brpop(final int timeout, final String key) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.brpop(timeout, key);
            }
        });
    }


    @Override
    public Long del(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.del(key);
            }
        });
    }


    @Override
    public String echo(final String string) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.echo(string);
            }
        });
    }


    @Override
    public Long move(final String key, final int dbIndex) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.move(key, dbIndex);
            }
        });
    }


    @Override
    public Long bitcount(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.bitcount(key);
            }
        });
    }


    @Override
    public Long bitcount(final String key, final long start, final long end) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.bitcount(key, start, end);
            }
        });
    }


    @Override
    public Long bitpos(final String key, final boolean value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.bitpos(key, value);
            }
        });
    }


    @Override
    public Long bitpos(final String key, final boolean value, final BitPosParams params) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.bitpos(key, value, params);
            }
        });
    }


    @Override
    public ScanResult<Map.Entry<String, String>> hscan(final String key, final int cursor) {

        return execute(new JedisExecutor<ScanResult<Map.Entry<String, String>>>() {
            @Override
            public ScanResult<Map.Entry<String, String>> execute(Jedis jedis) {
                return jedis.hscan(key, cursor);
            }
        });
    }


    @Override
    public ScanResult<String> sscan(final String key, final int cursor) {
        return execute(new JedisExecutor<ScanResult<String>>() {
            @Override
            public ScanResult<String> execute(Jedis jedis) {
                return jedis.sscan(key, cursor);
            }
        });
    }


    @Override
    public ScanResult<Tuple> zscan(final String key, final int cursor) {
        return execute(new JedisExecutor<ScanResult<Tuple>>() {
            @Override
            public ScanResult<Tuple> execute(Jedis jedis) {
                return jedis.zscan(key, cursor);
            }
        });
    }


    @Override
    public ScanResult<Map.Entry<String, String>> hscan(final String key, final String cursor) {

        return execute(new JedisExecutor<ScanResult<Map.Entry<String, String>>>() {
            @Override
            public ScanResult<Map.Entry<String, String>> execute(Jedis jedis) {
                return jedis.hscan(key, cursor);
            }
        });
    }


    @Override
    public ScanResult<Map.Entry<String, String>> hscan(final String key, final String cursor, final ScanParams params) {

        return execute(new JedisExecutor<ScanResult<Map.Entry<String, String>>>() {
            @Override
            public ScanResult<Map.Entry<String, String>> execute(Jedis jedis) {
                return jedis.hscan(key, cursor, params);
            }
        });
    }


    @Override
    public ScanResult<String> sscan(final String key, final String cursor) {
        return execute(new JedisExecutor<ScanResult<String>>() {
            @Override
            public ScanResult<String> execute(Jedis jedis) {
                return jedis.sscan(key, cursor);
            }
        });
    }


    @Override
    public ScanResult<String> sscan(final String key, final String cursor, final ScanParams params) {
        return execute(new JedisExecutor<ScanResult<String>>() {
            @Override
            public ScanResult<String> execute(Jedis jedis) {
                return jedis.sscan(key, cursor, params);
            }
        });
    }


    @Override
    public ScanResult<Tuple> zscan(final String key, final String cursor) {
        return execute(new JedisExecutor<ScanResult<Tuple>>() {
            @Override
            public ScanResult<Tuple> execute(Jedis jedis) {
                return jedis.zscan(key, cursor);
            }
        });
    }


    @Override
    public ScanResult<Tuple> zscan(final String key, final String cursor, final ScanParams params) {
        return execute(new JedisExecutor<ScanResult<Tuple>>() {
            @Override
            public ScanResult<Tuple> execute(Jedis jedis) {
                return jedis.zscan(key, cursor, params);
            }
        });
    }


    @Override
    public Long pfadd(final String key, final String... elements) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pfadd(key, elements);
            }
        });
    }


    @Override
    public long pfcount(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pfcount(key);
            }
        });
    }


    @Override
    public Long geoadd(final String key, final double longitude, final double latitude, final String member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.geoadd(key, longitude, latitude, member);
            }
        });
    }


    @Override
    public Long geoadd(final String key, final Map<String, GeoCoordinate> memberCoordinateMap) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.geoadd(key, memberCoordinateMap);
            }
        });
    }


    @Override
    public Double geodist(final String key, final String member1, final String member2) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.geodist(key, member1, member2);
            }
        });
    }


    @Override
    public Double geodist(final String key, final String member1, final String member2, final GeoUnit unit) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.geodist(key, member1, member2, unit);
            }
        });
    }


    @Override
    public List<String> geohash(final String key, final String... members) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.geohash(key, members);
            }
        });
    }


    @Override
    public List<GeoCoordinate> geopos(final String key, final String... members) {
        return execute(new JedisExecutor<List<GeoCoordinate>>() {
            @Override
            public List<GeoCoordinate> execute(Jedis jedis) {
                return jedis.geopos(key, members);
            }
        });
    }


    @Override
    public List<GeoRadiusResponse> georadius(final String key, final double longitude, final double latitude, final double radius, final GeoUnit unit) {
        return execute(new JedisExecutor<List<GeoRadiusResponse>>() {
            @Override
            public List<GeoRadiusResponse> execute(Jedis jedis) {
                return jedis.georadius(key, longitude, latitude, radius, unit);
            }
        });
    }


    @Override
    public List<GeoRadiusResponse> georadius(final String key, final double longitude, final double latitude, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return execute(new JedisExecutor<List<GeoRadiusResponse>>() {
            @Override
            public List<GeoRadiusResponse> execute(Jedis jedis) {
                return jedis.georadius(key, longitude, latitude, radius, unit, param);
            }
        });
    }


    @Override
    public List<GeoRadiusResponse> georadiusByMember(final String key, final String member, final double radius, final GeoUnit unit) {
        return execute(new JedisExecutor<List<GeoRadiusResponse>>() {
            @Override
            public List<GeoRadiusResponse> execute(Jedis jedis) {
                return jedis.georadiusByMember(key, member, radius, unit);
            }
        });
    }


    @Override
    public List<GeoRadiusResponse> georadiusByMember(final String key, final String member, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return execute(new JedisExecutor<List<GeoRadiusResponse>>() {
            @Override
            public List<GeoRadiusResponse> execute(Jedis jedis) {
                return jedis.georadiusByMember(key, member, radius, unit, param);
            }
        });
    }


    @Override
    public List<Long> bitfield(final String key, final String... arguments) {
        return execute(new JedisExecutor<List<Long>>() {
            @Override
            public List<Long> execute(Jedis jedis) {
                return jedis.bitfield(key, arguments);
            }
        });
    }


    @Override
    public Long del(final String... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.del(keys);
            }
        });
    }


    @Override
    public Long exists(final String... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.exists(keys);
            }
        });
    }


    @Override
    public List<String> blpop(final int timeout, final String... keys) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.blpop(timeout, keys);
            }
        });
    }


    @Override
    public List<String> brpop(final int timeout, final String... keys) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.brpop(timeout, keys);
            }
        });
    }


    @Override
    public List<String> blpop(final String... args) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.blpop(args);
            }
        });
    }


    @Override
    public List<String> brpop(final String... args) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.brpop(args);
            }
        });
    }


    @Override
    public Set<String> keys(final String pattern) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.keys(pattern);
            }
        });
    }


    @Override
    public List<String> mget(final String... keys) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.mget(keys);
            }
        });
    }


    @Override
    public String mset(final String... keysvalues) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.mset(keysvalues);
            }
        });
    }


    @Override
    public Long msetnx(final String... keysvalues) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.msetnx(keysvalues);
            }
        });
    }


    @Override
    public String rename(final String oldkey, final String newkey) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.rename(oldkey, newkey);
            }
        });
    }


    @Override
    public Long renamenx(final String oldkey, final String newkey) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.renamenx(oldkey, newkey);
            }
        });
    }


    @Override
    public String rpoplpush(final String srckey, final String dstkey) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.rpoplpush(srckey, dstkey);
            }
        });
    }


    @Override
    public Set<String> sdiff(final String... keys) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.sdiff(keys);
            }
        });
    }


    @Override
    public Long sdiffstore(final String dstkey, final String... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sdiffstore(dstkey, keys);
            }
        });
    }


    @Override
    public Set<String> sinter(final String... keys) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.sinter(keys);
            }
        });
    }


    @Override
    public Long sinterstore(final String dstkey, final String... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sinterstore(dstkey, keys);
            }
        });
    }


    @Override
    public Long smove(final String srckey, final String dstkey, final String member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.smove(srckey, dstkey, member);
            }
        });
    }


    @Override
    public Long sort(final String key, final SortingParams sortingParameters, final String dstkey) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sort(key, sortingParameters, dstkey);
            }
        });
    }


    @Override
    public Long sort(final String key, final String dstkey) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sort(key, dstkey);
            }
        });
    }


    @Override
    public Set<String> sunion(final String... keys) {
        return execute(new JedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) {
                return jedis.sunion(keys);
            }
        });
    }


    @Override
    public Long sunionstore(final String dstkey, final String... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sunionstore(dstkey, keys);
            }
        });
    }


    @Override
    public String watch(final String... keys) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.watch(keys);
            }
        });
    }


    @Override
    public String unwatch() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.unwatch();
            }
        });
    }


    @Override
    public Long zinterstore(final String dstkey, final String... sets) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zinterstore(dstkey, sets);
            }
        });
    }


    @Override
    public Long zinterstore(final String dstkey, final ZParams params, final String... sets) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zinterstore(dstkey, params, sets);
            }
        });
    }


    @Override
    public Long zunionstore(final String dstkey, final String... sets) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zunionstore(dstkey, sets);
            }
        });
    }


    @Override
    public Long zunionstore(final String dstkey, final ZParams params, final String... sets) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zunionstore(dstkey, params, sets);
            }
        });
    }


    @Override
    public String brpoplpush(final String source, final String destination, final int timeout) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.brpoplpush(source, destination, timeout);
            }
        });
    }


    @Override
    public Long publish(final String channel, final String message) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.publish(channel, message);
            }
        });
    }


    @Override
    public void subscribe(final JedisPubSub jedisPubSub, final String... channels) {
        execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                jedis.subscribe(jedisPubSub, channels);
                return null;
            }
        });
    }


    @Override
    public void psubscribe(final JedisPubSub jedisPubSub, final String... patterns) {
        execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                jedis.psubscribe(jedisPubSub, patterns);
                return null;
            }
        });
    }


    @Override
    public String randomKey() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.randomKey();
            }
        });
    }


    @Override
    public Long bitop(final BitOP op, final String destKey, final String... srcKeys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.bitop(op, destKey, srcKeys);
            }
        });
    }


    @Override
    public ScanResult<String> scan(final String cursor) {
        return execute(new JedisExecutor<ScanResult<String>>() {
            @Override
            public ScanResult<String> execute(Jedis jedis) {
                return jedis.scan(cursor);
            }
        });
    }


    @Override
    public ScanResult<String> scan(final String cursor, final ScanParams params) {
        return execute(new JedisExecutor<ScanResult<String>>() {
            @Override
            public ScanResult<String> execute(Jedis jedis) {
                return jedis.scan(cursor, params);
            }
        });
    }


    @Override
    public String pfmerge(final String destkey, final String... sourcekeys) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.pfmerge(destkey, sourcekeys);
            }
        });
    }


    @Override
    public long pfcount(final String... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pfcount(keys);
            }
        });
    }


    @Override
    public List<String> configGet(final String pattern) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.configGet(pattern);
            }
        });
    }


    @Override
    public String configSet(final String parameter, final String value) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.configSet(parameter, value);
            }
        });
    }


    @Override
    public String slowlogReset() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.slowlogReset();
            }
        });
    }


    @Override
    public Long slowlogLen() {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.slowlogLen();
            }
        });
    }


    @Override
    public List<Slowlog> slowlogGet() {
        return execute(new JedisExecutor<List<Slowlog>>() {
            @Override
            public List<Slowlog> execute(Jedis jedis) {
                return jedis.slowlogGet();
            }
        });
    }


    @Override
    public List<Slowlog> slowlogGet(final long entries) {
        return execute(new JedisExecutor<List<Slowlog>>() {
            @Override
            public List<Slowlog> execute(Jedis jedis) {
                return jedis.slowlogGet(entries);
            }
        });
    }


    @Override
    public Long objectRefcount(final String string) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.objectRefcount(string);
            }
        });
    }


    @Override
    public String objectEncoding(final String string) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.objectEncoding(string);
            }
        });
    }


    @Override
    public Long objectIdletime(final String string) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.objectIdletime(string);
            }
        });
    }


    @Override
    public Object eval(final String script, final int keyCount, final String... params) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.eval(script, keyCount, params);
            }
        });
    }


    @Override
    public Object eval(final String script, final List<String> keys, final List<String> args) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.eval(script, keys, args);
            }
        });
    }


    @Override
    public Object eval(final String script) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.eval(script);
            }
        });
    }


    @Override
    public Object evalsha(final String script) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.evalsha(script);
            }
        });
    }


    @Override
    public Object evalsha(final String sha1, final List<String> keys, final List<String> args) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.evalsha(sha1, keys, args);
            }
        });
    }


    @Override
    public Object evalsha(final String sha1, final int keyCount, final String... params) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.evalsha(sha1, keyCount, params);
            }
        });
    }


    @Override
    public Boolean scriptExists(final String sha1) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.scriptExists(sha1);
                return null == ret ? false : ret;
            }
        });
    }


    @Override
    public List<Boolean> scriptExists(final String... sha1) {
        return execute(new JedisExecutor<List<Boolean>>() {
            @Override
            public List<Boolean> execute(Jedis jedis) {
                return jedis.scriptExists(sha1);
            }
        });
    }


    @Override
    public String scriptLoad(final String script) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.scriptLoad(script);
            }
        });
    }


    @Override
    public String ping() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.ping();
            }
        });
    }


    @Override
    public String quit() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.quit();
            }
        });
    }


    @Override
    public String flushDB() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.flushDB();
            }
        });
    }


    @Override
    public Long dbSize() {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.dbSize();
            }
        });
    }


    @Override
    public String select(final int index) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.select(index);
            }
        });
    }


    @Override
    public String flushAll() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.flushAll();
            }
        });
    }


    @Override
    public String auth(final String password) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.auth(password);
            }
        });
    }


    @Override
    public String save() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.save();
            }
        });
    }


    @Override
    public String bgsave() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.bgsave();
            }
        });
    }


    @Override
    public String bgrewriteaof() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.bgrewriteaof();
            }
        });
    }


    @Override
    public Long lastsave() {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.lastsave();
            }
        });
    }


    @Override
    public String shutdown() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.shutdown();
            }
        });
    }


    @Override
    public String info() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.info();
            }
        });
    }


    @Override
    public String info(final String section) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.info(section);
            }
        });
    }


    @Override
    public String slaveof(final String host, final int port) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.slaveof(host, port);
            }
        });
    }


    @Override
    public String slaveofNoOne() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.slaveofNoOne();
            }
        });
    }


    @Override
    public Long getDB() {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.getDB();
            }
        });
    }


    @Override
    public String debug(final DebugParams params) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.debug(params);
            }
        });
    }


    @Override
    public String configResetStat() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.configResetStat();
            }
        });
    }


    @Override
    public Long waitReplicas(final int replicas, final long timeout) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.waitReplicas(replicas, timeout);
            }
        });
    }


    @Override
    public String clusterNodes() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterNodes();
            }
        });
    }


    @Override
    public String clusterMeet(final String ip, final int port) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterMeet(ip, port);
            }
        });
    }


    @Override
    public String clusterAddSlots(final int... slots) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterAddSlots(slots);
            }
        });
    }


    @Override
    public String clusterDelSlots(final int... slots) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterDelSlots(slots);
            }
        });
    }


    @Override
    public String clusterInfo() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterInfo();
            }
        });
    }


    @Override
    public List<String> clusterGetKeysInSlot(final int slot, final int count) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.clusterGetKeysInSlot(slot, count);
            }
        });
    }


    @Override
    public String clusterSetSlotNode(final int slot, final String nodeId) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterSetSlotNode(slot, nodeId);
            }
        });
    }


    @Override
    public String clusterSetSlotMigrating(final int slot, final String nodeId) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterSetSlotMigrating(slot, nodeId);
            }
        });
    }


    @Override
    public String clusterSetSlotImporting(final int slot, final String nodeId) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterSetSlotImporting(slot, nodeId);
            }
        });
    }


    @Override
    public String clusterSetSlotStable(final int slot) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterSetSlotStable(slot);
            }
        });
    }


    @Override
    public String clusterForget(final String nodeId) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterForget(nodeId);
            }
        });
    }


    @Override
    public String clusterFlushSlots() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterFlushSlots();
            }
        });
    }


    @Override
    public Long clusterKeySlot(final String key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.clusterKeySlot(key);
            }
        });
    }


    @Override
    public Long clusterCountKeysInSlot(final int slot) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.clusterCountKeysInSlot(slot);
            }
        });
    }


    @Override
    public String clusterSaveConfig() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterSaveConfig();
            }
        });
    }


    @Override
    public String clusterReplicate(final String nodeId) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterReplicate(nodeId);
            }
        });
    }


    @Override
    public List<String> clusterSlaves(final String nodeId) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.clusterSlaves(nodeId);
            }
        });
    }


    @Override
    public String clusterFailover() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterFailover();
            }
        });
    }


    @Override
    public List<Object> clusterSlots() {
        return execute(new JedisExecutor<List<Object>>() {
            @Override
            public List<Object> execute(Jedis jedis) {
                return jedis.clusterSlots();
            }
        });
    }


    @Override
    public String clusterReset(final JedisCluster.Reset resetType) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.clusterReset(resetType);
            }
        });
    }


    @Override
    public String readonly() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.readonly();
            }
        });
    }


    @Override
    public List<Map<String, String>> sentinelMasters() {
        return execute(new JedisExecutor<List<Map<String, String>>>() {
            @Override
            public List<Map<String, String>> execute(Jedis jedis) {
                return jedis.sentinelMasters();
            }
        });
    }


    @Override
    public List<String> sentinelGetMasterAddrByName(final String masterName) {
        return execute(new JedisExecutor<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) {
                return jedis.sentinelGetMasterAddrByName(masterName);
            }
        });
    }


    @Override
    public Long sentinelReset(final String pattern) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sentinelReset(pattern);
            }
        });
    }


    @Override
    public List<Map<String, String>> sentinelSlaves(final String masterName) {
        return execute(new JedisExecutor<List<Map<String, String>>>() {
            @Override
            public List<Map<String, String>> execute(Jedis jedis) {
                return jedis.sentinelSlaves(masterName);
            }
        });
    }


    @Override
    public String sentinelFailover(final String masterName) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.sentinelFailover(masterName);
            }
        });
    }


    @Override
    public String sentinelMonitor(final String masterName, final String ip, final int port, final int quorum) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.sentinelMonitor(masterName, ip, port, quorum);
            }
        });
    }


    @Override
    public String sentinelRemove(final String masterName) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.sentinelRemove(masterName);
            }
        });
    }


    @Override
    public String sentinelSet(final String masterName, final Map<String, String> parameterMap) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.sentinelSet(masterName, parameterMap);
            }
        });
    }


    @Override
    public String set(final byte[] key, final byte[] value) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.set(key, value);
            }
        });
    }


    @Override
    public String set(final byte[] key, final byte[] value, final byte[] nxxx) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.set(key, value, nxxx);
            }
        });
    }


    @Override
    public String set(final byte[] key, final byte[] value, final byte[] nxxx, final byte[] expx, final long time) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.set(key, value, nxxx, expx, time);
            }
        });
    }


    @Override
    public byte[] get(final byte[] key) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.get(key);
            }
        });
    }


    @Override
    public Boolean exists(final byte[] key) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.exists(key);
                return null == ret ? false : ret;
            }
        });
    }


    @Override
    public Long persist(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.persist(key);
            }
        });
    }


    @Override
    public String type(final byte[] key) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.type(key);
            }
        });
    }


    @Override
    public Long expire(final byte[] key, final int seconds) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.expire(key, seconds);
            }
        });
    }


    @Override
    public Long pexpire(final byte[] key, final long milliseconds) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pexpire(key, milliseconds);
            }
        });
    }


    @Override
    public Long expireAt(final byte[] key, final long unixTime) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.expireAt(key, unixTime);
            }
        });
    }


    @Override
    public Long pexpireAt(final byte[] key, final long millisecondsTimestamp) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pexpireAt(key, millisecondsTimestamp);
            }
        });
    }


    @Override
    public Long ttl(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.ttl(key);
            }
        });
    }


    @Override
    public Boolean setbit(final byte[] key, final long offset, final boolean value) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.setbit(key, offset, value);
                return null == ret ? false : ret;
            }
        });
    }


    @Override
    public Boolean setbit(final byte[] key, final long offset, final byte[] value) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.setbit(key, offset, value);
                return null == ret ? false : ret;
            }
        });
    }


    @Override
    public Boolean getbit(final byte[] key, final long offset) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.getbit(key, offset);
                return null == ret ? false : ret;
            }
        });
    }


    @Override
    public Long setrange(final byte[] key, final long offset, final byte[] value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.setrange(key, offset, value);
            }
        });
    }


    @Override
    public byte[] getrange(final byte[] key, final long startOffset, final long endOffset) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.getrange(key, startOffset, endOffset);
            }
        });
    }


    @Override
    public byte[] getSet(final byte[] key, final byte[] value) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.getSet(key, value);
            }
        });
    }


    @Override
    public Long setnx(final byte[] key, final byte[] value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.setnx(key, value);
            }
        });
    }


    @Override
    public String setex(final byte[] key, final int seconds, final byte[] value) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.setex(key, seconds, value);
            }
        });
    }


    @Override
    public Long decrBy(final byte[] key, final long integer) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.decrBy(key, integer);
            }
        });
    }


    @Override
    public Long decr(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.decr(key);
            }
        });
    }


    @Override
    public Long incrBy(final byte[] key, final long integer) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.incrBy(key, integer);
            }
        });
    }


    @Override
    public Double incrByFloat(final byte[] key, final double value) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.incrByFloat(key, value);
            }
        });
    }


    @Override
    public Long incr(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.incr(key);
            }
        });
    }


    @Override
    public Long append(final byte[] key, final byte[] value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.append(key, value);
            }
        });
    }


    @Override
    public byte[] substr(final byte[] key, final int start, final int end) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.substr(key, start, end);
            }
        });
    }


    @Override
    public Long hset(final byte[] key, final byte[] field, final byte[] value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hset(key, field, value);
            }
        });
    }


    @Override
    public byte[] hget(final byte[] key, final byte[] field) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.hget(key, field);
            }
        });
    }


    @Override
    public Long hsetnx(final byte[] key, final byte[] field, final byte[] value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hsetnx(key, field, value);
            }
        });
    }


    @Override
    public String hmset(final byte[] key, final Map<byte[], byte[]> hash) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.hmset(key, hash);
            }
        });
    }


    @Override
    public List<byte[]> hmget(final byte[] key, final byte[]... fields) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.hmget(key, fields);
            }
        });
    }


    @Override
    public Long hincrBy(final byte[] key, final byte[] field, final long value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hincrBy(key, field, value);
            }
        });
    }


    @Override
    public Double hincrByFloat(final byte[] key, final byte[] field, final double value) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.hincrByFloat(key, field, value);
            }
        });
    }


    @Override
    public Boolean hexists(final byte[] key, final byte[] field) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.hexists(key, field);
                return null == ret ? false : ret;
            }
        });
    }


    @Override
    public Long hdel(final byte[] key, final byte[]... field) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hdel(key, field);
            }
        });
    }


    @Override
    public Long hlen(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.hlen(key);
            }
        });
    }


    @Override
    public Set<byte[]> hkeys(final byte[] key) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.hkeys(key);
            }
        });
    }


    @Override
    public Collection<byte[]> hvals(final byte[] key) {
        return execute(new JedisExecutor<Collection<byte[]>>() {
            @Override
            public Collection<byte[]> execute(Jedis jedis) {
                return jedis.hvals(key);
            }
        });
    }


    @Override
    public Map<byte[], byte[]> hgetAll(final byte[] key) {
        return execute(new JedisExecutor<Map<byte[], byte[]>>() {
            @Override
            public Map<byte[], byte[]> execute(Jedis jedis) {
                return jedis.hgetAll(key);
            }
        });
    }


    @Override
    public Long rpush(final byte[] key, final byte[]... args) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.rpush(key, args);
            }
        });
    }


    @Override
    public Long lpush(final byte[] key, final byte[]... args) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.lpush(key, args);
            }
        });
    }


    @Override
    public Long llen(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.llen(key);
            }
        });
    }


    @Override
    public List<byte[]> lrange(final byte[] key, final long start, final long end) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.lrange(key, start, end);
            }
        });
    }


    @Override
    public String ltrim(final byte[] key, final long start, final long end) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.ltrim(key, start, end);
            }
        });
    }


    @Override
    public byte[] lindex(final byte[] key, final long index) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.lindex(key, index);
            }
        });
    }


    @Override
    public String lset(final byte[] key, final long index, final byte[] value) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.lset(key, index, value);
            }
        });
    }


    @Override
    public Long lrem(final byte[] key, final long count, final byte[] value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.lrem(key, count, value);
            }
        });
    }


    @Override
    public byte[] lpop(final byte[] key) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.lpop(key);
            }
        });
    }


    @Override
    public byte[] rpop(final byte[] key) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.rpop(key);
            }
        });
    }


    @Override
    public Long sadd(final byte[] key, final byte[]... member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sadd(key, member);
            }
        });
    }


    @Override
    public Set<byte[]> smembers(final byte[] key) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.smembers(key);
            }
        });
    }


    @Override
    public Long srem(final byte[] key, final byte[]... member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.srem(key, member);
            }
        });
    }


    @Override
    public byte[] spop(final byte[] key) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.spop(key);
            }
        });
    }


    @Override
    public Set<byte[]> spop(final byte[] key, final long count) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.spop(key, count);
            }
        });
    }


    @Override
    public Long scard(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.scard(key);
            }
        });
    }


    @Override
    public Boolean sismember(final byte[] key, final byte[] member) {
        return execute(new JedisExecutor<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                Boolean ret = jedis.sismember(key, member);
                return null == ret ? false : ret;
            }
        });
    }


    @Override
    public byte[] srandmember(final byte[] key) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.srandmember(key);
            }
        });
    }


    @Override
    public List<byte[]> srandmember(final byte[] key, final int count) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.srandmember(key, count);
            }
        });
    }


    @Override
    public Long strlen(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.strlen(key);
            }
        });
    }


    @Override
    public Long zadd(final byte[] key, final double score, final byte[] member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zadd(key, score, member);
            }
        });
    }


    @Override
    public Long zadd(final byte[] key, final double score, final byte[] member, final ZAddParams params) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zadd(key, score, member, params);
            }
        });
    }


    @Override
    public Long zadd(final byte[] key, final Map<byte[], Double> scoreMembers) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zadd(key, scoreMembers);
            }
        });
    }


    @Override
    public Long zadd(final byte[] key, final Map<byte[], Double> scoreMembers, final ZAddParams params) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zadd(key, scoreMembers, params);
            }
        });
    }


    @Override
    public Set<byte[]> zrange(final byte[] key, final long start, final long end) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrange(key, start, end);
            }
        });
    }


    @Override
    public Long zrem(final byte[] key, final byte[]... member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zrem(key, member);
            }
        });
    }


    @Override
    public Double zincrby(final byte[] key, final double score, final byte[] member) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.zincrby(key, score, member);
            }
        });
    }


    @Override
    public Double zincrby(final byte[] key, final double score, final byte[] member, final ZIncrByParams params) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.zincrby(key, score, member, params);
            }
        });
    }


    @Override
    public Long zrank(final byte[] key, final byte[] member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zrank(key, member);
            }
        });
    }


    @Override
    public Long zrevrank(final byte[] key, final byte[] member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zrevrank(key, member);
            }
        });
    }


    @Override
    public Set<byte[]> zrevrange(final byte[] key, final long start, final long end) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrevrange(key, start, end);
            }
        });
    }


    @Override
    public Set<Tuple> zrangeWithScores(final byte[] key, final long start, final long end) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeWithScores(key, start, end);
            }
        });
    }


    @Override
    public Set<Tuple> zrevrangeWithScores(final byte[] key, final long start, final long end) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeWithScores(key, start, end);
            }
        });
    }


    @Override
    public Long zcard(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zcard(key);
            }
        });
    }


    @Override
    public Double zscore(final byte[] key, final byte[] member) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.zscore(key, member);
            }
        });
    }


    @Override
    public List<byte[]> sort(final byte[] key) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.sort(key);
            }
        });
    }


    @Override
    public List<byte[]> sort(final byte[] key, final SortingParams sortingParameters) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.sort(key, sortingParameters);
            }
        });
    }


    @Override
    public Long zcount(final byte[] key, final double min, final double max) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zcount(key, min, max);
            }
        });
    }


    @Override
    public Long zcount(final byte[] key, final byte[] min, final byte[] max) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zcount(key, min, max);
            }
        });
    }


    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max);
            }
        });
    }


    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max);
            }
        });
    }


    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min);
            }
        });
    }


    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max, offset, count);
            }
        });
    }


    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min);
            }
        });
    }


    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max, offset, count);
            }
        });
    }


    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min, offset, count);
            }
        });
    }


    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max);
            }
        });
    }


    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min);
            }
        });
    }


    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        });
    }


    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min, offset, count);
            }
        });
    }


    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max);
            }
        });
    }


    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min);
            }
        });
    }


    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        });
    }


    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        });
    }


    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<Tuple>>() {
            @Override
            public Set<Tuple> execute(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        });
    }


    @Override
    public Long zremrangeByRank(final byte[] key, final long start, final long end) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zremrangeByRank(key, start, end);
            }
        });
    }


    @Override
    public Long zremrangeByScore(final byte[] key, final double start, final double end) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zremrangeByScore(key, start, end);
            }
        });
    }


    @Override
    public Long zremrangeByScore(final byte[] key, final byte[] start, final byte[] end) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zremrangeByScore(key, start, end);
            }
        });
    }


    @Override
    public Long zlexcount(final byte[] key, final byte[] min, final byte[] max) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zlexcount(key, min, max);
            }
        });
    }


    @Override
    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrangeByLex(key, min, max);
            }
        });
    }


    @Override
    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrangeByLex(key, min, max, offset, count);
            }
        });
    }


    @Override
    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrevrangeByLex(key, max, min);
            }
        });
    }


    @Override
    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.zrevrangeByLex(key, max, min, offset, count);
            }
        });
    }


    @Override
    public Long zremrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zremrangeByLex(key, min, max);
            }
        });
    }


    @Override
    public Long linsert(final byte[] key, final Client.LIST_POSITION where, final byte[] pivot, final byte[] value) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.linsert(key, where, pivot, value);
            }
        });
    }


    @Override
    public Long lpushx(final byte[] key, final byte[]... arg) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.lpushx(key, arg);
            }
        });
    }


    @Override
    public Long rpushx(final byte[] key, final byte[]... arg) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.rpushx(key, arg);
            }
        });
    }


    @Override
    public Long del(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.del(key);
            }
        });
    }


    @Override
    public byte[] echo(final byte[] arg) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.echo(arg);
            }
        });
    }


    @Override
    public Long move(final byte[] key, final int dbIndex) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.move(key, dbIndex);
            }
        });
    }


    @Override
    public Long bitcount(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.bitcount(key);
            }
        });
    }


    @Override
    public Long bitcount(final byte[] key, final long start, final long end) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.bitcount(key, start, end);
            }
        });
    }


    @Override
    public Long pfadd(final byte[] key, final byte[]... elements) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pfadd(key, elements);
            }
        });
    }


    @Override
    public long pfcount(final byte[] key) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pfcount(key);
            }
        });
    }


    @Override
    public Long geoadd(final byte[] key, final double longitude, final double latitude, final byte[] member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.geoadd(key, longitude, latitude, member);
            }
        });
    }


    @Override
    public Long geoadd(final byte[] key, final Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.geoadd(key, memberCoordinateMap);
            }
        });
    }


    @Override
    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.geodist(key, member1, member2);
            }
        });
    }


    @Override
    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2, final GeoUnit unit) {
        return execute(new JedisExecutor<Double>() {
            @Override
            public Double execute(Jedis jedis) {
                return jedis.geodist(key, member1, member2, unit);
            }
        });
    }


    @Override
    public List<byte[]> geohash(final byte[] key, final byte[]... members) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.geohash(key, members);
            }
        });
    }


    @Override
    public List<GeoCoordinate> geopos(final byte[] key, final byte[]... members) {
        return execute(new JedisExecutor<List<GeoCoordinate>>() {
            @Override
            public List<GeoCoordinate> execute(Jedis jedis) {
                return jedis.geopos(key, members);
            }
        });
    }


    @Override
    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude, final double radius, final GeoUnit unit) {
        return execute(new JedisExecutor<List<GeoRadiusResponse>>() {
            @Override
            public List<GeoRadiusResponse> execute(Jedis jedis) {
                return jedis.georadius(key, longitude, latitude, radius, unit);
            }
        });
    }


    @Override
    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return execute(new JedisExecutor<List<GeoRadiusResponse>>() {
            @Override
            public List<GeoRadiusResponse> execute(Jedis jedis) {
                return jedis.georadius(key, longitude, latitude, radius, unit, param);
            }
        });
    }


    @Override
    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius, final GeoUnit unit) {
        return execute(new JedisExecutor<List<GeoRadiusResponse>>() {
            @Override
            public List<GeoRadiusResponse> execute(Jedis jedis) {
                return jedis.georadiusByMember(key, member, radius, unit);
            }
        });
    }


    @Override
    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        return execute(new JedisExecutor<List<GeoRadiusResponse>>() {
            @Override
            public List<GeoRadiusResponse> execute(Jedis jedis) {
                return jedis.georadiusByMember(key, member, radius, unit, param);
            }
        });
    }


    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor) {
        return execute(new JedisExecutor<ScanResult<Map.Entry<byte[], byte[]>>>() {
            @Override
            public ScanResult<Map.Entry<byte[], byte[]>> execute(Jedis jedis) {
                return jedis.hscan(key, cursor);
            }
        });
    }


    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return execute(new JedisExecutor<ScanResult<Map.Entry<byte[], byte[]>>>() {
            @Override
            public ScanResult<Map.Entry<byte[], byte[]>> execute(Jedis jedis) {
                return jedis.hscan(key, cursor, params);
            }
        });
    }


    @Override
    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor) {
        return execute(new JedisExecutor<ScanResult<byte[]>>() {
            @Override
            public ScanResult<byte[]> execute(Jedis jedis) {
                return jedis.sscan(key, cursor);
            }
        });
    }


    @Override
    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return execute(new JedisExecutor<ScanResult<byte[]>>() {
            @Override
            public ScanResult<byte[]> execute(Jedis jedis) {
                return jedis.sscan(key, cursor, params);
            }
        });
    }


    @Override
    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor) {
        return execute(new JedisExecutor<ScanResult<Tuple>>() {
            @Override
            public ScanResult<Tuple> execute(Jedis jedis) {
                return jedis.zscan(key, cursor);
            }
        });
    }


    @Override
    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        return execute(new JedisExecutor<ScanResult<Tuple>>() {
            @Override
            public ScanResult<Tuple> execute(Jedis jedis) {
                return jedis.zscan(key, cursor, params);
            }
        });
    }


    @Override
    public List<byte[]> bitfield(final byte[] key, final byte[]... arguments) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.bitfield(key, arguments);
            }
        });
    }


    @Override
    public Long del(final byte[]... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.del(keys);
            }
        });
    }


    @Override
    public Long exists(final byte[]... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.exists(keys);
            }
        });
    }


    @Override
    public List<byte[]> blpop(final int timeout, final byte[]... keys) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.blpop(timeout, keys);
            }
        });
    }


    @Override
    public List<byte[]> brpop(final int timeout, final byte[]... keys) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.brpop(timeout, keys);
            }
        });
    }


    @Override
    public List<byte[]> blpop(final byte[]... args) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.blpop(args);
            }
        });
    }


    @Override
    public List<byte[]> brpop(final byte[]... args) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.brpop(args);
            }
        });
    }


    @Override
    public Set<byte[]> keys(final byte[] pattern) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.keys(pattern);
            }
        });
    }


    @Override
    public List<byte[]> mget(final byte[]... keys) {
        return execute(new JedisExecutor<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.mget(keys);
            }
        });
    }


    @Override
    public String mset(final byte[]... keysvalues) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.mset(keysvalues);
            }
        });
    }


    @Override
    public Long msetnx(final byte[]... keysvalues) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.msetnx(keysvalues);
            }
        });
    }


    @Override
    public String rename(final byte[] oldkey, final byte[] newkey) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.rename(oldkey, newkey);
            }
        });
    }


    @Override
    public Long renamenx(final byte[] oldkey, final byte[] newkey) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.renamenx(oldkey, newkey);
            }
        });
    }


    @Override
    public byte[] rpoplpush(final byte[] srckey, final byte[] dstkey) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.rpoplpush(srckey, dstkey);
            }
        });
    }


    @Override
    public Set<byte[]> sdiff(final byte[]... keys) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.sdiff(keys);
            }
        });
    }


    @Override
    public Long sdiffstore(final byte[] dstkey, final byte[]... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sdiffstore(dstkey, keys);
            }
        });
    }


    @Override
    public Set<byte[]> sinter(final byte[]... keys) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.sinter(keys);
            }
        });
    }


    @Override
    public Long sinterstore(final byte[] dstkey, final byte[]... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sinterstore(dstkey, keys);
            }
        });
    }


    @Override
    public Long smove(final byte[] srckey, final byte[] dstkey, final byte[] member) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.smove(srckey, dstkey, member);
            }
        });
    }


    @Override
    public Long sort(final byte[] key, final SortingParams sortingParameters, final byte[] dstkey) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sort(key, sortingParameters, dstkey);
            }
        });
    }


    @Override
    public Long sort(final byte[] key, final byte[] dstkey) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sort(key, dstkey);
            }
        });
    }


    @Override
    public Set<byte[]> sunion(final byte[]... keys) {
        return execute(new JedisExecutor<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.sunion(keys);
            }
        });
    }


    @Override
    public Long sunionstore(final byte[] dstkey, final byte[]... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.sunionstore(dstkey, keys);
            }
        });
    }


    @Override
    public String watch(final byte[]... keys) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.watch(keys);
            }
        });
    }


    @Override
    public Long zinterstore(final byte[] dstkey, final byte[]... sets) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zinterstore(dstkey, sets);
            }
        });
    }


    @Override
    public Long zinterstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zinterstore(dstkey, params, sets);
            }
        });
    }


    @Override
    public Long zunionstore(final byte[] dstkey, final byte[]... sets) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zunionstore(dstkey, sets);
            }
        });
    }


    @Override
    public Long zunionstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.zunionstore(dstkey, params, sets);
            }
        });
    }


    @Override
    public byte[] brpoplpush(final byte[] source, final byte[] destination, final int timeout) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.brpoplpush(source, destination, timeout);
            }
        });
    }


    @Override
    public Long publish(final byte[] channel, final byte[] message) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.publish(channel, message);
            }
        });
    }


    @Override
    public void subscribe(final BinaryJedisPubSub jedisPubSub, final byte[]... channels) {
        execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                jedis.subscribe(jedisPubSub, channels);
                return null;
            }
        });
    }


    @Override
    public void psubscribe(final BinaryJedisPubSub jedisPubSub, final byte[]... patterns) {
        execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                jedis.psubscribe(jedisPubSub, patterns);
                return null;
            }
        });
    }


    @Override
    public byte[] randomBinaryKey() {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.randomBinaryKey();
            }
        });
    }


    @Override
    public Long bitop(final BitOP op, final byte[] destKey, final byte[]... srcKeys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.bitop(op, destKey, srcKeys);
            }
        });
    }


    @Override
    public String pfmerge(final byte[] destkey, final byte[]... sourcekeys) {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.pfmerge(destkey, sourcekeys);
            }
        });
    }


    @Override
    public Long pfcount(final byte[]... keys) {
        return execute(new JedisExecutor<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.pfcount(keys);
            }
        });
    }


    @Override
    public Object eval(final byte[] script, final byte[] keyCount, final byte[]... params) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.eval(script, keyCount, params);
            }
        });
    }


    @Override
    public Object eval(final byte[] script, final int keyCount, final byte[]... params) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.eval(script, keyCount, params);
            }
        });
    }


    @Override
    public Object eval(final byte[] script, final List<byte[]> keys, final List<byte[]> args) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.eval(script, keys, args);
            }
        });
    }


    @Override
    public Object eval(final byte[] script) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.eval(script);
            }
        });
    }


    @Override
    public Object evalsha(final byte[] script) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.evalsha(script);
            }
        });
    }


    @Override
    public Object evalsha(final byte[] sha1, final List<byte[]> keys, final List<byte[]> args) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.evalsha(sha1, keys, args);
            }
        });
    }


    @Override
    public Object evalsha(final byte[] sha1, final int keyCount, final byte[]... params) {
        return execute(new JedisExecutor<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                return jedis.evalsha(sha1, keyCount, params);
            }
        });
    }


    @Override
    public List<Long> scriptExists(final byte[]... sha1) {
        return execute(new JedisExecutor<List<Long>>() {
            @Override
            public List<Long> execute(Jedis jedis) {
                return jedis.scriptExists(sha1);
            }
        });
    }


    @Override
    public byte[] scriptLoad(final byte[] script) {
        return execute(new JedisExecutor<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.scriptLoad(script);
            }
        });
    }


    @Override
    public String scriptFlush() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.scriptFlush();
            }
        });
    }


    @Override
    public String scriptKill() {
        return execute(new JedisExecutor<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.scriptKill();
            }
        });
    }

}

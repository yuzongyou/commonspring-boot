package com.commonspringboot.autoconfigure.redis.common;

import redis.clients.jedis.*;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Slowlog;

import java.util.*;

/**
 * Redis 操作接口定义
 *
 * @author yzy
 */
public interface Redis {

    /**
     * 初始化操作
     */
    void init();

    /**
     * 销毁操作
     */
    void destroy();

    /**
     * Jedis 提供者
     *
     * @param jedisProvider 提供者
     */
    void setProvider(JedisProvider jedisProvider);

    /**
     * 获取 Jedis 连接提供者
     *
     * @return 返回提供者
     */
    JedisProvider getProvider();

    /**
     * 关闭资源，关闭不代表销毁（比如连接池）
     *
     * @param jedis 要关闭的资源
     */
    void closeResource(Jedis jedis);

    /**
     * 销毁资源，永久销毁这个资源，将变成不可用
     *
     * @param jedis 要销毁的资源
     */
    void destroyResource(Jedis jedis);

    /**
     * 获取连接
     *
     * @return 返回连接
     */
    Jedis getResource();

    /**
     * 使用管道方式执行 Redis 命令
     *
     * @param executor 执行器
     * @param <T>      结果返回类型
     * @return 返回执行结果
     */
    <T> T execute(PipelineExecutor<T> executor);

    /**
     * 执行 Redis 命令
     *
     * @param executor 执行器
     * @param <T>      结果返回类型
     * @return 返回执行结果
     */
    <T> T execute(JedisExecutor<T> executor);

    /**
     * 执行 Redis 命令
     *
     * @param defaultResult 默认结果
     * @param executor      执行器
     * @param <T>           结果返回类型
     * @return 返回执行结果
     */
    <T> T execute(T defaultResult, JedisExecutor<T> executor);

    /**
     * 设置简单Key的值
     *
     * @param key           key
     * @param value         值
     * @param expireSeconds 过期时间，单位是秒
     * @return 返回状态码
     */
    String set(String key, String value, int expireSeconds);

    /**
     * 获取指定key的值
     *
     * @param key          key
     * @param defaultValue 默认值， 如果返回为 null 将返回默认值
     * @return 返回指定key的值
     */
    String get(String key, String defaultValue);

    /**
     * 获取指定key的boolean值
     *
     * @param key key
     * @return 返回指定key的值
     */
    Boolean getBoolean(String key);

    /**
     * 获取指定key的 boolean 值
     *
     * @param key          key
     * @param defaultValue 默认值， 如果返回为 null 将返回默认值
     * @return 返回指定key的值
     */
    Boolean getBoolean(String key, Boolean defaultValue);

    /**
     * 获取指定key的 long 值
     *
     * @param key key
     * @return 返回指定key的值
     */
    Long getLong(String key);

    /**
     * 获取指定key的 long 值
     *
     * @param key          key
     * @param defaultValue 默认值， 如果返回为 null 将返回默认值
     * @return 返回指定key的值
     */
    Long getLong(String key, Long defaultValue);

    /**
     * 获取指定key的 Integer 值
     *
     * @param key key
     * @return 返回指定key的值
     */
    Integer getInteger(String key);

    /**
     * 获取指定key的 Integer 值
     *
     * @param key          key
     * @param defaultValue 默认值， 如果返回为 null 将返回默认值
     * @return 返回指定key的值
     */
    Integer getInteger(String key, Integer defaultValue);

    /**
     * 获取指定key的 Date 值
     *
     * @param key key
     * @return 返回指定key的值
     */
    Date getDate(String key);

    /**
     * 获取指定key的 Date 值
     *
     * @param key          key
     * @param defaultValue 默认值， 如果返回为 null 将返回默认值
     * @return 返回指定key的值
     */
    Date getDate(String key, Date defaultValue);

    /**
     * 设置过期时间
     *
     * @param key           要设置的key
     * @param expireSeconds 过期时间，单位是秒
     * @return 返回redis结果状态码
     */
    Long expire(String key, int expireSeconds);

    /**
     * 设置过期时间
     *
     * @param key               要设置的key
     * @param unixTimeInSeconds unix时间戳，单位是秒，使用毫秒的时候需要除以 1000， 转换成秒
     * @return 返回redis结果状态码
     */
    Long expireAt(String key, long unixTimeInSeconds);

    /**
     * 获取 map 中的值
     *
     * @param mapKey   map 的key
     * @param mapField map 对应的属性
     * @return 返回字符串
     */
    String hget(String mapKey, String mapField);

    /**
     * 返回集合
     *
     * @param key 集合 key
     * @return 返回集合成员，始终返回非 null
     */
    Set<String> smembers(String key);

    /**
     * 判断指定的值是否在指定集合中
     *
     * @param key   集合key
     * @param value 要检查的值
     * @return 返回是否存在
     */
    boolean sismember(String key, String value);

    /**
     * 检查某个 key 是否存在
     *
     * @param key 返回 key
     * @return true - 存在， false - 不存在
     */
    boolean exists(String key);

    String set(String key, String value);

    String set(String key, String value, String nxxx, String expx, long time);

    String set(String key, String value, String nxxx);

    String get(String key);

    Long persist(String key);

    String type(String key);

    Long pexpire(String key, long milliseconds);

    Long pexpireAt(String key, long millisecondsTimestamp);

    Long ttl(String key);

    Long pttl(final String key);

    Boolean setbit(String key, long offset, boolean value);

    Boolean setbit(String key, long offset, String value);

    Boolean getbit(String key, long offset);

    Long setrange(String key, long offset, String value);

    String getrange(String key, long startOffset, long endOffset);

    String getSet(String key, String value);

    Long setnx(String key, String value);

    String setex(String key, int seconds, String value);

    String psetex(final String key, final long milliseconds, final String value);

    Long decrBy(String key, long integer);

    Long decr(String key);

    Long incrBy(String key, long integer);

    Double incrByFloat(String key, double value);

    Long incr(String key);

    Long append(String key, String value);

    String substr(String key, int start, int end);

    Long hset(String key, String field, String value);

    Long hsetnx(String key, String field, String value);

    String hmset(String key, Map<String, String> hash);

    List<String> hmget(String key, String... fields);

    Long hincrBy(String key, String field, long value);

    Double hincrByFloat(final String key, final String field, final double value);

    Boolean hexists(String key, String field);

    Long hdel(String key, String... field);

    Long hlen(String key);

    Set<String> hkeys(String key);

    List<String> hvals(String key);

    Map<String, String> hgetAll(String key);

    Long rpush(String key, String... string);

    Long lpush(String key, String... string);

    Long llen(String key);

    List<String> lrange(String key, long start, long end);

    String ltrim(String key, long start, long end);

    String lindex(String key, long index);

    String lset(String key, long index, String value);

    Long lrem(String key, long count, String value);

    String lpop(String key);

    String rpop(String key);

    Long sadd(String key, String... member);

    Long srem(String key, String... member);

    String spop(String key);

    Set<String> spop(String key, long count);

    Long scard(String key);

    String srandmember(String key);

    List<String> srandmember(String key, int count);

    Long strlen(String key);

    Long zadd(String key, double score, String member);

    Long zadd(String key, double score, String member, ZAddParams params);

    Long zadd(String key, Map<String, Double> scoreMembers);

    Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);

    Set<String> zrange(String key, long start, long end);

    Long zrem(String key, String... member);

    Double zincrby(String key, double score, String member);

    Double zincrby(String key, double score, String member, ZIncrByParams params);

    Long zrank(String key, String member);

    Long zrevrank(String key, String member);

    Set<String> zrevrange(String key, long start, long end);

    Set<Tuple> zrangeWithScores(String key, long start, long end);

    Set<Tuple> zrevrangeWithScores(String key, long start, long end);

    Long zcard(String key);

    Double zscore(String key, String member);

    List<String> sort(String key);

    List<String> sort(String key, SortingParams sortingParameters);

    Long zcount(String key, double min, double max);

    Long zcount(String key, String min, String max);

    Set<String> zrangeByScore(String key, double min, double max);

    Set<String> zrangeByScore(String key, String min, String max);

    Set<String> zrevrangeByScore(String key, double max, double min);

    Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

    Set<String> zrevrangeByScore(String key, String max, String min);

    Set<String> zrangeByScore(String key, String min, String max, int offset, int count);

    Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);

    Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min);

    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

    Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max);

    Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min);

    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);

    Long zremrangeByRank(String key, long start, long end);

    Long zremrangeByScore(String key, double start, double end);

    Long zremrangeByScore(String key, String start, String end);

    Long zlexcount(final String key, final String min, final String max);

    Set<String> zrangeByLex(final String key, final String min, final String max);

    Set<String> zrangeByLex(final String key, final String min, final String max, final int offset, final int count);

    Set<String> zrevrangeByLex(final String key, final String max, final String min);

    Set<String> zrevrangeByLex(final String key, final String max, final String min, final int offset, final int count);

    Long zremrangeByLex(final String key, final String min, final String max);

    Long linsert(String key, Client.LIST_POSITION where, String pivot, String value);

    Long lpushx(String key, String... string);

    Long rpushx(String key, String... string);

    @Deprecated
    List<String> blpop(String arg);

    List<String> blpop(int timeout, String key);

    @Deprecated
    List<String> brpop(String arg);

    List<String> brpop(int timeout, String key);

    Long del(String key);

    String echo(String string);

    Long move(String key, int dbIndex);

    Long bitcount(final String key);

    Long bitcount(final String key, long start, long end);

    Long bitpos(final String key, final boolean value);

    Long bitpos(final String key, final boolean value, final BitPosParams params);

    @Deprecated
    ScanResult<Map.Entry<String, String>> hscan(final String key, int cursor);

    @Deprecated
    ScanResult<String> sscan(final String key, int cursor);

    @Deprecated
    ScanResult<Tuple> zscan(final String key, int cursor);

    ScanResult<Map.Entry<String, String>> hscan(final String key, final String cursor);

    ScanResult<Map.Entry<String, String>> hscan(final String key, final String cursor, final ScanParams params);

    ScanResult<String> sscan(final String key, final String cursor);

    ScanResult<String> sscan(final String key, final String cursor, final ScanParams params);

    ScanResult<Tuple> zscan(final String key, final String cursor);

    ScanResult<Tuple> zscan(final String key, final String cursor, final ScanParams params);

    Long pfadd(final String key, final String... elements);

    long pfcount(final String key);

    // Geo Commands

    Long geoadd(String key, double longitude, double latitude, String member);

    Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap);

    Double geodist(String key, String member1, String member2);

    Double geodist(String key, String member1, String member2, GeoUnit unit);

    List<String> geohash(String key, String... members);

    List<GeoCoordinate> geopos(String key, String... members);

    List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param);

    List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param);

    List<Long> bitfield(String key, String... arguments);

    // MultiKeyCommands start

    Long del(String... keys);

    Long exists(String... keys);

    List<String> blpop(int timeout, String... keys);

    List<String> brpop(int timeout, String... keys);

    List<String> blpop(String... args);

    List<String> brpop(String... args);

    Set<String> keys(String pattern);

    List<String> mget(String... keys);

    String mset(String... keysvalues);

    Long msetnx(String... keysvalues);

    String rename(String oldkey, String newkey);

    Long renamenx(String oldkey, String newkey);

    String rpoplpush(String srckey, String dstkey);

    Set<String> sdiff(String... keys);

    Long sdiffstore(String dstkey, String... keys);

    Set<String> sinter(String... keys);

    Long sinterstore(String dstkey, String... keys);

    Long smove(String srckey, String dstkey, String member);

    Long sort(String key, SortingParams sortingParameters, String dstkey);

    Long sort(String key, String dstkey);

    Set<String> sunion(String... keys);

    Long sunionstore(String dstkey, String... keys);

    String watch(String... keys);

    String unwatch();

    Long zinterstore(String dstkey, String... sets);

    Long zinterstore(String dstkey, ZParams params, String... sets);

    Long zunionstore(String dstkey, String... sets);

    Long zunionstore(String dstkey, ZParams params, String... sets);

    String brpoplpush(String source, String destination, int timeout);

    Long publish(String channel, String message);

    void subscribe(JedisPubSub jedisPubSub, String... channels);

    void psubscribe(JedisPubSub jedisPubSub, String... patterns);

    String randomKey();

    Long bitop(BitOP op, final String destKey, String... srcKeys);

    ScanResult<String> scan(final String cursor);

    ScanResult<String> scan(final String cursor, final ScanParams params);

    String pfmerge(final String destkey, final String... sourcekeys);

    long pfcount(final String... keys);

    // MultiKeyCommands end

    // AdvancedJedisCommands start

    List<String> configGet(String pattern);

    String configSet(String parameter, String value);

    String slowlogReset();

    Long slowlogLen();

    List<Slowlog> slowlogGet();

    List<Slowlog> slowlogGet(long entries);

    Long objectRefcount(String string);

    String objectEncoding(String string);

    Long objectIdletime(String string);

    // AdvancedJedisCommands start

    // ScriptingCommands start

    Object eval(String script, int keyCount, String... params);

    Object eval(String script, List<String> keys, List<String> args);

    Object eval(String script);

    Object evalsha(String script);

    Object evalsha(String sha1, List<String> keys, List<String> args);

    Object evalsha(String sha1, int keyCount, String... params);

    Boolean scriptExists(String sha1);

    List<Boolean> scriptExists(String... sha1);

    String scriptLoad(String script);

    // ScriptingCommands end

    // BasicCommands start

    String ping();

    String quit();

    String flushDB();

    Long dbSize();

    String select(int index);

    String flushAll();

    String auth(String password);

    String save();

    String bgsave();

    String bgrewriteaof();

    Long lastsave();

    String shutdown();

    String info();

    String info(String section);

    String slaveof(String host, int port);

    String slaveofNoOne();

    Long getDB();

    String debug(DebugParams params);

    String configResetStat();

    Long waitReplicas(int replicas, long timeout);

    // BasicCommands end

    // ClusterCommands start

    String clusterNodes();

    String clusterMeet(final String ip, final int port);

    String clusterAddSlots(final int... slots);

    String clusterDelSlots(final int... slots);

    String clusterInfo();

    List<String> clusterGetKeysInSlot(final int slot, final int count);

    String clusterSetSlotNode(final int slot, final String nodeId);

    String clusterSetSlotMigrating(final int slot, final String nodeId);

    String clusterSetSlotImporting(final int slot, final String nodeId);

    String clusterSetSlotStable(final int slot);

    String clusterForget(final String nodeId);

    String clusterFlushSlots();

    Long clusterKeySlot(final String key);

    Long clusterCountKeysInSlot(final int slot);

    String clusterSaveConfig();

    String clusterReplicate(final String nodeId);

    List<String> clusterSlaves(final String nodeId);

    String clusterFailover();

    List<Object> clusterSlots();

    String clusterReset(JedisCluster.Reset resetType);

    String readonly();

    // ClusterCommands end

    // SentinelCommands start

    List<Map<String, String>> sentinelMasters();

    List<String> sentinelGetMasterAddrByName(String masterName);

    Long sentinelReset(String pattern);

    List<Map<String, String>> sentinelSlaves(String masterName);

    String sentinelFailover(String masterName);

    String sentinelMonitor(String masterName, String ip, int port, int quorum);

    String sentinelRemove(String masterName);

    String sentinelSet(String masterName, Map<String, String> parameterMap);

    // SentinelCommands end

    // BinaryJedisCommands start

    String set(byte[] key, byte[] value);

    String set(byte[] key, byte[] value, byte[] nxxx);

    String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time);

    byte[] get(byte[] key);

    Boolean exists(byte[] key);

    Long persist(byte[] key);

    String type(byte[] key);

    Long expire(byte[] key, int seconds);

    Long pexpire(byte[] key, final long milliseconds);

    Long expireAt(byte[] key, long unixTime);

    Long pexpireAt(byte[] key, long millisecondsTimestamp);

    Long ttl(byte[] key);

    Boolean setbit(byte[] key, long offset, boolean value);

    Boolean setbit(byte[] key, long offset, byte[] value);

    Boolean getbit(byte[] key, long offset);

    Long setrange(byte[] key, long offset, byte[] value);

    byte[] getrange(byte[] key, long startOffset, long endOffset);

    byte[] getSet(byte[] key, byte[] value);

    Long setnx(byte[] key, byte[] value);

    String setex(byte[] key, int seconds, byte[] value);

    Long decrBy(byte[] key, long integer);

    Long decr(byte[] key);

    Long incrBy(byte[] key, long integer);

    Double incrByFloat(byte[] key, double value);

    Long incr(byte[] key);

    Long append(byte[] key, byte[] value);

    byte[] substr(byte[] key, int start, int end);

    Long hset(byte[] key, byte[] field, byte[] value);

    byte[] hget(byte[] key, byte[] field);

    Long hsetnx(byte[] key, byte[] field, byte[] value);

    String hmset(byte[] key, Map<byte[], byte[]> hash);

    List<byte[]> hmget(byte[] key, byte[]... fields);

    Long hincrBy(byte[] key, byte[] field, long value);

    Double hincrByFloat(byte[] key, byte[] field, double value);

    Boolean hexists(byte[] key, byte[] field);

    Long hdel(byte[] key, byte[]... field);

    Long hlen(byte[] key);

    Set<byte[]> hkeys(byte[] key);

    Collection<byte[]> hvals(byte[] key);

    Map<byte[], byte[]> hgetAll(byte[] key);

    Long rpush(byte[] key, byte[]... args);

    Long lpush(byte[] key, byte[]... args);

    Long llen(byte[] key);

    List<byte[]> lrange(byte[] key, long start, long end);

    String ltrim(byte[] key, long start, long end);

    byte[] lindex(byte[] key, long index);

    String lset(byte[] key, long index, byte[] value);

    Long lrem(byte[] key, long count, byte[] value);

    byte[] lpop(byte[] key);

    byte[] rpop(byte[] key);

    Long sadd(byte[] key, byte[]... member);

    Set<byte[]> smembers(byte[] key);

    Long srem(byte[] key, byte[]... member);

    byte[] spop(byte[] key);

    Set<byte[]> spop(byte[] key, long count);

    Long scard(byte[] key);

    Boolean sismember(byte[] key, byte[] member);

    byte[] srandmember(byte[] key);

    List<byte[]> srandmember(final byte[] key, final int count);

    Long strlen(byte[] key);

    Long zadd(byte[] key, double score, byte[] member);

    Long zadd(byte[] key, double score, byte[] member, ZAddParams params);

    Long zadd(byte[] key, Map<byte[], Double> scoreMembers);

    Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params);

    Set<byte[]> zrange(byte[] key, long start, long end);

    Long zrem(byte[] key, byte[]... member);

    Double zincrby(byte[] key, double score, byte[] member);

    Double zincrby(byte[] key, double score, byte[] member, ZIncrByParams params);

    Long zrank(byte[] key, byte[] member);

    Long zrevrank(byte[] key, byte[] member);

    Set<byte[]> zrevrange(byte[] key, long start, long end);

    Set<Tuple> zrangeWithScores(byte[] key, long start, long end);

    Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end);

    Long zcard(byte[] key);

    Double zscore(byte[] key, byte[] member);

    List<byte[]> sort(byte[] key);

    List<byte[]> sort(byte[] key, SortingParams sortingParameters);

    Long zcount(byte[] key, double min, double max);

    Long zcount(byte[] key, byte[] min, byte[] max);

    Set<byte[]> zrangeByScore(byte[] key, double min, double max);

    Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max);

    Set<byte[]> zrevrangeByScore(byte[] key, double max, double min);

    Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count);

    Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min);

    Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count);

    Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max);

    Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count);

    Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max);

    Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min);

    Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count);

    Long zremrangeByRank(byte[] key, long start, long end);

    Long zremrangeByScore(byte[] key, double start, double end);

    Long zremrangeByScore(byte[] key, byte[] start, byte[] end);

    Long zlexcount(final byte[] key, final byte[] min, final byte[] max);

    Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max);

    Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max, int offset, int count);

    Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min);

    Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min, int offset, int count);

    Long zremrangeByLex(final byte[] key, final byte[] min, final byte[] max);

    Long linsert(byte[] key, Client.LIST_POSITION where, byte[] pivot, byte[] value);

    Long lpushx(byte[] key, byte[]... arg);

    Long rpushx(byte[] key, byte[]... arg);

    Long del(byte[] key);

    byte[] echo(byte[] arg);

    Long move(byte[] key, int dbIndex);

    Long bitcount(final byte[] key);

    Long bitcount(final byte[] key, long start, long end);

    Long pfadd(final byte[] key, final byte[]... elements);

    long pfcount(final byte[] key);

    // Geo Commands

    Long geoadd(byte[] key, double longitude, double latitude, byte[] member);

    Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap);

    Double geodist(byte[] key, byte[] member1, byte[] member2);

    Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit);

    List<byte[]> geohash(byte[] key, byte[]... members);

    List<GeoCoordinate> geopos(byte[] key, byte[]... members);

    List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param);

    List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit);

    List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param);

    ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor);

    ScanResult<Map.Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params);

    ScanResult<byte[]> sscan(byte[] key, byte[] cursor);

    ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params);

    ScanResult<Tuple> zscan(byte[] key, byte[] cursor);

    ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params);

    List<byte[]> bitfield(final byte[] key, final byte[]... arguments);

    // BinaryJedisCommands end

    // MultiKeyBinaryCommands start

    Long del(byte[]... keys);

    Long exists(byte[]... keys);

    List<byte[]> blpop(int timeout, byte[]... keys);

    List<byte[]> brpop(int timeout, byte[]... keys);

    List<byte[]> blpop(byte[]... args);

    List<byte[]> brpop(byte[]... args);

    Set<byte[]> keys(byte[] pattern);

    List<byte[]> mget(byte[]... keys);

    String mset(byte[]... keysvalues);

    Long msetnx(byte[]... keysvalues);

    String rename(byte[] oldkey, byte[] newkey);

    Long renamenx(byte[] oldkey, byte[] newkey);

    byte[] rpoplpush(byte[] srckey, byte[] dstkey);

    Set<byte[]> sdiff(byte[]... keys);

    Long sdiffstore(byte[] dstkey, byte[]... keys);

    Set<byte[]> sinter(byte[]... keys);

    Long sinterstore(byte[] dstkey, byte[]... keys);

    Long smove(byte[] srckey, byte[] dstkey, byte[] member);

    Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey);

    Long sort(byte[] key, byte[] dstkey);

    Set<byte[]> sunion(byte[]... keys);

    Long sunionstore(byte[] dstkey, byte[]... keys);

    String watch(byte[]... keys);

    Long zinterstore(byte[] dstkey, byte[]... sets);

    Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets);

    Long zunionstore(byte[] dstkey, byte[]... sets);

    Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets);

    byte[] brpoplpush(byte[] source, byte[] destination, int timeout);

    Long publish(byte[] channel, byte[] message);

    void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels);

    void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns);

    byte[] randomBinaryKey();

    Long bitop(BitOP op, final byte[] destKey, byte[]... srcKeys);

    String pfmerge(final byte[] destkey, final byte[]... sourcekeys);

    Long pfcount(byte[]... keys);

    // MultiKeyBinaryCommands end

    // BinaryScriptingCommands start

    Object eval(byte[] script, byte[] keyCount, byte[]... params);

    Object eval(byte[] script, int keyCount, byte[]... params);

    Object eval(byte[] script, List<byte[]> keys, List<byte[]> args);

    Object eval(byte[] script);

    Object evalsha(byte[] script);

    Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args);

    Object evalsha(byte[] sha1, int keyCount, byte[]... params);

    List<Long> scriptExists(byte[]... sha1);

    byte[] scriptLoad(byte[] script);

    String scriptFlush();

    String scriptKill();

    // BinaryScriptingCommands end
}

package com.commonspringboot.autoconfigure.redis.common;

import redis.clients.jedis.Jedis;

/**
 * Redis 执行器
 *
 * @author yzy
 */
public interface JedisExecutor<T> {

    /**
     * 执行并返回结果
     *
     * @param jedis redis 连接
     * @return 返回结果
     */
    T execute(Jedis jedis);

}

package com.commonspringboot.autoconfigure.redis.common;

import redis.clients.jedis.Jedis;

/**
 * Jedis 资源提供者
 *
 * @author yzy
 */
public interface JedisProvider {

    /**
     * 返回资源
     *
     * @return 资源
     */
    Jedis getResource();

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
}

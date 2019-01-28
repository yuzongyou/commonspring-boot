package com.commonspringboot.autoconfigure.redis.common.provider.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 默认 Jedis 连接提供者，直接使用 JedisPool 进行提供
 *
 * @author yzy
 */
public class DefaultJedisProvider extends AbstractProvider {

    /**
     * Jedis 连接池
     */
    protected JedisPool jedisPool;

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public Jedis getResource() {
        return jedisPool.getResource();
    }
}

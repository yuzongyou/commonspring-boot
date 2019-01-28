package com.commonspringboot.autoconfigure.redis.common.provider.jedis;


import com.commonspringboot.autoconfigure.redis.common.JedisProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * @author yzy
 */
public abstract class AbstractProvider implements JedisProvider {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void closeResource(Jedis jedis) {
        if (null != jedis) {
            jedis.close();
        }
    }

    @Override
    public void destroyResource(Jedis jedis) {
        if (null != jedis) {
            jedis.close();
        }
    }
}

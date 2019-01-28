package com.commonspringboot.autoconfigure.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import redis.clients.jedis.Jedis;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/13 19:47
 */
@Configuration
@ConditionalOnClass({Jedis.class})
@EnableConfigurationProperties(CommonRedisProperties.class)
public class CommonRedisAutoConfiguration {

}

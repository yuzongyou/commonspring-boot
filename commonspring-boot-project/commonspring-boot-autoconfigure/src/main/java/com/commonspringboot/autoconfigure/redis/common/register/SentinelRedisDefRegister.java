package com.commonspringboot.autoconfigure.redis.common.register;

import com.commonspringboot.autoconfigure.redis.common.DefaultRedisImpl;
import com.commonspringboot.autoconfigure.redis.common.model.SentinelRedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.provider.jedis.SentinelJedisProvider;
import com.myspringboot.commonspringboot.CommonUtil;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.Environment;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Map;

/**
 * <pre>
 *
 *     支持简单类型的 SentinelRedisDefinition 注册，注册结果包含如下Bean
 *     {redisId}Redis             DefaultRedisImpl
 *     {redisId}JedisPoolConfig   JedisPoolConfig
 *     {redisId}JedisProvider     SentinelJedisProvider
 *     {redisId}JedisPool         JedisSentinelPool
 *
 * </pre>
 *
 * @author yzy
 */
public class SentinelRedisDefRegister extends AbstractRedisRegister<SentinelRedisDefinition> {

    @Override
    protected void register(SentinelRedisDefinition definition, Environment environment, BeanDefinitionRegistry registry) {

        if (null == definition) {
            return;
        }

        String poolConfigBeanName = registerPoolConfigBeanDefinition(definition, registry);

        String jedisPoolBeanName = registerJedisPoolBeanDefinition(definition, registry, poolConfigBeanName);

        String providerBeanName = registerJedisProviderBeanDefinition(definition, registry, jedisPoolBeanName);

        registerRedisBeanDefinition(definition, registry, providerBeanName);

    }

    private void registerRedisBeanDefinition(SentinelRedisDefinition definition, BeanDefinitionRegistry registry, String providerBeanName) {
        String redisBeanName = definition.getId() + "Redis";
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DefaultRedisImpl.class);
        beanDefinition.setLazyInit(true);
        beanDefinition.getPropertyValues().addPropertyValue("provider", new RuntimeBeanReference(providerBeanName));

        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setPrimary(definition.isPrimary());

        registry.registerBeanDefinition(redisBeanName, beanDefinition);
    }

    private String registerJedisProviderBeanDefinition(SentinelRedisDefinition definition, BeanDefinitionRegistry registry, String jedisPoolBeanName) {
        String providerBeanName = definition.getId() + "JedisProvider";
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(SentinelJedisProvider.class);
        beanDefinition.setLazyInit(true);
        beanDefinition.getPropertyValues().addPropertyValue("jedisSentinelPool", new RuntimeBeanReference(jedisPoolBeanName));

        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setPrimary(definition.isPrimary());

        registry.registerBeanDefinition(providerBeanName, beanDefinition);
        return providerBeanName;
    }

    private String registerJedisPoolBeanDefinition(SentinelRedisDefinition definition, BeanDefinitionRegistry registry, String poolConfigBeanName) {
        String jedisPoolBeanName = definition.getId() + "JedisPool";
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(JedisSentinelPool.class);
        beanDefinition.setLazyInit(true);

        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, definition.getMasterName());
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, definition.getSentinelSet());
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(2, new RuntimeBeanReference(poolConfigBeanName));
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(3, definition.getTimeout());
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(4, definition.getPassword());
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(5, definition.getDatabase());

        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setPrimary(definition.isPrimary());

        registry.registerBeanDefinition(jedisPoolBeanName, beanDefinition);

        return jedisPoolBeanName;
    }

    private String registerPoolConfigBeanDefinition(SentinelRedisDefinition definition, BeanDefinitionRegistry registry) {
        String poolConfigBeanName = definition.getId() + "JedisPoolConfig";
        Class<?> poolClass = JedisPoolConfig.class;
        // 移除不识别的连接池配置
        Map<String, String> poolConfig = CommonUtil.filterUnRecordedField(definition.getPoolConfig(), poolClass);
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(poolClass);
        beanDefinition.setLazyInit(true);
        MutablePropertyValues properties = new MutablePropertyValues(poolConfig);
        beanDefinition.getPropertyValues().addPropertyValues(properties);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

        beanDefinition.setPrimary(definition.isPrimary());

        registry.registerBeanDefinition(poolConfigBeanName, beanDefinition);
        return poolConfigBeanName;
    }

}

package com.commonspringboot.autoconfigure.redis;


import com.myspringboot.commonspringboot.SpringApplicationRunListenerAdapter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/26 15:17
 */
public class CommonRedisConfigApplicationRunListener extends SpringApplicationRunListenerAdapter {

    private boolean needAutoConfigurer = false;

    public CommonRedisConfigApplicationRunListener(SpringApplication application, String[] args) {
        super(application, args);
        this.needAutoConfigurer = isClassesImported("com.duowan.common.redis.Redis");
    }

    @Override
    protected boolean needAutoConfigurer() {
        return needAutoConfigurer;
    }

    @Override
    protected void doContextPrepared(ConfigurableApplicationContext context, BeanDefinitionRegistry registry, ConfigurableEnvironment environment) {
        CommonRedisProperties redisProperties = bindProperties(CommonRedisProperties.PROPERTIES_PREFIX, CommonRedisProperties.class);

        if (null != redisProperties) {
            CommonRedisSpringRegister.registerRedisBeans(redisProperties, context, registry, environment);
        }
    }

}

package com.commonspringboot.autoconfigure.redis.common.register;

import com.commonspringboot.autoconfigure.redis.common.model.RedisDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

public interface RedisRegister {
    boolean canHandle(RedisDefinition var1);

    void registerRedis(RedisDefinition var1, Environment var2, BeanDefinitionRegistry var3);
}


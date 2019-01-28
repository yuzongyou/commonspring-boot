package com.commonspringboot.autoconfigure.redis.common.provider.def;


import com.commonspringboot.autoconfigure.redis.common.model.RedisDefinition;

/**
 * @author yzy
 * @since 2018/5/23 9:43
 */
public interface RedisDefinitionProvider {

    /**
     * 是否支持处理该RedisDef
     *
     * @param redisDefinition redis定义
     * @return 返回是否支持处理
     */
    boolean support(RedisDefinition redisDefinition);

    /**
     * 检查配置同时应用默认配置
     *
     * @param redisDefinition redis定义
     * @return true 表示成功处理了，false标识没有成功处理
     */
    boolean checkAndApplyDefaultConfig(RedisDefinition redisDefinition);


}

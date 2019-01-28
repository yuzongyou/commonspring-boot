package com.commonspringboot.autoconfigure.redis.common;


import com.commonspringboot.autoconfigure.redis.common.model.RedisDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/11/22 20:47
 */
public class RedisDefinitionContext {

    private Map<String, RedisDefinition> redisDefinitionMap;

    public RedisDefinitionContext(List<RedisDefinition> redisDefinitionList) {
        if (null != redisDefinitionList) {
            redisDefinitionMap = new HashMap<>(redisDefinitionList.size());
            for (RedisDefinition definition : redisDefinitionList) {
                redisDefinitionMap.put(definition.getId(), definition);
            }
        }
    }

    public RedisDefinition getRedisDefinition(String redisId) {
        return null == redisDefinitionMap ? null : redisDefinitionMap.get(redisId);
    }

    public Map<String, RedisDefinition> getRedisDefinitionMap() {
        return redisDefinitionMap;
    }
}

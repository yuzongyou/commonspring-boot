package com.commonspringboot.autoconfigure.redis;


import com.commonspringboot.autoconfigure.redis.common.model.RedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.model.RiseRedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.model.SentinelRedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.model.StdRedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.provider.def.RedisDefinitionProvider;
import com.commonspringboot.autoconfigure.redis.common.register.RedisRegister;
import com.commonspringboot.autoconfigure.redis.common.util.RedisRegisterUtil;
import com.myspringboot.commonspringboot.CommonUtil;
import com.myspringboot.commonspringboot.ReflectUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/12/28 14:11
 */
public class CommonRedisSpringRegister {

    private CommonRedisSpringRegister() {
        throw new IllegalStateException("Utility class");
    }

    public static void registerRedisBeans(CommonRedisProperties redisProperties, ApplicationContext applicationContext, BeanDefinitionRegistry registry, Environment environment) {
        List<RedisRegister> registerList = ReflectUtil.newInstancesByDefaultConstructor(RedisRegister.class, redisProperties.getRegisterClasses());
        List<RedisDefinitionProvider> providerList = ReflectUtil.newInstancesByDefaultConstructor(RedisDefinitionProvider.class, redisProperties.getRegisterClasses());

        RedisRegisterUtil.registerRedisBeanDefinitions(
                registerList,
                providerList,
                lookupRedisDefList(redisProperties),
                redisProperties.getEnabledIds(),
                redisProperties.getExcludeIds(),
                redisProperties.getPrimaryId(),
                registry,
                environment);
    }

    private static List<RedisDefinition> lookupRedisDefList(CommonRedisProperties redisProperties) {

        List<RedisDefinition> resultList = new ArrayList<>();

        CommonUtil.appendList(resultList, lookupStandardRedisDefList(redisProperties));
        CommonUtil.appendList(resultList, lookupRiseRedisDefList(redisProperties));
        CommonUtil.appendList(resultList, lookupSentinelRedisDefList(redisProperties));

        return resultList;
    }

    private static List<RedisDefinition> lookupStandardRedisDefList(CommonRedisProperties redisProperties) {

        Map<String, StdRedisDefinition> standardMap = redisProperties.getStandards();
        List<RedisDefinition> resultList = new ArrayList<>();

        if (null == standardMap || standardMap.isEmpty()) {
            return resultList;
        }

        for (Map.Entry<String, StdRedisDefinition> entry : standardMap.entrySet()) {
            StdRedisDefinition def = entry.getValue();
            if (StringUtils.isEmpty(def.getId())) {
                def.setId(entry.getKey());
            }
            resultList.add(def);
        }

        return resultList;
    }

    private static List<RedisDefinition> lookupSentinelRedisDefList(CommonRedisProperties redisProperties) {

        Map<String, SentinelRedisDefinition> standardMap = redisProperties.getSentinels();
        List<RedisDefinition> resultList = new ArrayList<>();

        if (null == standardMap || standardMap.isEmpty()) {
            return resultList;
        }

        for (Map.Entry<String, SentinelRedisDefinition> entry : standardMap.entrySet()) {
            SentinelRedisDefinition def = entry.getValue();
            if (StringUtils.isEmpty(def.getId())) {
                def.setId(entry.getKey());
            }
            resultList.add(def);
        }

        return resultList;
    }

    private static List<RedisDefinition> lookupRiseRedisDefList(CommonRedisProperties redisProperties) {

        Map<String, RiseRedisDefinition> standardMap = redisProperties.getRises();
        List<RedisDefinition> resultList = new ArrayList<>();

        if (null == standardMap || standardMap.isEmpty()) {
            return resultList;
        }

        Map<String, String> aliasMap = redisProperties.getRiseAlias();

        for (Map.Entry<String, RiseRedisDefinition> entry : standardMap.entrySet()) {
            RiseRedisDefinition def = entry.getValue();
            if (StringUtils.isEmpty(def.getId())) {
                def.setId(entry.getKey());
            }
            String dsName = def.getName();
            if (aliasMap != null) {
                String aliasDsName = aliasMap.get(dsName);
                if (!StringUtils.isEmpty(aliasDsName)) {
                    def.setName(aliasDsName);
                }
            }
            resultList.add(def);
        }

        return resultList;
    }
}

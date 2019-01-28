package com.commonspringboot.autoconfigure.redis.common.register;

import com.commonspringboot.autoconfigure.redis.common.model.RedisDefinition;
import com.myspringboot.commonspringboot.CommonUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * @param <T> Redis 定义注册器
 * @author yzy
 */
public abstract class AbstractRedisRegister<T extends RedisDefinition> implements RedisRegister {
    protected Class<T> definitionClass = (Class)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    public AbstractRedisRegister() {
    }

    public boolean canHandle(RedisDefinition redisDefinition) {
        return this.definitionClass.isInstance(redisDefinition);
    }

    public final void registerRedis(RedisDefinition redisDefinition, Environment environment, BeanDefinitionRegistry registry) {
        Assert.isTrue(this.canHandle(redisDefinition), "[" + this.getClass().getName() + "] 无法注册[" + redisDefinition + "]");
        this.register((T) redisDefinition, environment, registry);
    }

    protected Map<String, Object> removeDefinitionUnSupportPoolConfig(Map<String, Object> poolConfig, Class<?> poolClass) {
        Set<String> fieldNames = poolConfig.keySet();
        Method[] allMethods = poolClass.getMethods();
        Map<String, Method> getMethodMap = new HashMap();
        Map<String, Method> setMethodMap = new HashMap();
        Method[] var7 = allMethods;
        int var8 = allMethods.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            Method method = var7[var9];
            String methodName = method.getName();
            String fieldName = CommonUtil.firstLetterToLowerCase(methodName.replaceFirst("is|get|set", ""));
            if (!methodName.startsWith("is") && !methodName.startsWith("get")) {
                if (methodName.startsWith("set")) {
                    setMethodMap.put(fieldName, method);
                }
            } else {
                getMethodMap.put(fieldName, method);
            }
        }

        Set<String> needRemoveFieldNames = new HashSet();
        Iterator var14 = fieldNames.iterator();

        while(true) {
            String needRemoveFieldName;
            do {
                if (!var14.hasNext()) {
                    if (!needRemoveFieldNames.isEmpty()) {
                        var14 = needRemoveFieldNames.iterator();

                        while(var14.hasNext()) {
                            needRemoveFieldName = (String)var14.next();
                            poolConfig.remove(needRemoveFieldName);
                        }
                    }

                    return poolConfig;
                }

                needRemoveFieldName = (String)var14.next();
            } while(getMethodMap.containsKey(needRemoveFieldName) && setMethodMap.containsKey(needRemoveFieldName));

            needRemoveFieldNames.add(needRemoveFieldName);
        }
    }

    protected abstract void register(T var1, Environment var2, BeanDefinitionRegistry var3);
}
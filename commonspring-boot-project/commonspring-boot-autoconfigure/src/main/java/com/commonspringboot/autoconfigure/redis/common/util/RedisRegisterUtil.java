package com.commonspringboot.autoconfigure.redis.common.util;

import com.commonspringboot.autoconfigure.redis.common.RedisDefinitionContext;
import com.commonspringboot.autoconfigure.redis.common.model.RedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.provider.def.RedisDefinitionProvider;
import com.commonspringboot.autoconfigure.redis.common.register.RedisRegister;
import com.myspringboot.commonspringboot.CommonUtil;
import com.myspringboot.commonspringboot.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.*;

public class RedisRegisterUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRegisterUtil.class);
    private static Map<String, RedisDefinition> redisDefMap = new HashMap();
    private static volatile RedisDefinition primaryRedisDef = null;

    private RedisRegisterUtil() {
        throw new IllegalStateException("Utility Class");
    }

    public static void registerRedisBeanDefinitions(List<RedisRegister> registerList, List<RedisDefinitionProvider> providerList, List<RedisDefinition> redisDefinitionList, Set<String> enabledIds, Set<String> excludeIds, String primaryId, BeanDefinitionRegistry registry, Environment environment) {
        if (null != redisDefinitionList && !redisDefinitionList.isEmpty()) {
            redisDefinitionList = RedisDefinitionUtil.filterExcludeRedisDefList(excludeIds, redisDefinitionList);
            redisDefinitionList = RedisDefinitionUtil.extractEnabledRedisDefList(enabledIds, redisDefinitionList);
            redisDefinitionList = RedisDefinitionUtil.autoFillProperties(redisDefinitionList, environment);
            redisDefinitionList = RedisDefinitionUtil.applyPrimaryJdbcDefList(primaryId, redisDefinitionList);
            redisDefinitionList = checkRedisDefList(redisDefinitionList);
            registerList = appendDefaultRegisterAndFilterDuplicateInstance(registerList);
            Assert.isTrue(registerList!=null,"Redis注册列表不能为空，当前解析Redis列表数： " + redisDefinitionList.size());
            providerList = appendDefaultProviderAndFilterDuplicateInstance(providerList);
            RedisDefinition primary = null;
            Iterator var9 = redisDefinitionList.iterator();

            while(var9.hasNext()) {
                RedisDefinition redisDefinition = (RedisDefinition)var9.next();
                if (primary != null) {
                    Assert.isTrue(!redisDefinition.isPrimary(), "当前 Redis 定义 primary 冲突，只能设置一个primary,冲突项：[" + primary.getId() + "," + redisDefinition.getId() + "]");
                }

                if (redisDefinition.isPrimary()) {
                    primary = redisDefinition;
                }

                applyRedisDefinitionDefaultConfig(redisDefinition, providerList);
                RedisRegister register = lookupRegister(registerList, redisDefinition);
                Assert.isTrue(register!=null,redisDefinition.getClass().getName() + " 找不到对应的Redis 注册器！");
                LOGGER.info("自动注册Redis数据源：{}", redisDefinition);
            }

            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(RedisDefinitionContext.class);
            beanDefinition.setPrimary(true);
            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, redisDefinitionList);
            registry.registerBeanDefinition(RedisDefinitionContext.class.getName(), beanDefinition);
        }
    }

    private static List<RedisDefinitionProvider> appendDefaultProviderAndFilterDuplicateInstance(List<RedisDefinitionProvider> providerList) {
        if (null == providerList) {
            providerList = new ArrayList();
        }

        CommonUtil.appendList((List)providerList, ReflectUtil.scanAndInstanceByDefaultConstructor(RedisDefinitionProvider.class, RedisDefinitionProvider.class.getPackage().getName()));
        return (List)providerList;
    }

    private static List<RedisRegister> appendDefaultRegisterAndFilterDuplicateInstance(List<RedisRegister> registerList) {
        if (registerList == null) {
            registerList = new ArrayList();
        }

        CommonUtil.appendList((List)registerList, ReflectUtil.scanAndInstanceByDefaultConstructor(RedisRegister.class, RedisRegister.class.getPackage().getName()));
        return CommonUtil.filterDuplicateElement((List)registerList);
    }

    private static void applyRedisDefinitionDefaultConfig(RedisDefinition redisDefinition, List<RedisDefinitionProvider> providerList) {
        if (null != redisDefinition) {
            if (null != providerList && !providerList.isEmpty()) {
                Iterator var2 = providerList.iterator();

                while(var2.hasNext()) {
                    RedisDefinitionProvider provider = (RedisDefinitionProvider)var2.next();
                    if (provider.checkAndApplyDefaultConfig(redisDefinition)) {
                        break;
                    }
                }

            }
        }
    }

    private static RedisRegister lookupRegister(List<RedisRegister> registerList, RedisDefinition definition) {
        Iterator var2 = registerList.iterator();

        RedisRegister register;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            register = (RedisRegister)var2.next();
        } while(!register.canHandle(definition));

        return register;
    }

    private static List<RedisDefinition> checkRedisDefList(List<RedisDefinition> redisDefinitionList) {
        if (null != redisDefinitionList && !redisDefinitionList.isEmpty()) {
            RedisDefinition redisDefinition;
            for(Iterator var1 = redisDefinitionList.iterator(); var1.hasNext(); redisDefMap.put(redisDefinition.getId(), redisDefinition)) {
                redisDefinition = (RedisDefinition)var1.next();
                Assert.isTrue(!redisDefMap.containsKey(redisDefinition.getId()),"RedisDefinition[id=" + redisDefinition.getId() + "]重复定义！");
                if (redisDefinition.isPrimary()) {
                    if (null == primaryRedisDef) {
                        primaryRedisDef = redisDefinition;
                    } else {
                        Assert.isTrue(primaryRedisDef.getId().equals(redisDefinition.getId()), "不能定义多个 primary RedisDefinition 目前定义了[" + primaryRedisDef.getId() + "," + redisDefinition.getId() + "]");
                    }
                }
            }

            return redisDefinitionList;
        } else {
            return new ArrayList(0);
        }
    }
}
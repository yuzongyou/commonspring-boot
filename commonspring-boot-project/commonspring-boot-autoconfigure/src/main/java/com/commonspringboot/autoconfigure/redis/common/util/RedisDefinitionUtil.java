package com.commonspringboot.autoconfigure.redis.common.util;

import com.commonspringboot.autoconfigure.redis.common.model.RedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.model.RiseRedisDefinition;
import com.myspringboot.commonspringboot.CommonUtil;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RedisDefinitionUtil {
    private RedisDefinitionUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static List<RedisDefinition> filterExcludeRedisDefList(Set<String> excludeIds, List<RedisDefinition> redisDefinitionList) {
        if (excludeIds != null && !excludeIds.isEmpty() && null != redisDefinitionList && !redisDefinitionList.isEmpty()) {
            List<RedisDefinition> resultList = new ArrayList();
            Iterator var3 = redisDefinitionList.iterator();

            while (var3.hasNext()) {
                RedisDefinition redisDefinition = (RedisDefinition) var3.next();
                if (!isRedisDefInIdSet(excludeIds, redisDefinition)) {
                    resultList.add(redisDefinition);
                }
            }

            return resultList;
        } else {
            return redisDefinitionList;
        }
    }

    public static List<RedisDefinition> extractEnabledRedisDefList(Set<String> enabledIds, List<RedisDefinition> redisDefinitionList) {
        if (enabledIds != null && !enabledIds.isEmpty() && null != redisDefinitionList && !redisDefinitionList.isEmpty()) {
            List<RedisDefinition> resultList = new ArrayList();
            Iterator var3 = redisDefinitionList.iterator();

            while (var3.hasNext()) {
                RedisDefinition redisDefinition = (RedisDefinition) var3.next();
                if (isRedisDefInIdSet(enabledIds, redisDefinition)) {
                    resultList.add(redisDefinition);
                }
            }

            return resultList;
        } else {
            return redisDefinitionList;
        }
    }

    private static boolean isRedisDefInIdSet(Set<String> redisIds, RedisDefinition redisDefinition) {
        if (redisIds != null && !redisIds.isEmpty()) {
            if (redisIds.contains(redisDefinition.getId())) {
                return true;
            } else {
                Iterator var2 = redisIds.iterator();

                String enabledId;
                do {
                    if (!var2.hasNext()) {
                        return false;
                    }

                    enabledId = (String) var2.next();
                } while (!CommonUtil.isStartWildcardMatch(redisDefinition.getId(), enabledId));

                return true;
            }
        } else {
            return true;
        }
    }

    public static List<RedisDefinition> applyPrimaryJdbcDefList(String primaryId, List<RedisDefinition> redisDefinitionList) {
        if (null != redisDefinitionList && !redisDefinitionList.isEmpty()) {
            Iterator var2 = redisDefinitionList.iterator();

            while (var2.hasNext()) {
                RedisDefinition redisDefinition = (RedisDefinition) var2.next();
                redisDefinition.setPrimary(redisDefinition.getId().equals(primaryId));
            }

            return redisDefinitionList;
        } else {
            return redisDefinitionList;
        }
    }

    public static List<RedisDefinition> autoFillProperties(List<RedisDefinition> redisDefinitionList, Environment environment) {
        List<RedisDefinition> resultList = new ArrayList();
        Iterator var3 = redisDefinitionList.iterator();

        while (var3.hasNext()) {
            RedisDefinition jdbcDef = (RedisDefinition) var3.next();
            if (jdbcDef instanceof RiseRedisDefinition) {
                List<RedisDefinition> subList = autoFillProperties((RiseRedisDefinition) jdbcDef, environment);
                if (null != subList && !subList.isEmpty()) {
                    resultList.addAll(subList);
                }
            } else {
                resultList.add(jdbcDef);
            }
        }

        return resultList;
    }

    public static List<RedisDefinition> autoFillProperties(RiseRedisDefinition redisDef, Environment environment) {
        List<RedisDefinition> resultList = new ArrayList();
        String dsName = redisDef.getName();
        String host = lookupRiseDsEnvVar(environment, dsName, "host", "");
        String port = lookupRiseDsEnvVar(environment, dsName, "port", "");
        if (StringUtils.isEmpty(new CharSequence[]{host, port})) {
            return resultList;
        } else {
            String server = host + ":" + port;
            redisDef.setServer(server);
            resultList.add(redisDef);
            return resultList;

        }
    }

    private static String lookupRiseDsEnvVar(Environment environment, String dsName, String field, String defVal) {
        String key = dsName + "_" + field;
        String value = environment.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            value = System.getProperty(key);
        }

        if (StringUtils.isEmpty(value)) {
            value = System.getenv(key);
        }

        value = StringUtils.isEmpty(value) ? defVal : value;
        if (StringUtils.isEmpty(value)) {
            return value;
        } else {
            value = environment.resolvePlaceholders(value);
            return value;
        }
    }
}

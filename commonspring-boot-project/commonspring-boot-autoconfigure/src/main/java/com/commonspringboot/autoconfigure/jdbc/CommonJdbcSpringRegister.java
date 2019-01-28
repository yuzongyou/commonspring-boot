package com.commonspringboot.autoconfigure.jdbc;


import com.github.yuzongyou.jdbc.model.JdbcDefinition;
import com.github.yuzongyou.jdbc.model.RiseJdbcDefinition;
import com.github.yuzongyou.jdbc.provider.dbtype.DBProvider;
import com.github.yuzongyou.jdbc.provider.pooltype.PoolProvider;
import com.github.yuzongyou.jdbc.util.JdbcRegisterContext;
import com.github.yuzongyou.jdbc.util.JdbcRegisterUtil;
import com.myspringboot.commonspringboot.CommonUtil;
import com.myspringboot.commonspringboot.ReflectUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/12/28 14:04
 */
public class CommonJdbcSpringRegister {

    private CommonJdbcSpringRegister() {
        throw new IllegalStateException("Utility class");
    }

    public static void registerJdbcBeans(CommonJdbcProperties commonJdbcProperties, BeanDefinitionRegistry registry, Environment environment) {
        List<DBProvider> dbProviderList = ReflectUtil.newInstancesByDefaultConstructor(DBProvider.class, commonJdbcProperties.getDbProviderClasses());
        List<PoolProvider> poolProviderList = ReflectUtil.newInstancesByDefaultConstructor(PoolProvider.class, commonJdbcProperties.getPoolProviderClasses());
        List<JdbcDefinition> jdbcDefinitionList = lookupJdbcDefList(commonJdbcProperties);

        // 注册Bean
        JdbcRegisterUtil.registerJdbcBeanDefinitions(
                new JdbcRegisterContext()
                        .setDbProviderList(dbProviderList)
                        .setPoolProviderList(poolProviderList)
                        .setPrimaryId(commonJdbcProperties.getPrimaryId())
                        .setEnabledIds(commonJdbcProperties.getEnabledIds())
                        .setExcludeIds(commonJdbcProperties.getExcludeIds())
                        .setJdbcDefinitionList(jdbcDefinitionList)
                        .setRegistry(registry)
                        .setEnvironment(environment));
    }

    private static List<JdbcDefinition> lookupJdbcDefList(CommonJdbcProperties commonJdbcProperties) {
        List<JdbcDefinition> resultList = new ArrayList<>();

        CommonUtil.appendList(resultList, lookupStandardJdbcDefList(commonJdbcProperties));
        CommonUtil.appendList(resultList, lookupRiseJdbcDefList(commonJdbcProperties));

        return resultList;

    }

    private static List<JdbcDefinition> lookupRiseJdbcDefList(CommonJdbcProperties commonJdbcProperties) {
        Map<String, RiseJdbcDefinition> riseMap = commonJdbcProperties.getRises();
        List<JdbcDefinition> resultList = new ArrayList<>();

        if (null == riseMap || riseMap.isEmpty()) {
            return resultList;
        }

        Map<String, String> aliasMap = commonJdbcProperties.getRiseAlias();

        for (Map.Entry<String, RiseJdbcDefinition> entry : riseMap.entrySet()) {
            RiseJdbcDefinition jdbcDef = entry.getValue();
            if (!StringUtils.isEmpty(jdbcDef.getId())) {
                jdbcDef.setId(entry.getKey());
            }

            String dsName = jdbcDef.getName();
            if (aliasMap != null) {
                String aliasDsName = aliasMap.get(dsName);
                if (!StringUtils.isEmpty(aliasDsName)) {
                    jdbcDef.setName(aliasDsName);
                }
            }

            resultList.add(jdbcDef);
        }

        return resultList;
    }

    private static List<JdbcDefinition> lookupStandardJdbcDefList(CommonJdbcProperties commonJdbcProperties) {

        Map<String, JdbcDefinition> standardMap = commonJdbcProperties.getStandards();
        List<JdbcDefinition> resultList = new ArrayList<>();

        if (null == standardMap || standardMap.isEmpty()) {
            return resultList;
        }

        for (Map.Entry<String, JdbcDefinition> entry : standardMap.entrySet()) {
            JdbcDefinition jdbcDefinition = entry.getValue();
            if (StringUtils.isEmpty(jdbcDefinition.getId())) {
                jdbcDefinition.setId(entry.getKey());
            }
            resultList.add(jdbcDefinition);
        }

        return resultList;

    }
}

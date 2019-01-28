package com.commonspringboot.autoconfigure.jdbc;


import com.github.yuzongyou.jdbc.model.JdbcDefinition;
import com.github.yuzongyou.jdbc.model.RiseJdbcDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

@ConfigurationProperties(prefix = "commonspring.jdbc")
public class CommonJdbcProperties {
    public static final String PROPERTIES_PREFIX = "commonspring.jdbc";

    /**
     * 配置启用的 JDBC IDS， 中间用英文逗号分隔，如果为空或为配置都会启用所有的 JDBC, 允许使用通配符 '*'
     **/
    private Set<String> enabledIds;

    /**
     * 配置不启用的 JDBC IDS， 这个优先级要比启用的高，中间用英文逗号分隔，允许使用通配符 '*'
     **/
    private Set<String> excludeIds;

    /**
     * 一个应用只能有一个 primary 的Jdbc 定义, 默认没有主Jdbc
     **/
    private String primaryId;

    /**
     * 升龙数据源别名MAP
     **/
    private Map<String, String> riseAlias;

    /**
     * 标准JDBC定义列表
     **/
    private Map<String, JdbcDefinition> standards;

    /**
     * 升龙JDBC定义列表
     **/
    private Map<String, RiseJdbcDefinition> rises;

    /**
     * 扩展的 DBProvider 接口实现类全路径
     **/
    private Set<String> dbProviderClasses;

    /**
     * 连接池提供者 PoolProvider 实现类全路径
     **/
    private Set<String> poolProviderClasses;

    public Set<String> getEnabledIds() {
        return enabledIds;
    }

    public void setEnabledIds(Set<String> enabledIds) {
        this.enabledIds = enabledIds;
    }

    public Set<String> getExcludeIds() {
        return excludeIds;
    }

    public void setExcludeIds(Set<String> excludeIds) {
        this.excludeIds = excludeIds;
    }

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public Map<String, String> getRiseAlias() {
        return riseAlias;
    }

    public void setRiseAlias(Map<String, String> riseAlias) {
        this.riseAlias = riseAlias;
    }

    public Map<String, JdbcDefinition> getStandards() {
        return standards;
    }

    public void setStandards(Map<String, JdbcDefinition> standards) {
        this.standards = standards;
    }

    public Map<String, RiseJdbcDefinition> getRises() {
        return rises;
    }

    public void setRises(Map<String, RiseJdbcDefinition> rises) {
        this.rises = rises;
    }

    public Set<String> getDbProviderClasses() {
        return dbProviderClasses;
    }

    public void setDbProviderClasses(Set<String> dbProviderClasses) {
        this.dbProviderClasses = dbProviderClasses;
    }

    public Set<String> getPoolProviderClasses() {
        return poolProviderClasses;
    }

    public void setPoolProviderClasses(Set<String> poolProviderClasses) {
        this.poolProviderClasses = poolProviderClasses;
    }

}

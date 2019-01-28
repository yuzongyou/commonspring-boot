package com.commonspringboot.autoconfigure.redis;


import com.commonspringboot.autoconfigure.redis.common.model.RiseRedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.model.SentinelRedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.model.StdRedisDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/13 19:41
 */
@ConfigurationProperties(prefix = CommonRedisProperties.PROPERTIES_PREFIX)
public class CommonRedisProperties {

    public static final String PROPERTIES_PREFIX = "commonspring.redis";
    /**
     * 配置启用的 Redis IDS， 中间用英文逗号分隔，如果为空或为配置都会启用所有的 Redis, 允许使用通配符 '*'
     **/
    private Set<String> enabledIds;

    /**
     * 配置禁用的 Redis IDS， 中间用英文逗号分隔，允许使用通配符 '*'
     **/
    private Set<String> excludeIds;

    /**
     * 一个应用只能有一个 primary 的Jdbc 定义, 默认没有主 Redis
     **/
    private String primaryId;

    /**
     * 缓存的redis id
     */
    private Set<String> cacheIds;

    /**
     * 一个应用只能有一个 primary 的Cache Redis 定义, 多个cache Redis时需指定
     **/
    private String primaryCacheId;

    /**
     * 全局缓存过期时间，小于等于0表示不过期，单位秒
     */
    private Long cacheExpiredTime;

    /**
     * 升龙数据源别名MAP
     **/
    private Map<String, String> riseAlias;

    /**
     * 标准 Redis 定义
     **/
    private Map<String, StdRedisDefinition> standards;

    /**
     * 升龙Redis定义
     **/
    private Map<String, RiseRedisDefinition> rises;

    /**
     * 哨兵默认redis定义
     **/
    private Map<String, SentinelRedisDefinition> sentinels;

    /**
     * Redis Bean 注册器类全路径
     **/
    private Set<String> registerClasses;

    /**
     * RedisDefinitionProvider 定义类全路径
     **/
    private Set<String> providerClasses;

    public String getPrimaryCacheId() {
        return primaryCacheId;
    }

    public void setPrimaryCacheId(String primaryCacheId) {
        this.primaryCacheId = primaryCacheId;
    }

    public Long getCacheExpiredTime() {
        return cacheExpiredTime;
    }

    public void setCacheExpiredTime(Long cacheExpiredTime) {
        this.cacheExpiredTime = cacheExpiredTime;
    }

    public Set<String> getCacheIds() {
        return cacheIds;
    }

    public void setCacheIds(Set<String> cacheIds) {
        this.cacheIds = cacheIds;
    }

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

    public Map<String, StdRedisDefinition> getStandards() {
        return standards;
    }

    public void setStandards(Map<String, StdRedisDefinition> standards) {
        this.standards = standards;
    }

    public Map<String, RiseRedisDefinition> getRises() {
        return rises;
    }

    public void setRises(Map<String, RiseRedisDefinition> rises) {
        this.rises = rises;
    }

    public Map<String, SentinelRedisDefinition> getSentinels() {
        return sentinels;
    }

    public void setSentinels(Map<String, SentinelRedisDefinition> sentinels) {
        this.sentinels = sentinels;
    }

    public Set<String> getRegisterClasses() {
        return registerClasses;
    }

    public void setRegisterClasses(Set<String> registerClasses) {
        this.registerClasses = registerClasses;
    }

    public Set<String> getProviderClasses() {
        return providerClasses;
    }

    public void setProviderClasses(Set<String> providerClasses) {
        this.providerClasses = providerClasses;
    }
}

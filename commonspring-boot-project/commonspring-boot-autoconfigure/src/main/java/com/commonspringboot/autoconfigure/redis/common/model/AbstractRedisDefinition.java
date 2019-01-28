package com.commonspringboot.autoconfigure.redis.common.model;

/**
 * @author yzy
 */
public abstract class AbstractRedisDefinition implements RedisDefinition {

    /**
     * 唯一标识符
     */
    protected String id;

    /**
     * 是否是主 Redis
     */
    protected boolean primary = false;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean isPrimary() {
        return primary;
    }

    @Override
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}

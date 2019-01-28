package com.commonspringboot.autoconfigure.redis.common.model;

/**
 * @author yzy
 */
public class RiseRedisDefinition extends StdRedisDefinition {

    /**
     * 数据源实例名称，对应升龙上的实例名称
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

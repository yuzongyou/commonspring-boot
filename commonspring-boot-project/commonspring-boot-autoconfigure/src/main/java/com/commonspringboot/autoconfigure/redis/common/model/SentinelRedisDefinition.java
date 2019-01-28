package com.commonspringboot.autoconfigure.redis.common.model;



import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 哨兵模式部署的 redis 定义
 *
 * @author yzy
 */
public class SentinelRedisDefinition extends AbstractRedisDefinition {

    /**
     * 服务器列表，host:port,host2:port2......
     */
    private String sentinels;

    /**
     * 要连接的主机名称
     */
    private String masterName;

    /**
     * 连接超时，单位是毫秒，默认是 3000
     */
    private String timeout = "3000";

    /**
     * 密码
     */
    private String password = null;

    /**
     * 选择的数据库，默认是 0
     */
    private String database = "0";

    /**
     * 连接池配置
     */
    private Map<String, String> poolConfig = new HashMap<>();

    /**
     * 哨兵列表
     */
    private Set<String> sentinelSet = new HashSet<>();

    public String getSentinels() {
        return sentinels;
    }

    public void setSentinels(String sentinels) {
        this.sentinels = sentinels;

        // 拆分
        String[] array = sentinels.split(",");
        for (String server : array) {
            Assert.isTrue(isValidSentinelServer(server), "Redis 哨兵格式错误：[" + server + "]");
            sentinelSet.add(server.trim());
        }
    }

    private boolean isValidSentinelServer(String server) {

        String[] array = server.split(":");

        if (array.length != 2) {
            return false;
        }

        String pureNumberRegex = "^[0-9]+$";
        if (!array[1].trim().matches(pureNumberRegex)) {
            return false;
        }

        return true;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Map<String, String> getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(Map<String, String> poolConfig) {
        this.poolConfig = poolConfig;
    }

    public Set<String> getSentinelSet() {
        return sentinelSet;
    }

    @Override
    public String toString() {
        return "SentinelRedisDefinition{" +
                "sentinels='" + sentinels + '\'' +
                ", masterName='" + masterName + '\'' +
                ", timeout='" + timeout + '\'' +
                ", password='" + password + '\'' +
                ", database='" + database + '\'' +
                ", poolConfig=" + poolConfig +
                ", sentinels=" + sentinels +
                '}';
    }
}

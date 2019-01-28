package com.commonspringboot.autoconfigure.redis.common.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单类型的Redis定义，最简单的形式，最低配置为 host， 端口默认 6379
 *
 * @author yzy
 */
public class StdRedisDefinition extends AbstractRedisDefinition {

    /**
     * 服务器 模式则为单个 host:port
     */
    private String server;

    /**
     * 主机ID
     */
    private String host;

    /**
     * 端口
     */
    private String port;

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

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;

        String[] array = this.server.split(":");
        if (array.length == 1) {
            this.host = array[0].trim();
            this.port = "6379";
        } else {
            this.host = array[0].trim();
            this.port = array[1].trim();
        }

    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    @Override
    public String toString() {
        return "StdRedisDefinition{" +
                "poolConfig=" + poolConfig +
                ", database='" + database + '\'' +
                ", password='" + password + '\'' +
                ", server='" + server + '\'' +
                ", timeout='" + timeout + '\'' +
                '}';
    }
}

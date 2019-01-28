package com.commonspringboot.autoconfigure.redis.common.provider.def;



import com.commonspringboot.autoconfigure.redis.common.model.RedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.model.StdRedisDefinition;
import com.commonspringboot.autoconfigure.redis.common.util.ConvertUtil;
import com.myspringboot.commonspringboot.CommonUtil;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yzy
 */
public class StdRedisDefinitionProvider implements RedisDefinitionProvider {
    @Override
    public boolean support(RedisDefinition redisDefinition) {
        return null != redisDefinition && redisDefinition instanceof StdRedisDefinition;
    }

    @Override
    public boolean checkAndApplyDefaultConfig(RedisDefinition redisDefinition) {
        if (!support(redisDefinition)) {
            return false;
        }

        StdRedisDefinition stdRedisDef = (StdRedisDefinition) redisDefinition;

        stdRedisDef.setDatabase(filterDefaultDatabase(stdRedisDef.getDatabase(), "0"));
        stdRedisDef.setPassword(filterDefaultPassword(stdRedisDef.getPassword(), null));
        stdRedisDef.setTimeout(filterDefaultTimeout(stdRedisDef.getTimeout(), "3000"));
        stdRedisDef.setPoolConfig(applyDefaultPoolConfig(stdRedisDef));

        Assert.isTrue(!StringUtils.isEmpty(stdRedisDef.getId()),"RedisDefinition.id 不能为空");
        Assert.isTrue(!StringUtils.isEmpty(stdRedisDef.getServer()), "[" + stdRedisDef.getId() + "]RedisDefinition.server不能为空");

        return true;
    }

    /**
     * 应用默认的连接池配置
     *
     * @param stdRedisDef RedisDefinition 定义
     * @return 返回设置了默认连接池之后的配置
     */
    private Map<String, String> applyDefaultPoolConfig(StdRedisDefinition stdRedisDef) {

        Map<String, String> poolConfig = stdRedisDef.getPoolConfig();
        if (null == poolConfig) {
            poolConfig = new HashMap<>();
            stdRedisDef.setPoolConfig(poolConfig);
        }

        poolConfig.put("maxTotal", ConvertUtil.toString(poolConfig.get("maxTotal"), ConvertUtil.toString(poolConfig.get("maxActive"), "128")));
        poolConfig.put("maxIdle", String.valueOf(ConvertUtil.toInteger(poolConfig.get("maxIdle"), 8)));

        // maxTotal: 最大连接数， 原默认是 8， 修改默认是128,控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted。
        // maxIdle：控制一个pool最多有多少个状态为idle(空闲)的jedis实例；默认是 8
        // maxWaitMillis：表示当borrow一个jedis实例时，最大的等待时间，-1 表示一直等待，默认是-1L如果超过等待时间，则直接抛JedisConnectionException；
        // testWhileIdle：如果为true(默认)，表示有一个idle object evitor线程对idle object进行扫描，如果validate失败，此object会被从pool中drop掉；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义；
        // timeBetweenEvictionRunsMillis：默认30秒，表示idle object evitor两次扫描之间要sleep的毫秒数；
        // numTestsPerEvictionRun：表示idle object evitor每次扫描的最多的对象数, 默认是-1，表示所有都要扫描；
        // minEvictableIdleTimeMillis：默认60秒，表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义；

        // 删除不识别的配置项
        poolConfig = CommonUtil.filterUnRecordedField(poolConfig, JedisPoolConfig.class);

        stdRedisDef.setPoolConfig(poolConfig);

        return poolConfig;
    }

    private String filterDefaultTimeout(String timeout, String defVal) {
        Integer timeoutInt = ConvertUtil.toInteger(timeout, null);
        if (timeoutInt == null) {
            return String.valueOf(ConvertUtil.toInteger(defVal, 3000));
        }

        return String.valueOf(timeoutInt);
    }

    private String filterDefaultPassword(String password, String defVal) {
        return StringUtils.isEmpty(password) ? defVal : password;
    }

    private String filterDefaultDatabase(String database, String defDb) {
        Integer db = ConvertUtil.toInteger(database, null);
        if (db == null) {
            return String.valueOf(ConvertUtil.toInteger(defDb, 0));
        }

        return String.valueOf(db);
    }
}

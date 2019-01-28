package com.commonspringboot.autoconfigure.redis.common;

import redis.clients.jedis.Pipeline;

/**
 * @author yzy
 */
public interface PipelineExecutor<T> {

    /**
     * 使用管道命令执行
     *
     * @param pipeline 管道命令
     * @return 返回指定结果
     */
    T execute(Pipeline pipeline);
}

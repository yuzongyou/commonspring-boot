package com.myspringboot.commonspringboot;

import org.springframework.core.env.StandardEnvironment;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/1 19:07
 */
public interface EnvReader {
    /**
     * 读取当前运行环境
     *
     * @param environment 环境
     * @param sourceClass 启动class
     * @return 返回当前运行环境，默认是开发环境
     */
    String readRuntimeEnv(StandardEnvironment environment, Class<?> sourceClass);
}

package com.myspringboot.commonspringboot;

import org.springframework.core.env.StandardEnvironment;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/1 19:23
 */
public class DefaultEnvReader implements EnvReader {
    @Override
    public String readRuntimeEnv(StandardEnvironment environment, Class<?> sourceClass) {
        return AppContext.lookupFirstNotBlankValue(environment, new String[]{"DWENV", "ENV"}, AppContext.ENV_DEV);
    }
}

package com.myspringboot.commonspringboot;

import org.springframework.core.env.StandardEnvironment;

/**
 * @author yzy
 * @version 1.0
 * @since 2019/01/03 16:05
 */
public class DefaultProjectNoReader implements ProjectNoReader {

    @Override
    public String readProjectNo(StandardEnvironment environment, Class<?> sourceClass) {
        return AppContext.lookupFirstNotBlankValue(environment, new String[]{"DWPROJECTNO", "PROJECTNO", "APPNO", "DWAPPNO"}, null);
    }
}

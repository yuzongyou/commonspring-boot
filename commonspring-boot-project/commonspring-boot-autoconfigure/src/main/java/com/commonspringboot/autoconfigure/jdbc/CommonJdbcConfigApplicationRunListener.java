package com.commonspringboot.autoconfigure.jdbc;


import com.myspringboot.commonspringboot.SpringApplicationRunListenerAdapter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/26 15:17
 */
public class CommonJdbcConfigApplicationRunListener extends SpringApplicationRunListenerAdapter {

    private boolean needAutoConfigurer = false;

    public CommonJdbcConfigApplicationRunListener(SpringApplication application, String[] args) {
        super(application, args);
        this.needAutoConfigurer = isClassesImported(
                "org.springframework.jdbc.core.JdbcTemplate"
        );
    }

    @Override
    protected boolean needAutoConfigurer() {
        return needAutoConfigurer;
    }

    @Override
    protected void doContextPrepared(ConfigurableApplicationContext context, BeanDefinitionRegistry registry, ConfigurableEnvironment environment) {
        CommonJdbcProperties commonJdbcProperties = bindProperties(CommonJdbcProperties.PROPERTIES_PREFIX, CommonJdbcProperties.class);

        if (null != commonJdbcProperties) {
            CommonJdbcSpringRegister.registerJdbcBeans(commonJdbcProperties, registry, environment);
        }
    }
}

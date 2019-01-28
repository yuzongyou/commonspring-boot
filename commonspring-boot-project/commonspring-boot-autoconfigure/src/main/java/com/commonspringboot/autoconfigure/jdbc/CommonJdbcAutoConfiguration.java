package com.commonspringboot.autoconfigure.jdbc;


import com.github.yuzongyou.jdbc.Jdbc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/13 12:50
 */
@Configuration
@ConditionalOnClass({Jdbc.class})
@EnableConfigurationProperties(CommonJdbcProperties.class)
public class CommonJdbcAutoConfiguration {

    private final CommonJdbcProperties commonJdbcProperties;

    CommonJdbcAutoConfiguration(CommonJdbcProperties commonJdbcProperties) {
        this.commonJdbcProperties = commonJdbcProperties;

    }

    @Autowired
    public void buildJdbcTemplates() {

    }

}

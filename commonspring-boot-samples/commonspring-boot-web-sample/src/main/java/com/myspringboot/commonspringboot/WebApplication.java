package com.myspringboot.commonspringboot;


import com.myspringboot.commonspringboot.annotations.CommonSpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/10 8:59
 */
@SpringBootApplication
@CommonSpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}

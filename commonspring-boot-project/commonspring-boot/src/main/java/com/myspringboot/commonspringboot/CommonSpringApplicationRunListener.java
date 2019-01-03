package com.myspringboot.commonspringboot;


import com.myspringboot.commonspringboot.annotations.CommonSpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/1 17:36
 */
public class CommonSpringApplicationRunListener extends SpringApplicationRunListenerAdapter {

    public CommonSpringApplicationRunListener(SpringApplication application, String[] args) {
        super(application, args);

        if (isFirstInit("constructor")) {
            Set<Object> sources = getApplication().getAllSources();
            Class<?> sourceClass = validateSourcesThenReturnFirstHasCommonSpringApplicationAnnotationSource(sources);
            // 初始化
            AppContext.initialize(sourceClass);
        }
    }

    @Override
    protected boolean needAutoConfigurer() {
        return true;
    }

    private void initialize() {
        Set<Object> sources = getApplication().getAllSources();
        Class<?> sourceClass = validateSourcesThenReturnFirstHasCommonSpringApplicationAnnotationSource(sources);

        CommonSpringBootApplication applicationAnn = sourceClass.getAnnotation(CommonSpringBootApplication.class);
        boolean tryEnabledDefaultBeanNameGenerator = true;
        if (null != applicationAnn) {
            tryEnabledDefaultBeanNameGenerator = applicationAnn.tryEnabledDefaultBeanNameGenerator();
        }
        if (tryEnabledDefaultBeanNameGenerator) {
            // 如果用户没有自定义 BeanNameGenerator 则默认给一个
            Object beanNameGenerator = ReflectUtil.getFieldValue(getApplication(), "beanNameGenerator");
            if (null == beanNameGenerator) {
                getApplication().setBeanNameGenerator(new DefaultAnnotationBeanNameGenerator());
            }
        }
    }

    private static Class<?> validateSourcesThenReturnFirstHasCommonSpringApplicationAnnotationSource(Set<Object> sources) {
        String message = "必须提供应用Source对象";
        if (null == sources || sources.isEmpty()) {
            throw new RuntimeException(message);
        }

        Class<?> firstNullSource = null;
        Class<?> firstHasCommonSpringApplicationAnnotationSource = null;
        for (Object sourceObj : sources) {
            if (null != sourceObj) {
                Class<?> sourceClass = getObjectClass(sourceObj);
                if (firstNullSource == null) {
                    firstNullSource = sourceClass;
                }
                CommonSpringBootApplication ann = sourceClass.getAnnotation(CommonSpringBootApplication.class);
                if (ann != null) {
                    Assert.isNull(firstHasCommonSpringApplicationAnnotationSource, "@CommonSpringBootApplication 注解只能在一个启动来源上设置！");
                    firstHasCommonSpringApplicationAnnotationSource = sourceClass;
                }
            }
        }
        if (null != firstHasCommonSpringApplicationAnnotationSource) {
            return firstHasCommonSpringApplicationAnnotationSource;
        }
        if (null != firstNullSource) {
            return firstNullSource;
        }
        throw new RuntimeException(message);
    }

    private static Class<?> getObjectClass(Object object) {
        if (null == object) {
            return null;
        }
        if (object instanceof Class) {
            return (Class<?>) object;
        } else {
            return object.getClass();
        }
    }

    @Override
    public void doStarting() {
        initialize();
    }

    @Override
    public void doEnvironmentPrepared(ConfigurableEnvironment environment) {
        environment.getPropertySources().addLast(new MapPropertySource("projectInfo", AppContext.getProjectInfoMap()));
        environment.getPropertySources().addLast(new MapPropertySource("commonApplicationProperties", AppContext.getApplicationProperties()));
    }

    @Override
    public void doContextPrepared(ConfigurableApplicationContext context, BeanDefinitionRegistry registry, ConfigurableEnvironment environment) {
        AppContext.setAcx(context);
    }

    @Override
    public void doContextLoaded(ConfigurableApplicationContext context) {
        // DO nothing
    }

    @Override
    public void doStarted(ConfigurableApplicationContext context) {
        Logger appContextLogger = LoggerFactory.getLogger(AppContext.class);
        List<String> infoList = AppContext.getInitInfo();
        for (String info : infoList) {
            appContextLogger.info(info);
        }
    }

    @Override
    public void doRunning(ConfigurableApplicationContext context) {
        // DO nothing
    }

    @Override
    public void doFailed(ConfigurableApplicationContext context, Throwable exception) {
        // DO nothing
    }

    @Override
    public int order() {
        return -1 * DEFAULT_ORDER + COMMONCORE_DEFAULT_ORDER;
    }
}

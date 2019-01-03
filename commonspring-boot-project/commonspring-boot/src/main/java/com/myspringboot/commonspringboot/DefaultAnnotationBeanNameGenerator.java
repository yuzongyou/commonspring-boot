package com.myspringboot.commonspringboot;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 * Bean 名称生成，主要做几个事情：
 * 1. 对DaoCacheImpl 后缀Bean 设置为Primary Bean
 * 2. 对于 ServiceImpl 和 HandlerImpl 结尾的Bean，替换为 Service， Handler 结尾
 *
 * @author yzy
 */
public class DefaultAnnotationBeanNameGenerator extends AnnotationBeanNameGenerator {

    private static final String PRIMARY_DAO_CACHE_IMPL_SUFFIX = "DaoCacheImpl";
    private static final String SERVICE_IMPL_SUFFIX = "ServiceImpl";
    private static final String HANDLER_IMPL_SUFFIX = "HandlerImpl";

    @Override
    protected String buildDefaultBeanName(BeanDefinition definition) {
        String beanName = super.buildDefaultBeanName(definition);
        beanName = this.replaceBeanName(beanName);
        this.initPrimaryBean(definition);
        return beanName;
    }

    private String replaceBeanName(String beanName) {
        if (beanName.endsWith(SERVICE_IMPL_SUFFIX)) {
            beanName = beanName.replace(SERVICE_IMPL_SUFFIX, "Service");
        } else if (beanName.endsWith(HANDLER_IMPL_SUFFIX)) {
            beanName = beanName.replace(HANDLER_IMPL_SUFFIX, "Handler");
        }
        return beanName;
    }

    private void initPrimaryBean(BeanDefinition definition) {
        String className = definition.getBeanClassName();
        if (className.endsWith(PRIMARY_DAO_CACHE_IMPL_SUFFIX)) {
            definition.setPrimary(true);
        }
    }
}

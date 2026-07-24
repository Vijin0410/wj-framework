package com.wangjin.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Spring 工具：在非注入场景获取 Bean。
 */
@Component
public final class SpringUtils implements BeanFactoryPostProcessor {

    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringUtils.beanFactory = beanFactory;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) beanFactory.getBean(name);
    }

    public static <T> T getBean(Class<T> clz) {
        return beanFactory.getBean(clz);
    }

    public static boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.isSingleton(name);
    }

    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getType(name);
    }
}

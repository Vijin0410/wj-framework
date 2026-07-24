package com.wangjin.common.web.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

/**
 * 参数校验：快速失败。
 */
@Configuration
public class ValidationConfig {

    @Bean
    public Validator validator(AutowireCapableBeanFactory beanFactory) {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .failFast(true)
                .constraintValidatorFactory(new SpringConstraintValidatorFactory(beanFactory))
                .buildValidatorFactory();
        return factory.getValidator();
    }
}

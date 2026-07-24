package com.wangjin.common.mq.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * common-mq 自动装配。
 */
@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
@Import(RabbitConfig.class)
public class MqAutoConfiguration {
}

package com.jamaa_bank.service_recharge_retrait.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ACCOUNT_EXCHANGE = "AccountExchange";
    public static final String RECHARGE_QUEUE = "recharge.queue";
    public static final String RECHARGE_QUEUE_FOR_TRANSACTION = "recharge.queue.transaction";
    public static final String RETRAIT_QUEUE = "retrait.queue";
    public static final String RETRAIT_QUEUE_FOR_TRANSACTION = "retrait.queue.transaction";
    public static final String NOTIFICATION_RECHARGE_ROUTING_KEY = "notification.recharge.done";
    public static final String NOTIFICATION_RETRAIT_ROUTING_KEY = "notification.retrait.done";
    public static final String TRANSACTION_RECHARGE_ROUTING_KEY = "transactions.recharge.done";
    public static final String TRANSACTION_RETRAIT_ROUTING_KEY = "transactions.retrait.done";

    @Bean
    public TopicExchange accountExchange() {
        return new TopicExchange(ACCOUNT_EXCHANGE);
    }

    @Bean
    public Queue rechargeQueue() {
        return new Queue(RECHARGE_QUEUE, true);
    }

    @Bean
    public Queue rechargeQueueForTransaction() {
        return new Queue(RECHARGE_QUEUE_FOR_TRANSACTION, true);
    }

    @Bean
    public Queue retraitQueue() {
        return new Queue(RETRAIT_QUEUE, true);
    }

    @Bean
    public Queue retraitQueueForTransaction() {
        return new Queue(RETRAIT_QUEUE_FOR_TRANSACTION, true);
    }

    @Bean
    public Binding rechargeBinding() {
        return BindingBuilder.bind(rechargeQueue())
                .to(accountExchange())
                .with(NOTIFICATION_RECHARGE_ROUTING_KEY);
    }

    @Bean
    public Binding rechargeBindingForTransaction() {
        return BindingBuilder.bind(rechargeQueueForTransaction())
                .to(accountExchange())
                .with(TRANSACTION_RECHARGE_ROUTING_KEY);
    }

    @Bean
    public Binding retraitBindingForTransaction() {
        return BindingBuilder.bind(retraitQueueForTransaction())
                .to(accountExchange())
                .with(TRANSACTION_RETRAIT_ROUTING_KEY);
    }

    @Bean
    public Binding retraitBinding() {
        return BindingBuilder.bind(retraitQueue())
                .to(accountExchange())
                .with(NOTIFICATION_RETRAIT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

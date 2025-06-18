package com.jamaa.service_notifications.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
    
    @Bean
    public TopicExchange AccountExchange(){
        return new TopicExchange("AccountExchange", true, false);
    }
    
    @Bean
    public Queue depositNotificationQueue() {
        return new Queue("deposit.notification.queue", true);
    }

    @Bean
    public Queue withdrawalNotificationQueue() {
        return new Queue("withdrawal.notification.queue", true);
    }

    @Bean
    public Queue transferDoneQueue(){
        return new Queue("notification.transfer.done", true);
    }

    @Bean
    public Queue rechargeNotificationQueue() {
        return new Queue("recharge.notification.queue", true);
    }

    @Bean
    public Queue authNotificationQueue() {
        return new Queue("auth.notification.queue", true);
    }

    @Bean
    public Queue bankNotificationQueue() {
        return new Queue("bank.notification.queue", true);
    }

    @Bean
    public Queue transactionNotificationQueue() {
        return new Queue("transaction.notification.queue", true);
    }
    @Bean
    public Queue accountNotificationQueue() {
        return new Queue("account.notification.queue", true);
    }
    @Bean
    public Queue suspiciousActivityQueue() {
        return new Queue("suspicious.activity.queue", true);
    }
    @Bean
    public Queue insufficientFundsQueue() {
        return new Queue("insufficient.funds.queue", true);
    }
    @Bean
    public Queue customerCreateQueueNotification() {
        return new Queue("customerCreateQueueNotification", true);
    }

    @Bean
    public Queue accountCreateQueue(){
        return new Queue("accountCreateQueue", true, false, false);
    }

} 
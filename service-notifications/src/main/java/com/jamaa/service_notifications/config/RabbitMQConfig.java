package com.jamaa.service_notifications.config;

import org.springframework.amqp.core.Queue;
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
    public Queue depositNotificationQueue() {
        return new Queue("deposit.notification.queue", true);
    }

    @Bean
    public Queue withdrawalNotificationQueue() {
        return new Queue("withdrawal.notification.queue", true);
    }

    @Bean
    public Queue transferNotificationQueue() {
        return new Queue("transfer.notification.queue", true);
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

} 
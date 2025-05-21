package com.jamaa_bank.service_transfert.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
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
public class RabbitConfig {
    
    
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
    public TopicExchange TransactionExchange(){
        return new TopicExchange("TransactionExchange", true, false);
    }

    @Bean
    public Queue transactionQueue(){
        return new Queue("transactionQueue", true, false, false);
    }

    @Bean
    public Queue notificationQueue(){
        return new Queue("notificationQueue", true, false, false);
    }

    @Bean
    public Queue transfertDoneQueue() {
        return new Queue("transfertDoneQueue", true, false, false);
    }

    @Bean
    public Binding binding(TopicExchange AccountExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue).to(AccountExchange).with("notification.transfer.done");
    }

    @Bean
    public Binding binding2(TopicExchange TransactionExchange, Queue transfertDoneQueue) {
        return BindingBuilder.bind(transfertDoneQueue).to(TransactionExchange).with("transactions.transfer.done");
    }

}
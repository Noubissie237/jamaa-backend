package com.jamaa_bank.service_transactions.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;


@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange TransactionExchange(){
        return new TopicExchange("TransactionExchange", true, false);
    }

    @Bean
    public Queue depotDoneQueue() {
        return new Queue("depotDoneQueue", true, false, false);
    }

    @Bean
    public Queue transfertDoneQueue() {
        return new Queue("transfertDoneQueue", true, false, false);
    }
    
    @Bean
    public Queue retraitDoneQueue() {
        return new Queue("retraitDoneQueue", true, false, false);
    }

    @Bean
    public Queue rechargeByAgenceDoneQueue() {
        return new Queue("rechargeByAgenceDoneQueue", true, false, false);
    }
}


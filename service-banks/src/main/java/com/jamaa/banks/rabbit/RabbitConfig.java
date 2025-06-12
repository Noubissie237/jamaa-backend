package com.jamaa.banks.rabbit;



import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;




@Configuration
public class RabbitConfig {

    public static final String BANK_EXCHANGE = "bank.exchange";
    public static final String BANK_INFO_QUEUE = "bank.info.queue";
    public static final String BANK_INFO_ROUTING_KEY = "bank.info";

    public static final String CARD_EXCHANGE = "CardExchange";
    public static final String CARD_INFO_QUEUE = "card.info.queue";
    public static final String CARD_INFO_ROUTING_KEY = "bank-subscription";
    

    @Bean
    public TopicExchange bankExchange() {
        return new TopicExchange(BANK_EXCHANGE);
    }

    @Bean
    public TopicExchange cardExchange() {
        return new TopicExchange(CARD_EXCHANGE);
    }

    @Bean
    public Queue bankInfoQueue() {
        return new Queue(BANK_INFO_QUEUE, true); 
    }

    @Bean
    public Queue cardInfoQueue() {
        return new Queue(CARD_INFO_QUEUE, true); 
    }

    @Bean
    public Binding binding(TopicExchange bankExchange, Queue bankInfoQueue) {
        return BindingBuilder.bind(bankInfoQueue).to(bankExchange).with(BANK_INFO_ROUTING_KEY);
    }

    @Bean
    public Binding binding1(TopicExchange cardExchange, Queue cardInfoQueue) {
        return BindingBuilder.bind(cardInfoQueue).to(cardExchange).with(CARD_INFO_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory); 
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
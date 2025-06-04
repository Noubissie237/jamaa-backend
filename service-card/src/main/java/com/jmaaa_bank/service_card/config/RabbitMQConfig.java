package com.jmaaa_bank.service_card.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableRabbit

public class RabbitMQConfig {

    // Exchanges
    public static final String CARD_EXCHANGE = "card.exchange";
    
    // Queues
    public static final String CARD_CREATED_QUEUE = "card.created.queue";
    public static final String CARD_UPDATED_QUEUE = "card.updated.queue";
    public static final String CARD_DELETED_QUEUE = "card.deleted.queue";
    public static final String CARD_ACTIVATED_QUEUE = "card.activated.queue";
    public static final String CARD_BLOCKED_QUEUE = "card.blocked.queue";
    
    // Routing Keys
    public static final String CARD_CREATED_KEY = "card.created";
    public static final String CARD_UPDATED_KEY = "card.updated";
    public static final String CARD_DELETED_KEY = "card.deleted";
    public static final String CARD_ACTIVATED_KEY = "card.activated";
    public static final String CARD_BLOCKED_KEY = "card.blocked";
    

    @Bean
    public TopicExchange cardExchange() {
        return new TopicExchange(CARD_EXCHANGE);
    }
    @Bean
    public Queue cardCreatedQueue() {
        return QueueBuilder.durable(CARD_CREATED_QUEUE).build();
    }
    
    @Bean
    public Queue cardUpdatedQueue() {
        return QueueBuilder.durable(CARD_UPDATED_QUEUE).build();
    }
    
    @Bean
    public Queue cardDeletedQueue() {
        return QueueBuilder.durable(CARD_DELETED_QUEUE).build();
    }
    
    @Bean
    public Queue cardActivatedQueue() {
        return QueueBuilder.durable(CARD_ACTIVATED_QUEUE).build();
    }
    
    @Bean
    public Queue cardBlockedQueue() {
        return QueueBuilder.durable(CARD_BLOCKED_QUEUE).build();
    }
    
    @Bean
    public Binding cardCreatedBinding() {
        return BindingBuilder.bind(cardCreatedQueue())
                .to(cardExchange())
                .with(CARD_CREATED_KEY);
    }
    
    @Bean
    public Binding cardUpdatedBinding() {
        return BindingBuilder.bind(cardUpdatedQueue())
                .to(cardExchange())
                .with(CARD_UPDATED_KEY);
    }
    
    @Bean
    public Binding cardDeletedBinding() {
        return BindingBuilder.bind(cardDeletedQueue())
                .to(cardExchange())
                .with(CARD_DELETED_KEY);
    }
    
    @Bean
    public Binding cardActivatedBinding() {
        return BindingBuilder.bind(cardActivatedQueue())
                .to(cardExchange())
                .with(CARD_ACTIVATED_KEY);
    }
    
    @Bean
    public Binding cardBlockedBinding() {
        return BindingBuilder.bind(cardBlockedQueue())
                .to(cardExchange())
                .with(CARD_BLOCKED_KEY);
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

}

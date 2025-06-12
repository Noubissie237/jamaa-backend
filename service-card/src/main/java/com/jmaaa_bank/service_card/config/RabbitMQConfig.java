package com.jmaaa_bank.service_card.config;

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
public class RabbitMQConfig {
    public static final String CARD_EXCHANGE = "CardExchange";
    public static final String CARD_CREATED_QUEUE = "card.created.notification";
    public static final String CARD_UPDATED_QUEUE = "card.updated.notification";
    public static final String CARD_DELETED_QUEUE = "card.deleted.notification";
    public static final String CARD_ACTIVATED_QUEUE = "card.activated.notification";
    public static final String CARD_BLOCKED_QUEUE = "card.blocked.notification";
    public static final String CARD_INFO_QUEUE = "card.info.queue";



    public static final String CARD_CREATED_KEY = "card.created";
    public static final String CARD_UPDATED_KEY = "card.updated";
    public static final String CARD_DELETED_KEY = "card.deleted";
    public static final String CARD_ACTIVATED_KEY = "card.activated";
    public static final String CARD_BLOCKED_KEY = "card.blocked";

    @Bean
    public TopicExchange cardExchange() {
        return new TopicExchange(CARD_EXCHANGE, true, false);
    }

    @Bean
    public Queue cardCreatedQueue() {
        return new Queue(CARD_CREATED_QUEUE, true);
    }
    @Bean
    public Queue cardUpdatedQueue() {
        return new Queue(CARD_UPDATED_QUEUE, true);
    }
    @Bean
    public Queue cardDeletedQueue() {
        return new Queue(CARD_DELETED_QUEUE, true);
    }
    @Bean
    public Queue cardActivatedQueue() {
        return new Queue(CARD_ACTIVATED_QUEUE, true);
    }
    @Bean
    public Queue cardBlockedQueue() {
        return new Queue(CARD_BLOCKED_QUEUE, true);

    }
    @Bean
    public Queue cardInfoQueue() {
        return new Queue(CARD_INFO_QUEUE, true);

    }

    @Bean
    public Binding cardCreatedBinding() {
        return BindingBuilder.bind(cardCreatedQueue()).to(cardExchange()).with(CARD_CREATED_KEY);
    }
    @Bean
    public Binding cardUpdatedBinding() {
        return BindingBuilder.bind(cardUpdatedQueue()).to(cardExchange()).with(CARD_UPDATED_KEY);
    }
    @Bean
    public Binding cardDeletedBinding() {
        return BindingBuilder.bind(cardDeletedQueue()).to(cardExchange()).with(CARD_DELETED_KEY);
    }
    @Bean
    public Binding cardActivatedBinding() {
        return BindingBuilder.bind(cardActivatedQueue()).to(cardExchange()).with(CARD_ACTIVATED_KEY);
    }
    @Bean
    public Binding cardBlockedBinding() {
        return BindingBuilder.bind(cardBlockedQueue()).to(cardExchange()).with(CARD_BLOCKED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

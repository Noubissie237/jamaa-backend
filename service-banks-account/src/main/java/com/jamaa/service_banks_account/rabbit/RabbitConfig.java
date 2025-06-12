
package com.jamaa.service_banks_account.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;


@Configuration
public class RabbitConfig {

    public static final String BANK_INFO_QUEUE = "bank.info.queue";
    
    public static final String BANK_EXCHANGE = "bank.exchange";
    
    public static final String BANK_INFO_ROUTING_KEY = "bank.info";
    

    @Bean
    public Queue bankInfoQueue() {
        return new Queue(BANK_INFO_QUEUE, true);
    }

   
    @Bean
    public TopicExchange bankExchange() {
        return new TopicExchange(BANK_EXCHANGE);
    }

    
    @Bean
    public Binding bankInfoBinding(Queue bankInfoQueue, TopicExchange bankExchange) {
        return BindingBuilder.bind(bankInfoQueue).to(bankExchange).with(BANK_INFO_ROUTING_KEY);
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

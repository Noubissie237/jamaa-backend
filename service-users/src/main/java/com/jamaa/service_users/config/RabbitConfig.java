package com.jamaa.service_users.config;

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
    public TopicExchange CustomerExchange(){
        return new TopicExchange("CustomerExchange", true, false);
    }

    @Bean
    public TopicExchange AdminExchange(){
        return new TopicExchange("AdminExchange", true, false);
    }

    @Bean
    public Queue customerCreateQueueAdmin(){
        return new Queue("customerCreateQueueAdmin", true, false, false);
    }

    @Bean
    public Queue customerCreateQueueAccount(){
        return new Queue("customerCreateQueueAccount", true, false, false);
    }

    @Bean
    public Queue SuperAdminCreateQueue(){
        return new Queue("SuperAdminCreateQueue", true, false, false);
    }

    @Bean
    public Binding binding(TopicExchange CustomerExchange, Queue customerCreateQueueAdmin) {
        return BindingBuilder.bind(customerCreateQueueAdmin).to(CustomerExchange).with("customer.create.admin");
    }

    @Bean
    public Binding binding1(TopicExchange CustomerExchange, Queue customerCreateQueueAccount) {
        return BindingBuilder.bind(customerCreateQueueAccount).to(CustomerExchange).with("customer.create.account");
    }

    @Bean
    public Binding binding2(TopicExchange AdminExchange, Queue SuperAdminCreateQueue) {
        return BindingBuilder.bind(SuperAdminCreateQueue).to(AdminExchange).with("superadmin.create.notification");
    }

}

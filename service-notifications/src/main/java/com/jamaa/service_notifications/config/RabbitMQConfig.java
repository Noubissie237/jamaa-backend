package com.jamaa.service_notifications.config;

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
    
    // Exchanges
    @Bean
    public TopicExchange accountExchange() {
        return new TopicExchange("AccountExchange", true, false);
    }
    // Card Exchange
    @Bean
    public TopicExchange cardExchange() {
        return new TopicExchange("CardExchange", true, false);
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

    // Account Queues
    @Bean
    public Binding bindingDepositNotification(TopicExchange accountExchange, Queue depositNotificationQueue) {
        return BindingBuilder.bind(depositNotificationQueue).to(accountExchange).with("deposit.notification");
    }
    
    @Bean
    public Binding bindingWithdrawalNotification(TopicExchange accountExchange, Queue withdrawalNotificationQueue) {
        return BindingBuilder.bind(withdrawalNotificationQueue).to(accountExchange).with("withdrawal.notification");
    }
    
    @Bean
    public Binding bindingTransferDone(TopicExchange accountExchange, Queue transferDoneQueue) {
        return BindingBuilder.bind(transferDoneQueue).to(accountExchange).with("transfer.done");
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
    
    // Recharge/Retrait Queues
    @Bean
    public Queue rechargeDoneQueue() {
        return new Queue("notification.recharge.done", true);
    }
    
    @Bean
    public Queue retraitDoneQueue() {
        return new Queue("notification.retrait.done", true);
    }
    @Bean
    public Queue insufficientFundsQueue() {
        return new Queue("insufficient.funds.queue", true);
    }
    
    // Card Queues
    @Bean
    public Queue cardCreatedQueue() {
        return new Queue("card.created.notification", true);
    }
    
    @Bean
    public Queue cardUpdatedQueue() {
        return new Queue("card.updated.notification", true);
    }
    
    @Bean
    public Queue cardDeletedQueue() {
        return new Queue("card.deleted.notification", true);
    }
    
    @Bean
    public Queue cardActivatedQueue() {
        return new Queue("card.activated.notification", true);
    }
    
    @Bean
    public Queue cardBlockedQueue() {
        return new Queue("card.blocked.notification", true);
    }
    
    @Bean
    public Queue cardErrorQueue() {
        return new Queue("card.error.notification", true);
    }
    
    // Card Bindings
    @Bean
    public Binding bindingCardCreated(TopicExchange cardExchange, Queue cardCreatedQueue) {
        return BindingBuilder.bind(cardCreatedQueue).to(cardExchange).with("card.created");
    }
    
    @Bean
    public Binding bindingCardUpdated(TopicExchange cardExchange, Queue cardUpdatedQueue) {
        return BindingBuilder.bind(cardUpdatedQueue).to(cardExchange).with("card.updated");
    }
    
    @Bean
    public Binding bindingCardDeleted(TopicExchange cardExchange, Queue cardDeletedQueue) {
        return BindingBuilder.bind(cardDeletedQueue).to(cardExchange).with("card.deleted");
    }
    
    @Bean
    public Binding bindingCardActivated(TopicExchange cardExchange, Queue cardActivatedQueue) {
        return BindingBuilder.bind(cardActivatedQueue).to(cardExchange).with("card.activated");
    }
    
    @Bean
    public Binding bindingCardBlocked(TopicExchange cardExchange, Queue cardBlockedQueue) {
        return BindingBuilder.bind(cardBlockedQueue).to(cardExchange).with("card.blocked");
    }
    
    @Bean
    public Binding bindingCardError(TopicExchange cardExchange, Queue cardErrorQueue) {
        return BindingBuilder.bind(cardErrorQueue).to(cardExchange).with("card.error.notification");
    }
    
    @Bean
    public Binding bindingRechargeDone(TopicExchange accountExchange, Queue rechargeDoneQueue) {
        return BindingBuilder.bind(rechargeDoneQueue).to(accountExchange).with("notification.recharge.done");
    }
    
    @Bean
    public Binding bindingRetraitDone(TopicExchange accountExchange, Queue retraitDoneQueue) {
        return BindingBuilder.bind(retraitDoneQueue).to(accountExchange).with("notification.retrait.done");
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
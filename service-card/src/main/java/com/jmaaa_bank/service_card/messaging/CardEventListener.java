package com.jmaaa_bank.service_card.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.jmaaa_bank.service_card.dto.CustomerDTO;
import com.jmaaa_bank.service_card.service.impl.CardServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CardEventListener {

    @Autowired
    CardServiceImpl cardServiceImpl;


    @RabbitListener(queues = "card.info.queue")
    public void handleTransactionRequest(CustomerDTO customer) {
        cardServiceImpl.createCard(customer);
    }
    
    // @RabbitListener(queues = "customer.updated.queue")
    // public void handleCustomerUpdated(CustomerUpdatedEvent event) {
    //     log.info("Mise à jour du client reçue: {}", event.getCustomerId());
        
    //     // Mettre à jour les informations des cartes si nécessaire
    //     // Par exemple, si le nom du client change
    // }
    
    // private void validateTransaction(TransactionRequest request) {
        // Logique de validation ici
        // Vérifier si la carte existe, est active, a suffisamment de crédit, etc.
    }
    
    // Classes pour les événements entrants
    // @lombok.Data
    // public static class TransactionRequest {
    //     private String cardNumber;
    //     private java.math.BigDecimal amount;
    //     private String merchantId;
    //     private String transactionType;
    // }
    
    // @lombok.Data
    // public static class CustomerUpdatedEvent {
    //     private Long customerId;
    //     private String firstName;
    //     private String lastName;
    //     private String email;
    // }
// }


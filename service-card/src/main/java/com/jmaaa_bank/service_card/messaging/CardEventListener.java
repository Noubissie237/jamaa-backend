package com.jmaaa_bank.service_card.messaging;

// import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CardEventListener {
    // @RabbitListener(queues = "transaction.request.queue")
    // public void handleTransactionRequest(TransactionRequest request) {
    //     log.info("Demande de transaction reçue pour la carte: {}", request.getCardNumber());
        
    //     // Ici, vous pourriez valider la carte, vérifier les limites, etc.
    //     // Pour l'exemple, on log simplement
        
    //     try {
    //         // Logique de validation de la transaction
    //         validateTransaction(request);
    //         log.info("Transaction validée pour la carte: {}", request.getCardNumber());
    //     } catch (Exception e) {
    //         log.error("Erreur lors de la validation de la transaction", e);
    //     }
    // }
    
    // @RabbitListener(queues = "customer.updated.queue")
    // public void handleCustomerUpdated(CustomerUpdatedEvent event) {
    //     log.info("Mise à jour du client reçue: {}", event.getCustomerId());
        
    //     // Mettre à jour les informations des cartes si nécessaire
    //     // Par exemple, si le nom du client change
    // }
    
    // private void validateTransaction(TransactionRequest request) {
    //     // Logique de validation ici
    //     // Vérifier si la carte existe, est active, a suffisamment de crédit, etc.
    // }
    
    // Classes pour les événements entrants
    @lombok.Data
    public static class TransactionRequest {
        private String cardNumber;
        private java.math.BigDecimal amount;
        private String merchantId;
        private String transactionType;
    }
    
    @lombok.Data
    public static class CustomerUpdatedEvent {
        private Long customerId;
        private String firstName;
        private String lastName;
        private String email;
    }
}


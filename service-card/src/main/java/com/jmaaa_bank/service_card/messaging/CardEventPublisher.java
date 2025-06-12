package com.jmaaa_bank.service_card.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.jmaaa_bank.service_card.config.RabbitMQConfig;
import com.jmaaa_bank.service_card.model.Card;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardEventPublisher {

     private final RabbitTemplate rabbitTemplate;
    
    public void publishCardCreated(Card card) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_CREATED_KEY,
                    createCardEvent(card, "CARD_CREATED")
            );
            log.info("Événement CARD_CREATED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_CREATED", e);
        }
    }
    
    public void publishCardUpdated(Card card) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_UPDATED_KEY,
                    createCardEvent(card, "CARD_UPDATED")
            );
            log.info("Événement CARD_UPDATED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_UPDATED", e);
        }
    }
    
    public void publishCardDeleted(Card card) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_DELETED_KEY,
                    createCardEvent(card, "CARD_DELETED")
            );
            log.info("Événement CARD_DELETED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_DELETED", e);
        }
    }
    
    public void publishCardActivated(Card card) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_ACTIVATED_KEY,
                    createCardEvent(card, "CARD_ACTIVATED")
            );
            log.info("Événement CARD_ACTIVATED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_ACTIVATED", e);
        }
    }
    
    public void publishCardBlocked(Card card) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_BLOCKED_KEY,
                    createCardEvent(card, "CARD_BLOCKED")
            );
            log.info("Événement CARD_BLOCKED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_BLOCKED", e);
        }
    }
    
    private CardEvent createCardEvent(Card card, String eventType) {
        return CardEvent.builder()
                .eventType(eventType)
                .cardId(card.getId())
                .cardNumber(card.getCardNumber())
                .customerId(card.getCustomerId())
                .status(card.getStatus().toString())
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
    
    // Classe interne pour l'événement
    @lombok.Data
    @lombok.Builder
    public static class CardEvent {
        private String eventType;
        private Long cardId;
        private String cardNumber;
        private Long customerId;
        private String status;
        private java.time.LocalDateTime timestamp;
    }
    
}

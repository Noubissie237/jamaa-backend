package com.jmaaa_bank.service_card.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.jmaaa_bank.service_card.config.RabbitMQConfig;
import com.jmaaa_bank.service_card.dto.CardCreateDTO;
import com.jmaaa_bank.service_card.model.Card;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardEventPublisher {

     private final RabbitTemplate rabbitTemplate;
    
    public void publishCardCreated(Card card, String customerEmail) {
        try {
            CardCreateDTO cardCreateDTO = new CardCreateDTO();
            cardCreateDTO.setEmail(customerEmail);
            cardCreateDTO.setName(card.getHolderName());
            cardCreateDTO.setCardNumber(card.getCardNumber());
            cardCreateDTO.setBankName(card.getBankName());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_CREATED_KEY,
                    cardCreateDTO
            );
            log.info("Événement CARD_CREATED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_CREATED", e);
        }
    }
    
    public void publishCardUpdated(Card card, String customerEmail) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_UPDATED_KEY,
                    createCardEvent(card, "CARD_UPDATED", customerEmail)
            );
            log.info("Événement CARD_UPDATED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_UPDATED", e);
        }
    }
    
    public void publishCardDeleted(Card card, String customerEmail) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_DELETED_KEY,
                    createCardEvent(card, "CARD_DELETED", customerEmail)
            );
            log.info("Événement CARD_DELETED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_DELETED", e);
        }
    }
    
    public void publishCardActivated(Card card, String customerEmail) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_ACTIVATED_KEY,
                    createCardEvent(card, "CARD_ACTIVATED", customerEmail)
            );
            log.info("Événement CARD_ACTIVATED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_ACTIVATED", e);
        }
    }
    
    public void publishCardBlocked(Card card, String customerEmail) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CARD_EXCHANGE,
                    RabbitMQConfig.CARD_BLOCKED_KEY,
                    createCardEvent(card, "CARD_BLOCKED", customerEmail)
            );
            log.info("Événement CARD_BLOCKED publié pour la carte: {}", card.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement CARD_BLOCKED", e);
        }
    }
    
    private CardEvent createCardEvent(Card card, String eventType, String customerEmail) {
        return CardEvent.builder()
                .eventType(eventType)
                .cardId(card.getId())
                .cardNumber(card.getCardNumber())
                .customerId(card.getCustomerId())
                .customerEmail(customerEmail)
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
        private String customerEmail;
        private String status;
        private java.time.LocalDateTime timestamp;
    }
    
}

package com.jmaaa_bank.service_card.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jmaaa_bank.service_card.exception.CardNotFoundException;
import com.jmaaa_bank.service_card.dto.CardCreateRequest;
import com.jmaaa_bank.service_card.dto.CardResponse;
import com.jmaaa_bank.service_card.dto.CardUpdateRequest;
import com.jmaaa_bank.service_card.enums.CardStatus;
import com.jmaaa_bank.service_card.model.Card;
import com.jmaaa_bank.service_card.repository.CardRepository;
import com.jmaaa_bank.service_card.service.CardService;
import com.jmaaa_bank.service_card.utils.CardNumberGenerator;
import com.jmaaa_bank.service_card.messaging.CardEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl  implements CardService{

    private final CardRepository cardRepository;
    private final CardEventPublisher eventPublisher;
    private final CardNumberGenerator cardNumberGenerator;
    
    @Override
    public CardResponse createCard(CardCreateRequest request) {
        log.info("Création d'une nouvelle carte pour le client: {}", request.getCustomerId());
        
        // Générer un numéro de carte unique
        String cardNumber = cardNumberGenerator.generateCardNumber(request.getCardType());
        
        // Générer CVV
        String cvv = cardNumberGenerator.generateCVV();
        
        // Générer date d'expiration (3 ans à partir de maintenant)
        String expiryDate = generateExpiryDate();
        
        Card card = Card.builder()
                .cardNumber(cardNumber)
                .holderName(request.getHolderName())
                .customerId(request.getCustomerId())
                .cardType(request.getCardType())
                .expiryDate(expiryDate)
                .cvv(cvv)
                .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : BigDecimal.valueOf(1000))
                .pin(request.getPin()) // Dans un vrai système, il faudrait hasher le PIN
                .build();
        
        Card savedCard = cardRepository.save(card);
        
        // Publier l'événement de création
        eventPublisher.publishCardCreated(savedCard);
        
        log.info("Carte créée avec succès: {}", savedCard.getId());
        return mapToResponse(savedCard);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Carte non trouvée avec l'ID: " + id));
        return mapToResponse(card);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CardResponse getCardByNumber(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Carte non trouvée avec le numéro: " + cardNumber));
        return mapToResponse(card);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getCardsByCustomerId(Long customerId) {
        List<Card> cards = cardRepository.findByCustomerId(customerId);
        return cards.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public CardResponse updateCard(Long id, CardUpdateRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Carte non trouvée avec l'ID: " + id));
        
        boolean updated = false;
        
        if (request.getStatus() != null && !request.getStatus().equals(card.getStatus())) {
            card.setStatus(request.getStatus());
            updated = true;
        }
        
        if (request.getCreditLimit() != null && !request.getCreditLimit().equals(card.getCreditLimit())) {
            card.setCreditLimit(request.getCreditLimit());
            updated = true;
        }
        
        if (request.getPin() != null) {
            card.setPin(request.getPin()); // Hasher dans un vrai système
            updated = true;
        }
        
        if (updated) {
            Card savedCard = cardRepository.save(card);
            eventPublisher.publishCardUpdated(savedCard);
            log.info("Carte mise à jour: {}", id);
            return mapToResponse(savedCard);
        }
        
        return mapToResponse(card);
    }
    
    @Override
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Carte non trouvée avec l'ID: " + id));
        
        cardRepository.delete(card);
        eventPublisher.publishCardDeleted(card);
        log.info("Carte supprimée: {}", id);
    }
    
    @Override
    public CardResponse activateCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Carte non trouvée avec l'ID: " + id));
        
        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        
        eventPublisher.publishCardActivated(savedCard);
        log.info("Carte activée: {}", id);
        
        return mapToResponse(savedCard);
    }
    
    @Override
    public CardResponse blockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Carte non trouvée avec l'ID: " + id));
        
        card.setStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        
        eventPublisher.publishCardBlocked(savedCard);
        log.info("Carte bloquée: {}", id);
        
        return mapToResponse(savedCard);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        return cards.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private CardResponse mapToResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .holderName(card.getHolderName())
                .customerId(card.getCustomerId())
                .cardType(card.getCardType())
                .status(card.getStatus())
                .expiryDate(card.getExpiryDate())
                .creditLimit(card.getCreditLimit())
                .currentBalance(card.getCurrentBalance())
                .isVirtual(card.getIsVirtual())
                .createdAt(card.getCreatedAt())
                .lastUsedAt(card.getLastUsedAt())
                .build();
    }
    
    private String generateExpiryDate() {
        LocalDateTime expiryDateTime = LocalDateTime.now().plusYears(3);
        return expiryDateTime.format(DateTimeFormatter.ofPattern("MM/yy"));
    } 
    
}

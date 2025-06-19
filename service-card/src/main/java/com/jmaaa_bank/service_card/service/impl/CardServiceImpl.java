package com.jmaaa_bank.service_card.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jmaaa_bank.service_card.exception.CardNotFoundException;
import com.jmaaa_bank.service_card.dto.CardResponse;
import com.jmaaa_bank.service_card.dto.CardUpdateRequest;
import com.jmaaa_bank.service_card.dto.CustomerDTO;
import com.jmaaa_bank.service_card.enums.CardStatus;
import com.jmaaa_bank.service_card.enums.CardType;
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
    public CardResponse createCard(CustomerDTO request) {
        log.info("Création d'une nouvelle carte pour le client: {}", request.getCustomerId());
        
        // Générer un numéro de carte unique
        String cardNumber = cardNumberGenerator.generateCardNumber(CardType.VISA);
        
        // Générer CVV
        String cvv = cardNumberGenerator.generateCVV();
        
        // Générer date d'expiration (3 ans à partir de maintenant)
        String expiryDate = generateExpiryDate();
        
        Card card = Card.builder()
                .cardNumber(cardNumber)
                .holderName(request.getHolderName())
                .customerId(request.getCustomerId())
                .cardType(CardType.VISA)
                .status(CardStatus.PENDING_ACTIVATION)
                .expiryDate(expiryDate)
                .cvv(cvv)
                .creditLimit(BigDecimal.valueOf(1000))
                .currentBalance(BigDecimal.ZERO)
                .isVirtual(true)
                .bankId(request.getBankId())
                .bankName(request.getBankName())
                .build();
        
        Card savedCard = cardRepository.save(card);
        log.info("Publication de l'événement de création de carte pour la carte: {}", savedCard.getId());
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
    public CardResponse incrementBalance(Long id, BigDecimal amount) {
        log.info("Début de l'incrémentation du solde pour la carte ID: {} avec montant: {}", id, amount);

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Carte non trouvée pour l'ID: {}", id);
                    return new CardNotFoundException("Carte non trouvée avec l'ID: " + id);
                });

        BigDecimal oldBalance = card.getCurrentBalance();
        BigDecimal newBalance = oldBalance.add(amount);

        card.setCurrentBalance(newBalance);
        Card savedCard = cardRepository.save(card);

        log.info("Solde mis à jour avec succès pour la carte ID: {} — Ancien solde: {}, Nouveau solde: {}", id, oldBalance, newBalance);
        return mapToResponse(savedCard);
    }

    @Override
    public CardResponse decrementBalance(Long id, BigDecimal amount) {
        log.info("Début de la décrémentation du solde pour la carte ID: {} avec montant: {}", id, amount);

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Carte non trouvée pour l'ID: {}", id);
                    return new CardNotFoundException("Carte non trouvée avec l'ID: " + id);
                });

        BigDecimal oldBalance = card.getCurrentBalance();
        BigDecimal newBalance = oldBalance.subtract(amount);

        card.setCurrentBalance(newBalance);
        Card savedCard = cardRepository.save(card);

        log.info("Solde mis à jour avec succès pour la carte ID: {} — Ancien solde: {}, Nouveau solde: {}", id, oldBalance, newBalance);
        return mapToResponse(savedCard);
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
        
        if (updated) {
            Card savedCard = cardRepository.save(card);
            log.info("Publication de l'événement de mise à jour de carte pour la carte: {}", savedCard.getId());
            eventPublisher.publishCardUpdated(savedCard);
            log.info("Carte mise à jour: {}", id);
            return mapToResponse(savedCard);
        } else {
            log.info("Aucune modification détectée pour la carte: {}. Aucun événement publié.", id);
        }
        return mapToResponse(card);
    }
    
    @Override
    public CardResponse deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Carte non trouvée avec l'ID: " + id));
        
        CardResponse response = mapToResponse(card);
        cardRepository.delete(card);
        log.info("Publication de l'événement de suppression de carte pour la carte: {}", card.getId());
        eventPublisher.publishCardDeleted(card);
        log.info("Carte supprimée: {}", id);
        return response;
    }
    
    @Override
    public CardResponse activateCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Carte non trouvée avec l'ID: " + id));
        
        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        log.info("Publication de l'événement d'activation de carte pour la carte: {}", savedCard.getId());
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
        log.info("Publication de l'événement de blocage de carte pour la carte: {}", savedCard.getId());
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
    
    @Override
    @Transactional(readOnly = true)
    public CardResponse getCardByBankId(Long bankId) {
        Card card = cardRepository.findByBankId(bankId)
                .orElseThrow(() -> new CardNotFoundException("Aucune carte trouvée pour la banque ID: " + bankId));
        return mapToResponse(card);
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
                .bankId(card.getBankId())
                .bankName(card.getBankName())
                .build();
    }
    
    private String generateExpiryDate() {
        LocalDateTime expiryDateTime = LocalDateTime.now().plusYears(3);
        return expiryDateTime.format(DateTimeFormatter.ofPattern("MM/yy"));
    } 
    
}

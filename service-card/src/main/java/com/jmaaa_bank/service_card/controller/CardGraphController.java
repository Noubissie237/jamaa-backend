package com.jmaaa_bank.service_card.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.jmaaa_bank.service_card.dto.CardResponse;
import com.jmaaa_bank.service_card.dto.CardUpdateRequest;
import com.jmaaa_bank.service_card.dto.CustomerDTO;
import com.jmaaa_bank.service_card.service.CardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CardGraphController {

     private final CardService cardService;
    
    // === QUERIES ===
    
    @QueryMapping
    public CardResponse card(@Argument Long id) {
        log.info("Requête GraphQL: récupération de la carte {}", id);
        return cardService.getCardById(id);
    }
    
    @QueryMapping
    public CardResponse cardByNumber(@Argument String cardNumber) {
        log.info("Requête GraphQL: récupération de la carte par numéro");
        return cardService.getCardByNumber(cardNumber);
    }
    
    @QueryMapping
    public List<CardResponse> cardsByCustomer(@Argument Long customerId) {
        log.info("Requête GraphQL: récupération des cartes du client {}", customerId);
        return cardService.getCardsByCustomerId(customerId);
    }
    
    @QueryMapping
    public List<CardResponse> allCards() {
        log.info("Requête GraphQL: récupération de toutes les cartes");
        return cardService.getAllCards();
    }
    
    @QueryMapping
    public CardResponse cardByBankId(@Argument Long bankId) {
        log.info("Requête GraphQL: récupération de la carte par bankId {}", bankId);
        return cardService.getCardById(bankId);
    }
    
    // === MUTATIONS ===
    
    @MutationMapping
    public CardResponse createCard(@Argument CustomerDTO input) {
        log.info("Mutation GraphQL: création d'une carte pour le client {}", input.getCustomerId());
        return cardService.createCard(input);
    }
    
    @MutationMapping
    public CardResponse updateCard(@Argument Long id, @Argument CardUpdateRequest input) {
        log.info("Mutation GraphQL: mise à jour de la carte {}", id);
        return cardService.updateCard(id, input);
    }
    
    @MutationMapping
    public CardResponse deleteCard(@Argument Long id) {
        log.info("Mutation GraphQL: suppression de la carte {}", id);
        return cardService.deleteCard(id);
    }
    
    @MutationMapping
    public CardResponse activateCard(@Argument Long id) {
        log.info("Mutation GraphQL: activation de la carte {}", id);
        return cardService.activateCard(id);
    }
    
    @MutationMapping
    public CardResponse blockCard(@Argument Long id) {
        log.info("Mutation GraphQL: blocage de la carte {}", id);
        return cardService.blockCard(id);
    }

    @MutationMapping
    public CardResponse decrementBalance(@Argument Long id, @Argument BigDecimal amount) {
        log.info("Mutation GraphQL: diminution du solde de la carte {}", id);
        return cardService.decrementBalance(id, amount);
    }

    @MutationMapping
    public CardResponse incrementBalance(@Argument Long id, @Argument BigDecimal amount) {
        log.info("Mutation GraphQL: augmentation du solde de la carte {}", id);
        return cardService.incrementBalance(id, amount);
    }
    
}

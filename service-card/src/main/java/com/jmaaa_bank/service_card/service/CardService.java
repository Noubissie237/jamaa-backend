package com.jmaaa_bank.service_card.service;

import java.util.List;

import com.jmaaa_bank.service_card.dto.CardCreateRequest;
import com.jmaaa_bank.service_card.dto.CardResponse;
import com.jmaaa_bank.service_card.dto.CardUpdateRequest;

public interface CardService {

CardResponse createCard(CardCreateRequest request);
    CardResponse getCardById(Long id);
    CardResponse getCardByNumber(String cardNumber);
    List<CardResponse> getCardsByCustomerId(Long customerId);
    CardResponse updateCard(Long id, CardUpdateRequest request);
    void deleteCard(Long id);
    CardResponse activateCard(Long id);
    CardResponse blockCard(Long id);
    List<CardResponse> getAllCards();
    
}

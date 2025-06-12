package com.jmaaa_bank.service_card.service;

import java.util.List;

import com.jmaaa_bank.service_card.dto.CardResponse;
import com.jmaaa_bank.service_card.dto.CardUpdateRequest;
import com.jmaaa_bank.service_card.dto.CustomerDTO;
import com.jmaaa_bank.service_card.model.Card;

public interface CardService {

    void createCard(CustomerDTO request);
    CardResponse getCardById(Long id);
    CardResponse getCardByNumber(String cardNumber);
    List<CardResponse> getCardsByCustomerId(Long customerId);
    CardResponse updateCard(Long id, CardUpdateRequest request);
    void deleteCard(Long id);
    CardResponse activateCard(Long id);
    CardResponse blockCard(Long id);
    List<CardResponse> getAllCards();
    
}

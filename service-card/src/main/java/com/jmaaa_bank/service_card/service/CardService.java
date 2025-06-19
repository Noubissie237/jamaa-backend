package com.jmaaa_bank.service_card.service;

import java.math.BigDecimal;
import java.util.List;

import com.jmaaa_bank.service_card.dto.CardResponse;
import com.jmaaa_bank.service_card.dto.CardUpdateRequest;
import com.jmaaa_bank.service_card.dto.CustomerDTO;

public interface CardService {

    CardResponse createCard(CustomerDTO request);
    CardResponse getCardById(Long id);
    CardResponse getCardByNumber(String cardNumber);
    CardResponse getCardByBankId(Long bankId);
    List<CardResponse> getCardsByCustomerId(Long customerId);
    CardResponse updateCard(Long id, CardUpdateRequest request);
    CardResponse deleteCard(Long id);
    CardResponse activateCard(Long id);
    CardResponse blockCard(Long id);
    List<CardResponse> getAllCards();
    CardResponse incrementBalance(Long id, BigDecimal amount);
    CardResponse decrementBalance(Long id, BigDecimal amount);
}

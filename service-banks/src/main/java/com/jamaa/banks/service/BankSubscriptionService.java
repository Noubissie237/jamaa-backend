package com.jamaa.banks.service;

import com.jamaa.banks.dto.BankSubscriptionDTO;
import com.jamaa.banks.dto.BankSubscriptionInputDTO;
import com.jamaa.banks.dto.CustomerDTO;
import com.jamaa.banks.entity.Bank;
import com.jamaa.banks.entity.BankSubscription;
import com.jamaa.banks.entity.SubscriptionStatus;
import com.jamaa.banks.exception.CustomException;
import com.jamaa.banks.repository.BankRepository;
import com.jamaa.banks.repository.BankSubscriptionRepository;
import com.jamaa.banks.utils.Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BankSubscriptionService {
    private final BankSubscriptionRepository subscriptionRepository;
    private final BankRepository bankRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Util util;

    @Transactional
public BankSubscriptionDTO subscribeToBank(BankSubscriptionInputDTO inputDTO) {
    log.info("Tentative de souscription à la banque {} pour l'utilisateur {}", 
             inputDTO.getBankId(), inputDTO.getUserId());

    Bank bank = bankRepository.findById(inputDTO.getBankId())
            .orElseThrow(() -> CustomException.notFound("Banque", inputDTO.getBankId()));

    if (!bank.getIsActive()) {
        throw CustomException.invalidOperation("La banque n'est pas active");
    }

    if (subscriptionRepository.existsByUserIdAndBankId(
            inputDTO.getUserId(), inputDTO.getBankId())) {
        throw CustomException.alreadyExists("Souscription", "utilisateur et banque", 
                inputDTO.getUserId() + " - " + inputDTO.getBankId());
    }

    BankSubscription subscription = new BankSubscription();
    subscription.setUserId(inputDTO.getUserId());
    subscription.setBank(bank);
    subscription.setStatus(SubscriptionStatus.ACTIVE); // Définir le statut ici
    // ou le laisser à null si c’est fait via @PrePersist

    BankSubscription saved = subscriptionRepository.save(subscription);

    CustomerDTO customerDTO = new CustomerDTO();
    customerDTO.setCustomerId(util.getCustomer(inputDTO.getUserId()).getCustomerId());
    customerDTO.setHolderName(util.getCustomer(inputDTO.getUserId()).getHolderName());
    customerDTO.setBankId(bank.getId());
    customerDTO.setBankName(bank.getName());

    rabbitTemplate.convertAndSend("CardExchange", "bank-subscription", customerDTO);

    return convertToDTO(saved);
}


    @Transactional
    public BankSubscriptionDTO updateSubscriptionStatus(Long id, SubscriptionStatus newStatus) {
        log.info("Mise à jour du statut de la souscription {} vers {}", id, newStatus);

        BankSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Souscription", id));

        if (subscription.getStatus() == newStatus) {
            throw CustomException.invalidOperation("Le statut est déjà " + newStatus);
        }

        subscription.setStatus(newStatus);
        BankSubscription updatedSubscription = subscriptionRepository.save(subscription);
        log.info("Statut de la souscription mis à jour avec succès, id: {}", id);

        return convertToDTO(updatedSubscription);
    }

    public BankSubscriptionDTO getSubscriptionById(Long id) {
        log.debug("Recherche de la souscription avec l'id: {}", id);

        return subscriptionRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> CustomException.notFound("Souscription", id));
    }

    public List<BankSubscriptionDTO> getSubscriptionsByUserId(Long userId) {
        log.debug("Récupération des souscriptions pour l'utilisateur: {}", userId);

        return subscriptionRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BankSubscriptionDTO> getSubscriptionsByBankId(Long bankId) {
        log.debug("Récupération des souscriptions pour la banque: {}", bankId);

        return subscriptionRepository.findByBankId(bankId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BankSubscriptionDTO> getActiveSubscriptionsByBankId(Long bankId) {
        log.debug("Récupération des souscriptions actives pour la banque {}", bankId);
        return subscriptionRepository.findByBankIdAndStatus(bankId, SubscriptionStatus.ACTIVE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BankSubscriptionDTO> getInactiveSubscriptionsByBankId(Long bankId) {
        log.debug("Récupération des souscriptions inactives pour la banque {}", bankId);
        return subscriptionRepository.findByBankIdAndStatus(bankId, SubscriptionStatus.INACTIVE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private BankSubscriptionDTO convertToDTO(BankSubscription subscription) {
        BankSubscriptionDTO dto = new BankSubscriptionDTO();
        dto.setId(subscription.getId());
        dto.setUserId(subscription.getUserId());
        dto.setBankId(subscription.getBank().getId());
        dto.setStatus(subscription.getStatus());
        dto.setCreatedAt(subscription.getCreatedAt());
        return dto;
    }
} 
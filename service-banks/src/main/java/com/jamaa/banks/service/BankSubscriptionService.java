package com.jamaa.banks.service;

import com.jamaa.banks.dto.BankSubscriptionDTO;
import com.jamaa.banks.entity.Bank;
import com.jamaa.banks.entity.BankSubscription;
import com.jamaa.banks.entity.SubscriptionStatus;
import com.jamaa.banks.exception.CustomException;
import com.jamaa.banks.repository.BankRepository;
import com.jamaa.banks.repository.BankSubscriptionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BankSubscriptionService {
    private final BankSubscriptionRepository subscriptionRepository;
    private final BankRepository bankRepository;

    @Transactional
    public BankSubscriptionDTO subscribeToBank(@Valid BankSubscriptionDTO subscriptionDTO) {
        log.info("Tentative de souscription à la banque {} pour l'utilisateur {}", 
                subscriptionDTO.getBankId(), subscriptionDTO.getUserId());

        Bank bank = bankRepository.findById(subscriptionDTO.getBankId())
                .orElseThrow(() -> CustomException.notFound("Banque", subscriptionDTO.getBankId()));

        if (!bank.getIsActive()) {
            throw CustomException.invalidOperation("La banque n'est pas active");
        }

        if (subscriptionRepository.existsByUserIdAndBankId(
                subscriptionDTO.getUserId(), subscriptionDTO.getBankId())) {
            throw CustomException.alreadyExists("Souscription", "utilisateur et banque", 
                    subscriptionDTO.getUserId() + " - " + subscriptionDTO.getBankId());
        }

        BankSubscription subscription = new BankSubscription();
        subscription.setUserId(subscriptionDTO.getUserId());
        subscription.setBank(bank);
        // Le statut ACTIVE est défini automatiquement dans @PrePersist

        BankSubscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Souscription créée avec succès, id: {}", savedSubscription.getId());

        return convertToDTO(savedSubscription);
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
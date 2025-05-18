package com.jamaa.banks.service;

import com.jamaa.banks.dto.BankSubscriptionStats;
import com.jamaa.banks.model.entities.Bank;
import com.jamaa.banks.model.entities.BankSubscription;
import com.jamaa.banks.model.enums.SubscriptionStatus;
import com.jamaa.banks.repository.BankRepository;
import com.jamaa.banks.repository.BankSubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service pour la gestion des souscriptions bancaires
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankSubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(BankSubscriptionService.class);

    private final BankRepository bankRepository;
    private final BankSubscriptionRepository subscriptionRepository;

    /**
     * Crée une nouvelle souscription pour un utilisateur à une banque.
     * Un utilisateur peut souscrire à plusieurs banques mais une seule fois par banque.
     */
    @Transactional
    public BankSubscription subscribeToBank(Long userId, Long bankId) {
        logger.info("Création d'une souscription - userId: {}, bankId: {}", userId, bankId);
        
        Bank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> {
                    logger.error("Banque non trouvée avec l'ID : {}", bankId);
                    return new EntityNotFoundException("Banque non trouvée");
                });

        if (!bank.isActive()) {
            logger.warn("Tentative de souscription à une banque inactive - bankId: {}", bankId);
            throw new IllegalStateException("Cette banque n'accepte pas de nouvelles souscriptions actuellement");
        }

        boolean hasExistingSubscription = subscriptionRepository.existsByUserIdAndBankIdAndStatusIn(
                userId, 
                bankId, 
                List.of(SubscriptionStatus.PENDING, SubscriptionStatus.ACTIVE, SubscriptionStatus.APPROVED)
        );

        if (hasExistingSubscription) {
            logger.warn("L'utilisateur {} a déjà une souscription active avec la banque {}", userId, bankId);
            throw new IllegalStateException("Vous avez déjà une souscription active ou en attente avec cette banque");
        }

        BankSubscription subscription = new BankSubscription();
        subscription.setUserId(userId);
        subscription.setBank(bank);
        subscription.setStatus(SubscriptionStatus.PENDING);

        subscription = subscriptionRepository.save(subscription);
        logger.info("Souscription créée avec succès - subscriptionId: {}", subscription.getId());
        
        return subscription;
    }

    public List<BankSubscription> getUserSubscriptions(Long userId) {
        logger.debug("Récupération des souscriptions pour l'utilisateur : {}", userId);
        return subscriptionRepository.findByUserId(userId);
    }

    public BankSubscription getSubscription(Long id) {
        logger.debug("Récupération de la souscription : {}", id);
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Souscription non trouvée"));
    }

    /**
     * Met à jour le statut d'une souscription
     */
    @Transactional
    public BankSubscription updateSubscriptionStatus(
            Long id,
            SubscriptionStatus newStatus,
            String rejectionReason
    ) {
        logger.info("Mise à jour du statut de la souscription {} vers {}", id, newStatus);
        
        BankSubscription subscription = getSubscription(id);
        subscription.setStatus(newStatus);
        
        if (newStatus == SubscriptionStatus.REJECTED && rejectionReason != null) {
            subscription.setRejectionReason(rejectionReason);
        }

        subscription = subscriptionRepository.save(subscription);
        logger.info("Statut de la souscription mis à jour avec succès");
        
        return subscription;
    }

    public BankSubscriptionStats getBankSubscriptionStats(Long bankId) {
        logger.debug("Récupération des statistiques pour la banque : {}", bankId);
        
        Bank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new EntityNotFoundException("Banque non trouvée"));

        List<BankSubscription> subscriptions = subscriptionRepository.findByBankId(bankId);
        
        long totalSubscriptions = subscriptions.size();
        long activeSubscriptions = subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .count();
        long pendingSubscriptions = subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.PENDING)
                .count();
        long rejectedSubscriptions = subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.REJECTED)
                .count();
        long closedSubscriptions = subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.CLOSED)
                .count();
        long approvedSubscriptions = subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.APPROVED)
                .count();

        double approvalRate = totalSubscriptions > 0 ? 
                (double) approvedSubscriptions / totalSubscriptions : 0.0;
        double activeRate = totalSubscriptions > 0 ? 
                (double) activeSubscriptions / totalSubscriptions : 0.0;

        return BankSubscriptionStats.builder()
                .bankId(bankId)
                .bankName(bank.getName())
                .totalSubscriptions((int) totalSubscriptions)
                .activeSubscriptions((int) activeSubscriptions)
                .pendingSubscriptions((int) pendingSubscriptions)
                .rejectedSubscriptions((int) rejectedSubscriptions)
                .closedSubscriptions((int) closedSubscriptions)
                .approvedSubscriptions((int) approvedSubscriptions)
                .approvalRate(approvalRate)
                .activeRate(activeRate)
                .build();
    }
} 
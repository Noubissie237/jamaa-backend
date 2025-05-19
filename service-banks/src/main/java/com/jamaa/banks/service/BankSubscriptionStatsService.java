package com.jamaa.banks.service;

import com.jamaa.banks.dto.BankSubscriptionStats;
import com.jamaa.banks.model.entities.Bank;
import com.jamaa.banks.model.enums.SubscriptionStatus;
import com.jamaa.banks.repository.BankRepository;
import com.jamaa.banks.repository.BankSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des statistiques de souscription bancaire
 */
@Service
@RequiredArgsConstructor
public class BankSubscriptionStatsService {

    private final BankRepository bankRepository;
    private final BankSubscriptionRepository subscriptionRepository;

    /**
     * Récupère les statistiques de souscription pour une banque spécifique
     *
     * @param bankId ID de la banque
     * @return DTO contenant les statistiques
     */
    @Transactional(readOnly = true)
    public BankSubscriptionStats getBankStats(Long bankId) {
        Bank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new IllegalArgumentException("Banque non trouvée: " + bankId));

        Map<SubscriptionStatus, Long> statsByStatus = subscriptionRepository.countByBankIdGroupByStatus(bankId);
        
        long total = statsByStatus.values().stream().mapToLong(Long::longValue).sum();
        long active = statsByStatus.getOrDefault(SubscriptionStatus.ACTIVE, 0L);
        long pending = statsByStatus.getOrDefault(SubscriptionStatus.PENDING, 0L);
        long rejected = statsByStatus.getOrDefault(SubscriptionStatus.REJECTED, 0L);
        long closed = statsByStatus.getOrDefault(SubscriptionStatus.CLOSED, 0L);
        long approved = active + statsByStatus.getOrDefault(SubscriptionStatus.APPROVED, 0L);

        List<BankSubscriptionStats.StatusCount> statusCounts = statsByStatus.entrySet().stream()
                .map(entry -> BankSubscriptionStats.StatusCount.builder()
                        .status(entry.getKey().name())
                        .label(entry.getKey().getLabel())
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());

        double approvalRate = total > 0 ? (double) approved / total : 0.0;
        double activeRate = total > 0 ? (double) active / total : 0.0;

        return BankSubscriptionStats.builder()
                .bankId(bankId)
                .bankName(bank.getName())
                .totalSubscriptions((int) total)
                .activeSubscriptions((int) active)
                .pendingSubscriptions((int) pending)
                .rejectedSubscriptions((int) rejected)
                .closedSubscriptions((int) closed)
                .approvedSubscriptions((int) approved)
                .subscriptionsByStatus(statusCounts)
                .approvalRate(approvalRate)
                .activeRate(activeRate)
                .build();
    }

    /**
     * Récupère les statistiques de souscription pour toutes les banques
     *
     * @return Liste des statistiques par banque
     */
    @Transactional(readOnly = true)
    public List<BankSubscriptionStats> getAllBanksStats() {
        return bankRepository.findAll().stream()
                .map(bank -> getBankStats(bank.getId()))
                .collect(Collectors.toList());
    }
} 
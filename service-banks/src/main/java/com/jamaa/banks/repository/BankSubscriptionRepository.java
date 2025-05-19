package com.jamaa.banks.repository;

import com.jamaa.banks.model.entities.BankSubscription;
import com.jamaa.banks.model.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository pour la gestion des souscriptions bancaires
 */
@Repository
public interface BankSubscriptionRepository extends JpaRepository<BankSubscription, Long> {
    
    /**
     * Trouve toutes les souscriptions d'un utilisateur
     */
    List<BankSubscription> findByUserId(Long userId);

    /**
     * Trouve toutes les souscriptions d'un utilisateur avec pagination
     */
    Page<BankSubscription> findByUserId(Long userId, Pageable pageable);

    /**
     * Trouve toutes les souscriptions pour une banque
     */
    List<BankSubscription> findByBankId(Long bankId);

    /**
     * Trouve toutes les souscriptions pour une banque avec pagination
     */
    Page<BankSubscription> findByBankId(Long bankId, Pageable pageable);

    /**
     * Trouve une souscription par son numéro
     */
    Optional<BankSubscription> findBySubscriptionNumber(String subscriptionNumber);

    /**
     * Vérifie si un utilisateur a déjà une souscription active pour une banque donnée
     */
    @Query("SELECT COUNT(s) > 0 FROM BankSubscription s " +
           "WHERE s.userId = :userId AND s.bank.id = :bankId " +
           "AND s.status IN :statuses")
    boolean existsByUserIdAndBankIdAndStatusIn(
            @Param("userId") Long userId,
            @Param("bankId") Long bankId,
            @Param("statuses") List<SubscriptionStatus> statuses
    );

    /**
     * Compte le nombre total de souscriptions actives pour une banque
     */
    @Query("SELECT COUNT(s) FROM BankSubscription s " +
           "WHERE s.bank.id = :bankId AND s.status = 'ACTIVE'")
    Long countActiveSubscriptionsByBankId(@Param("bankId") Long bankId);

    /**
     * Compte le nombre de souscriptions par statut pour une banque
     */
    @Query("SELECT s.status, COUNT(s) FROM BankSubscription s " +
           "WHERE s.bank.id = :bankId " +
           "GROUP BY s.status")
    List<Object[]> countSubscriptionsByStatusForBank(@Param("bankId") Long bankId);

    /**
     * Trouve la dernière souscription d'un utilisateur pour une banque
     */
    Optional<BankSubscription> findFirstByUserIdAndBankIdOrderByCreatedAtDesc(Long userId, Long bankId);

    /**
     * Trouve une souscription active pour un utilisateur et une banque
     */
    Optional<BankSubscription> findByUserIdAndBankIdAndStatus(Long userId, Long bankId, SubscriptionStatus status);

    /**
     * Compte le nombre total de souscriptions pour une banque
     */
    Long countByBankId(Long bankId);

    /**
     * Compte le nombre de souscriptions par statut pour une banque
     */
    @Query("SELECT bs.status as status, COUNT(bs) as count FROM BankSubscription bs " +
           "WHERE bs.bank.id = :bankId GROUP BY bs.status")
    List<Object[]> countByStatusForBank(@Param("bankId") Long bankId);

    /**
     * Compte le nombre de souscriptions par statut pour une banque avec filtres de date
     */
    @Query("SELECT bs.status as status, COUNT(bs) as count FROM BankSubscription bs " +
           "WHERE bs.bank.id = :bankId " +
           "AND (:fromDate IS NULL OR bs.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR bs.createdAt <= :toDate) " +
           "GROUP BY bs.status")
    List<Object[]> countByStatusForBankAndDateRange(
            @Param("bankId") Long bankId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    /**
     * Compte le nombre de souscriptions par statut pour une banque
     */
    default Map<SubscriptionStatus, Long> countByBankIdGroupByStatus(Long bankId) {
        List<Object[]> results = countByStatusForBank(bankId);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                    row -> (SubscriptionStatus) row[0],
                    row -> (Long) row[1]
                ));
    }

    /**
     * Liste toutes les souscriptions d'une banque avec un statut spécifique
     */
    List<BankSubscription> findByBankIdAndStatus(Long bankId, SubscriptionStatus status);
} 
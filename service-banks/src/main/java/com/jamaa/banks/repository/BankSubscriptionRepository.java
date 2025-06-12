package com.jamaa.banks.repository;

import com.jamaa.banks.entity.BankSubscription;
import com.jamaa.banks.entity.SubscriptionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BankSubscriptionRepository extends JpaRepository<BankSubscription, Long> {
    List<BankSubscription> findByBankId(Long bankId);
    List<BankSubscription> findByUserId(Long userId);
    List<BankSubscription> findByBankIdAndStatus(Long bankId, SubscriptionStatus status);
    boolean existsByUserIdAndBankId(Long userId, Long bankId);
} 
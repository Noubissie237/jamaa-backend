package com.jamaa.service_account.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jamaa.service_account.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUserId(Long userId);
}

package com.jamaa.service_banks_account.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.jamaa.service_banks_account.model.*;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    BankAccount findByBankId(Long bankId);
}
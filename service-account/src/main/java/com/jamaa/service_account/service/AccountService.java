package com.jamaa.service_account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jamaa.service_account.model.Account;
import com.jamaa.service_account.repository.AccountRepository;
import com.jamaa.service_account.utils.Util;

@Service
public class AccountService {
    
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    Util util;

    public Account createAccount(Long userId, String firstName) {
        Account account = new Account();

        account.setBalance(BigDecimal.ZERO);
        account.setCreatedAt(LocalDateTime.now());
        account.setUserId(userId);

        String tmpNumber;
        do {
            tmpNumber = "2025" + "-" + util.generateRandomCode();
        } while (accountRepository.findByAccountNumber(tmpNumber).isPresent());

        account.setAccountNumber(tmpNumber);
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}

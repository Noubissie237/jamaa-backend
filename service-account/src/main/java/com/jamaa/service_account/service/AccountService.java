package com.jamaa.service_account.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public Account createAccount(Long userId) throws IOException {

        if (util.userExist(userId)) {
            Account account = new Account();
    
            account.setBalance(BigDecimal.ZERO);
            account.setCreatedAt(LocalDateTime.now());
            account.setUserId(userId);
    
            String tmpNumber = "";
            int attempts = 0;
            final int MAX_ATTEMPTS = 10;
            do {
                tmpNumber = "2025" + "-" + util.generateRandomCode();
                attempts++;
                if (attempts > MAX_ATTEMPTS) {
                    throw new IllegalStateException("Unable to generate unique account number");
                }
            } while (accountRepository.findByAccountNumber(tmpNumber).isPresent());
    
    
            account.setAccountNumber(tmpNumber);
    
            return accountRepository.save(account);
            
        } else {
            throw new RuntimeException("Cet Id n'appartient à aucun utilisateur !");
        }


    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account incrementBalance(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable avec l'ID : " + accountId));

        BigDecimal newBalance = account.getBalance().add(amount).setScale(2, RoundingMode.HALF_UP);
        account.setBalance(newBalance);

        return accountRepository.save(account);
    }

    public Account decrementBalance(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable avec l'ID : " + accountId));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Solde insuffisant pour effectuer cette opération");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount).setScale(2, RoundingMode.HALF_UP);
        account.setBalance(newBalance);

        return accountRepository.save(account);
    }

    public boolean deleteAccount(Account account) {
        if (account == null || account.getId() == null) {
            System.out.println("Le compte à supprimer est invalide.");
            return false;
        }

        boolean exists = accountRepository.existsById(account.getId());
        if (!exists) {
            System.out.println("Le compte avec l'ID " + account.getId() + " n'existe pas.");
            return false;
        }

        accountRepository.delete(account);
        System.out.println("Compte supprimé avec succès : " + account.getId());
        return true;

    }

}

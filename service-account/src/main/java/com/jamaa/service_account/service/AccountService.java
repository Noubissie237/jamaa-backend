package com.jamaa.service_account.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jamaa.service_account.model.Account;
import com.jamaa.service_account.repository.AccountRepository;
import com.jamaa.service_account.utils.Util;
import com.jamaa.service_account.events.CustomerAccountEvent;
import com.jamaa.service_account.events.CustomerEvent;
import com.jamaa.service_account.exception.AccountNotFoundException;
import com.jamaa.service_account.exception.InsufficientBalanceException;
import com.jamaa.service_account.exception.UserNotFoundException;

@Service
@Transactional
public class AccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private static final int MAX_ATTEMPTS = 10;
    private static final String ACCOUNT_PREFIX = "2025-";
    
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Util util;

    public void createAccount(CustomerEvent customerEv) throws IOException {
        logger.info("Tentative de création d'un compte pour l'utilisateur ID: {}", customerEv.getId());

        if (!util.userExist(customerEv.getId())) {
            logger.error("Utilisateur non trouvé avec l'ID: {}", customerEv.getId());
            throw new UserNotFoundException("Utilisateur introuvable avec l'ID : " + customerEv.getId());
        }

        Optional<Account> existingAccount = accountRepository.findByUserId(customerEv.getId());
        if (existingAccount.isPresent()) {
            logger.warn("Un compte existe déjà pour l'utilisateur ID: {}. Retour de l'existant.", customerEv.getId());
        }

        Account account = new Account();
        account.setBalance(BigDecimal.ZERO);
        account.setCreatedAt(LocalDateTime.now());
        account.setUserId(customerEv.getId());

        logger.info("Instance Account créée avec succès pour l'utilisateur ID {}", customerEv.getId());

        String accountNumber = generateUniqueAccountNumber();
        logger.info("Numéro de compte {} généré avec succès", accountNumber);
        account.setAccountNumber(accountNumber);

        accountRepository.save(account);
        logger.info("Compte créé avec succès - Numéro de compte: {}, User ID: {}", accountNumber, customerEv.getId());

        CustomerAccountEvent event = new CustomerAccountEvent();
        event.setFirstName(customerEv.getFirstName());
        event.setLastName(customerEv.getLastName());
        event.setEmail(customerEv.getEmail());
        event.setAccountNumber(account.getAccountNumber());

        rabbitTemplate.convertAndSend("AccountExchange", "account.create", event);
    }


    private String generateUniqueAccountNumber() {
        logger.debug("Génération d'un numéro de compte unique");
        
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String candidateNumber = ACCOUNT_PREFIX + util.generateRandomCode();
            
            if (!accountRepository.findByAccountNumber(candidateNumber).isPresent()) {
                logger.debug("Numéro de compte unique généré: {} après {} tentative(s)", candidateNumber, attempt);
                return candidateNumber;
            }
            
            logger.debug("Numéro de compte {} déjà existant, tentative {}/{}", candidateNumber, attempt, MAX_ATTEMPTS);
        }
        
        logger.error("Impossible de générer un numéro de compte unique après {} tentatives", MAX_ATTEMPTS);
        throw new IllegalStateException("Impossible de générer un numéro de compte unique");
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        logger.debug("Récupération de tous les comptes");
        List<Account> accounts = accountRepository.findAll();
        logger.info("Nombre de comptes récupérés: {}", accounts.size());
        return accounts;
    }

    @Transactional(readOnly = true)
    public Optional<Account> getAccount(Long id) {
        logger.debug("Récupération du compte id {}", id);
        return accountRepository.findById(id);
    }

    public Account getAccountByUserId(Long userId) {
        logger.debug("Récupération du compte par utilisateur id {}", userId);
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Compte introuvable pour l'utilisateur ID: " + userId));
    }

    public Account incrementBalance(Long accountId, BigDecimal amount) {
        validateAmount(amount);
        logger.info("Incrémentation du solde - Compte ID: {}, Montant: {}", accountId, amount);
        
        Account account = findAccountById(accountId);
        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.add(amount).setScale(2, RoundingMode.HALF_UP);
        
        account.setBalance(newBalance);
        Account updatedAccount = accountRepository.save(account);
        
        logger.info("Solde incrémenté avec succès - Compte ID: {}, Ancien solde: {}, Nouveau solde: {}", 
                   accountId, oldBalance, newBalance);
        
        return updatedAccount;
    }

    public Account decrementBalance(Long accountId, BigDecimal amount) {
        validateAmount(amount);
        logger.info("Décrémentation du solde - Compte ID: {}, Montant: {}", accountId, amount);
        
        Account account = findAccountById(accountId);
        BigDecimal currentBalance = account.getBalance();
        
        if (currentBalance.compareTo(amount) < 0) {
            logger.error("Solde insuffisant - Compte ID: {}, Solde actuel: {}, Montant demandé: {}", 
                        accountId, currentBalance, amount);
            throw new InsufficientBalanceException("Solde insuffisant pour effectuer cette opération");
        }
        
        BigDecimal newBalance = currentBalance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        account.setBalance(newBalance);
        Account updatedAccount = accountRepository.save(account);
        
        logger.info("Solde décrémenté avec succès - Compte ID: {}, Ancien solde: {}, Nouveau solde: {}", 
                   accountId, currentBalance, newBalance);
        
        return updatedAccount;
    }

    public boolean deleteAccount(Account account) {
        if (account == null || account.getId() == null) {
            logger.warn("Tentative de suppression d'un compte invalide");
            return false;
        }

        Long accountId = account.getId();
        
        if (!accountRepository.existsById(accountId)) {
            logger.warn("Tentative de suppression d'un compte inexistant - ID: {}", accountId);
            return false;
        }

        logger.info("Suppression du compte - ID: {}, Numéro de compte: {}", accountId, account.getAccountNumber());
        accountRepository.delete(account);
        logger.info("Compte supprimé avec succès - ID: {}", accountId);
        
        return true;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Montant invalide: {}", amount);
            throw new IllegalArgumentException("Le montant doit être strictement positif");
        }
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    logger.error("Compte introuvable avec l'ID: {}", accountId);
                    return new AccountNotFoundException("Compte introuvable avec l'ID : " + accountId);
                });
    }
    
    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        validateAmount(amount);
        if (fromAccountId.equals(toAccountId)) {
            logger.error("Transfert impossible : comptes source et destination identiques");
            throw new IllegalArgumentException("Les comptes source et destination doivent être différents");
        }
        logger.info("Transfert de {} à {} du montant {}", fromAccountId, toAccountId, amount);
        Account fromAccount = findAccountById(fromAccountId);
        Account toAccount = findAccountById(toAccountId);
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            logger.error("Solde insuffisant pour le transfert - Compte source: {}, Solde: {}, Montant: {}", fromAccountId, fromAccount.getBalance(), amount);
            throw new InsufficientBalanceException("Solde insuffisant pour effectuer le transfert");
        }
        // Débit
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        // Crédit
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        logger.info("Transfert réussi de {} à {} du montant {}", fromAccountId, toAccountId, amount);
    }
}
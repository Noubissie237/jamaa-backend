package com.jamaa.service_banks_account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jamaa.service_banks_account.dto.BankInfoDTO;
import com.jamaa.service_banks_account.model.BankAccount;
import com.jamaa.service_banks_account.repository.BankAccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final Map<Long, BankInfoDTO> bankInfoCache = new ConcurrentHashMap<>();

    @Transactional
    public BankAccount createBankAccount(BankInfoDTO bankInfo) {
        if (bankInfo == null || bankInfo.getId() == null) {
            throw new IllegalArgumentException("Informations de la banque invalides");
        }
        if (bankAccountRepository.findByBankId(bankInfo.getId()) != null) {
            throw new IllegalArgumentException("Compte financier déjà existant pour cette banque");
        }
        bankInfoCache.put(bankInfo.getId(), bankInfo);
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBankId(bankInfo.getId());
        return bankAccountRepository.save(bankAccount);
    }

    @Transactional
    public BankAccount updateFinancialsFromTransaction(Long bankId, BigDecimal amount, String operationType) {
        BankAccount bankAccount = bankAccountRepository.findByBankId(bankId);
        if (bankAccount == null) {
            throw new IllegalArgumentException("Compte financier non trouvé pour cette banque");
        }
        BankInfoDTO bankInfo = bankInfoCache.get(bankId);
        if (bankInfo == null) {
            throw new IllegalStateException("Informations de la banque non disponibles");
        }
        switch (operationType) {
            case "DEPOSIT":
                bankAccount.setTotalBalance(bankAccount.getTotalBalance().add(amount));
                break;
            case "WITHDRAW":
                bankAccount.setTotalBalance(bankAccount.getTotalBalance().subtract(amount));
                bankAccount.setTotalWithdrawFees(bankAccount.getTotalWithdrawFees().add(bankInfo.getWithdrawFees()));
                break;
            case "INTERNAL_TRANSFER":
                bankAccount.setTotalInternalTransferFees(bankAccount.getTotalInternalTransferFees().add(bankInfo.getInternalTransferFees()));
                break;
            case "EXTERNAL_TRANSFER":
                bankAccount.setTotalExternalTransferFees(bankAccount.getTotalExternalTransferFees().add(bankInfo.getExternalTransferFees()));
                break;
            default:
                throw new IllegalArgumentException("Type d’opération inconnu : " + operationType);
        }
        return bankAccountRepository.save(bankAccount);
    }

    public BankAccount getBankAccountByBankId(Long bankId) {
        BankAccount bankAccount = bankAccountRepository.findByBankId(bankId);
        if (bankAccount == null) {
            throw new IllegalArgumentException("Compte financier non trouvé pour cette banque");
        }
        return bankAccount;
    }

    public void updateBankInfoCache(BankInfoDTO bankInfo) {
        bankInfoCache.put(bankInfo.getId(), bankInfo);
    }

    public List<BankAccount> getAllBankAccounts() {
        return bankAccountRepository.findAll();
    }

    @Transactional
    public BankAccount addInternalTransferFees(Long id, BigDecimal amount) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Compte bancaire non trouvé avec l'ID: " + id));
        
        bankAccount.setTotalInternalTransferFees(
            bankAccount.getTotalInternalTransferFees().add(amount)
        );
        
        return bankAccountRepository.save(bankAccount);
    }

    @Transactional
    public BankAccount addExternalTransferFees(Long id, BigDecimal amount) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Compte bancaire non trouvé avec l'ID: " + id));
        
        bankAccount.setTotalExternalTransferFees(
            bankAccount.getTotalExternalTransferFees().add(amount)
        );
        
        return bankAccountRepository.save(bankAccount);
    }

}
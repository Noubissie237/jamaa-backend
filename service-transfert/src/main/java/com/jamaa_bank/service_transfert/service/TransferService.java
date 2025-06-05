package com.jamaa_bank.service_transfert.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jamaa_bank.service_transfert.dto.TransferRequest;
import com.jamaa_bank.service_transfert.model.AccountType;

@Service
public class TransferService {
    @Autowired
    private TransfertService transfertService; // Pour les transferts application → application

    @Transactional
    public void transfer(TransferRequest request) {
        if (request.getFromAccountType() == AccountType.APPLICATION && request.getToAccountType() == AccountType.APPLICATION) {
            transfertService.transfertAppAccounts(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        } else if (request.getFromAccountType() == AccountType.APPLICATION && request.getToAccountType() == AccountType.BANK) {
            // TODO: Appeler le service bank-account plus tard
            throw new UnsupportedOperationException("Transfert vers un compte bancaire non encore implémenté");
        } else if (request.getFromAccountType() == AccountType.BANK && request.getToAccountType() == AccountType.APPLICATION) {
            // TODO: Appeler le service bank-account plus tard
            throw new UnsupportedOperationException("Transfert depuis un compte bancaire non encore implémenté");
        } else if (request.getFromAccountType() == AccountType.BANK && request.getToAccountType() == AccountType.BANK) {
            // TODO: Appeler le service bank-account plus tard
            throw new UnsupportedOperationException("Transfert entre comptes bancaires non encore implémenté");
        } else {
            throw new IllegalArgumentException("Type de compte inconnu");
        }
    }
} 
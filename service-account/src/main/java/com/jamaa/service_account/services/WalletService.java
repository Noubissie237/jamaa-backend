package com.jamaa.service_account.services;

import com.jamaa.service_account.models.Wallet;
import com.jamaa.service_account.repositories.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    // 🔹 Créer un portefeuille pour un utilisateur donné
    public Wallet createWallet(String userId, String name) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setName(name);
        wallet.setBalance(0.0);
        wallet.setCreationDate(Date.valueOf(LocalDate.now()));
        return walletRepository.save(wallet);
    }

    // 🔹 Obtenir un portefeuille à partir de l'identifiant utilisateur
    public Wallet getWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    // 🔹 Mettre à jour le solde du portefeuille (ajouter ou retirer de l'argent)
    public Wallet updateBalance(String userId, Double amount) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        return walletRepository.save(wallet);
    }

    // 🔹 Supprimer le portefeuille d’un utilisateur
    public Boolean deleteWallet(String userId) {
        Wallet wallet = getWalletByUserId(userId);
        walletRepository.delete(wallet);
        return true;
    }
    public Wallet getWallet(String userId) {
        return getWalletByUserId(userId);
    }
    

}

package com.jamaa.service_account.controllers;

import com.jamaa.service_account.models.Wallet;
import com.jamaa.service_account.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.Argument;
import java.util.Optional;

@Controller
public class WalletGraphQLController {

    @Autowired
    private WalletService walletService;

    // 🔹 Créer un portefeuille
    @MutationMapping
    public Wallet createWallet(@Argument Optional<String> userId, @Argument Optional<String> name) {
        String userIdValue = userId.orElse("defaultUserId");
        String nameValue = name.orElse("defaultName");
        return walletService.createWallet(userIdValue, nameValue);
    }

    // 🔹 Récupérer un portefeuille par userId
    @QueryMapping
    public Wallet getWallet(@Argument String userId) {
        return walletService.getWalletByUserId(userId);
    }

    // 🔹 Mettre à jour le solde
    @MutationMapping
    public Wallet updateBalance(@Argument String userId, @Argument Double amount) {
        return walletService.updateBalance(userId, amount);
    }

    // 🔹 Supprimer un portefeuille
    @MutationMapping
    public Boolean deleteWallet(@Argument String userId) {
        walletService.deleteWallet(userId);
        return true;
    }
}

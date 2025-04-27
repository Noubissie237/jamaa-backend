package com.example.wallet.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.jamaa.service_account.models.Wallet;
import com.jamaa.service_account.services.WalletService;

import org.springframework.graphql.data.method.annotation.Argument;

@Controller
public class WalletResolver {

    @Autowired
    private WalletService service;

    @QueryMapping
    public Wallet getWallet(@Argument String userId) {
        return service.getWallet(userId);
    }

    @MutationMapping
    public Wallet createWallet(@Argument String userId, @Argument String name) {
        return service.createWallet(userId, name);
    }

    @MutationMapping
    public Wallet updateBalance(@Argument String userId, @Argument double amount) {
        return service.updateBalance(userId, amount);
    }

    @MutationMapping
    public Boolean deleteWallet(@Argument String userId) {
        return service.deleteWallet(userId);
    }
}

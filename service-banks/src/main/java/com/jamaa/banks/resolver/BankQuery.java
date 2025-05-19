package com.jamaa.banks.resolver;

import com.jamaa.banks.dto.BankResponse;
import com.jamaa.banks.dto.BankSubscriptionStats;
import com.jamaa.banks.model.entities.Bank;
import com.jamaa.banks.model.entities.BankSubscription;
import com.jamaa.banks.service.BankService;
import com.jamaa.banks.service.BankSubscriptionService;
import com.jamaa.banks.service.BankSubscriptionStatsService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;


import java.util.List;

/**
 * Resolver pour toutes les queries GraphQL liées aux banques
 */
@Controller
@RequiredArgsConstructor
public class BankQuery {

    private final BankService bankService;
    private final BankSubscriptionService subscriptionService;
    private final BankSubscriptionStatsService statsService;

    @QueryMapping
    public Bank bank(@Argument Long id) {
        return bankService.getBank(id);
    }

    @QueryMapping
    public List<Bank> banks() {
        return bankService.getAllBanks();
    }

    @QueryMapping
    public List<BankResponse> activeBanks() {
        return bankService.getActiveBanks();
    }

    @QueryMapping
    public BankSubscription bankSubscription(@Argument Long id) {
        return subscriptionService.getSubscription(id);
    }

    @QueryMapping
    public List<BankSubscription> userBankSubscriptions(@Argument Long userId) {
        return subscriptionService.getUserSubscriptions(userId);
    }

    @QueryMapping
    public BankSubscriptionStats bankSubscriptionStats(@Argument Long bankId) {
        return subscriptionService.getBankSubscriptionStats(bankId);
    }

    @QueryMapping
    public List<BankSubscriptionStats> allBanksSubscriptionStats() {
        return statsService.getAllBanksStats();
    }
} 
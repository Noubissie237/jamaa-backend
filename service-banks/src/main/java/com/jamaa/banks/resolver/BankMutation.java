package com.jamaa.banks.resolver;

import com.jamaa.banks.dto.CreateBankInput;
import com.jamaa.banks.dto.UpdateBankInput;
import com.jamaa.banks.dto.BankSubscriptionInput;
import com.jamaa.banks.dto.BankFeeInput;
import com.jamaa.banks.model.entities.Bank;
import com.jamaa.banks.model.entities.BankSubscription;
import com.jamaa.banks.model.enums.SubscriptionStatus;
import com.jamaa.banks.service.BankService;
import com.jamaa.banks.service.BankSubscriptionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Resolver pour toutes les mutations GraphQL liées aux banques
 */
@Controller
@Validated
@RequiredArgsConstructor
public class BankMutation {

    private final BankService bankService;
    private final BankSubscriptionService subscriptionService;

    @MutationMapping
    public Bank createBank(@Argument @Valid CreateBankInput input) {
        return bankService.createBank(input);
    }

    @MutationMapping
    public Bank updateBank(@Argument Long id, @Argument @Valid UpdateBankInput input) {
        return bankService.updateBank(id, input);
    }

    @MutationMapping
    public Bank toggleBankStatus(@Argument Long id) {
        return bankService.toggleBankStatus(id);
    }

    @MutationMapping
    public BankSubscription createBankSubscription(@Argument @Valid BankSubscriptionInput input) {
        return subscriptionService.subscribeToBank(input.getUserId(), input.getBankId());
    }

    @MutationMapping
    public BankSubscription approveBankSubscription(@Argument Long id) {
        return subscriptionService.updateSubscriptionStatus(id, SubscriptionStatus.APPROVED, null);
    }

    @MutationMapping
    public BankSubscription rejectBankSubscription(@Argument Long id, @Argument String reason) {
        return subscriptionService.updateSubscriptionStatus(id, SubscriptionStatus.REJECTED, reason);
    }

    @MutationMapping
    public BankSubscription closeBankSubscription(@Argument Long id) {
        return subscriptionService.updateSubscriptionStatus(id, SubscriptionStatus.CLOSED, null);
    }

    @MutationMapping
    public Bank addBankFee(@Argument Long bankId, @Argument @Valid BankFeeInput input) {
        return bankService.addBankFee(bankId, input);
    }

    @MutationMapping
    public Bank updateBankFee(@Argument Long bankId, @Argument Long feeId, @Argument @Valid BankFeeInput input) {
        return bankService.updateBankFee(bankId, feeId, input);
    }

    @MutationMapping
    public Bank removeBankFee(@Argument Long bankId, @Argument Long feeId) {
        return bankService.removeBankFee(bankId, feeId);
    }

    @MutationMapping
    public Bank updateBankFees(@Argument Long bankId, @Argument List<@Valid BankFeeInput> fees) {
        return bankService.updateBankFees(bankId, fees);
    }
} 
package com.jamaa.service_account.resolver;

import java.io.IOException;
import java.math.BigDecimal;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.jamaa.service_account.events.CustomerEvent;
import com.jamaa.service_account.model.Account;
import com.jamaa.service_account.service.AccountService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AccountMutationResolver {
    
    private final AccountService accountService;

    @MutationMapping
    public void createAccount(@Argument CustomerEvent customer) throws IOException {
        accountService.createAccount(customer);
    }

    @MutationMapping
    public Account incrementBalance(@Argument Long accountId, @Argument BigDecimal amount) throws IOException{
        return accountService.incrementBalance(accountId,amount);
    }

    @MutationMapping
    public Account decrementBalance(@Argument Long accountId, @Argument BigDecimal amount) throws IOException{
        return accountService.decrementBalance(accountId,amount);
    }
}

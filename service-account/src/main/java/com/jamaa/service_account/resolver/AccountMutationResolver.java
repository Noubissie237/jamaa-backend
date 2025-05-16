package com.jamaa.service_account.resolver;

import java.io.IOException;
import java.math.BigDecimal;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.jamaa.service_account.model.Account;
import com.jamaa.service_account.service.AccountService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AccountMutationResolver {
    
    private final AccountService accountService;

    @MutationMapping
    public Account createAccount(@Argument Long userId) throws IOException {
        return accountService.createAccount(userId);
    }
    @MutationMapping
    public Account incrementBalance(@Argument BigDecimal amount ,@Argument Long accountId) throws IOException{
        return accountService.incrementBalance(accountId,amount);
    }
}

package com.jamaa.service_account.resolver;

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
    public Account createAccount(@Argument Long userId, @Argument String firstName) {
        return accountService.createAccount(userId, firstName);
    }
}

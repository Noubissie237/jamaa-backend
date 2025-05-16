package com.jamaa.service_account.resolver;

import java.util.List;
import java.util.Optional;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.jamaa.service_account.model.Account;
import com.jamaa.service_account.service.AccountService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AccountQueryResolver {
    private final AccountService accountService;

    @QueryMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @QueryMapping
    public Optional<Account> getAccount(@Argument Long id) {
        return accountService.getAccount(id);
    }
}

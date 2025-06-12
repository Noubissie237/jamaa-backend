package com.jamaa.service_banks_account.resolver;





import com.jamaa.service_banks_account.model.BankAccount;
import com.jamaa.service_banks_account.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BankAccountGraphQLController {

    private final BankAccountService bankAccountService;

    @QueryMapping
    public BankAccount getBankAccountByBankId(@Argument Long bankId) {
        return bankAccountService.getBankAccountByBankId(bankId);
    }

    @QueryMapping
    public List<BankAccount> getAllBankAccounts() {
        return bankAccountService.getAllBankAccounts();
    }
}

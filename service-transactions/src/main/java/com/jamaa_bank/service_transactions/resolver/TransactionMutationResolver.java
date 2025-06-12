package com.jamaa_bank.service_transactions.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.jamaa_bank.service_transactions.services.TransactionService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class TransactionMutationResolver {
    
    @Autowired
    private TransactionService transactionService;

    @MutationMapping
    public String deleteTransactionStream() {
        transactionService.deleteTransactionStream();
        return "Stream deletion initiated.";
    }

}

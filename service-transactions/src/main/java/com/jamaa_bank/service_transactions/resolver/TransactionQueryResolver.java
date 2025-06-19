package com.jamaa_bank.service_transactions.resolver;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.jamaa_bank.service_transactions.event.TransactionEvent;
import com.jamaa_bank.service_transactions.services.TransactionService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class TransactionQueryResolver {

    @Autowired
    private TransactionService transactionService;
    
    @QueryMapping
    public List<TransactionEvent> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @QueryMapping
    public List<TransactionEvent> getTransactionByIdAccount(@Argument Long idAccount) {
        return transactionService.getTransactionByIdAccount(idAccount);
    }

    @QueryMapping
    public List<TransactionEvent> getTransactionsByUserId(@Argument Long userId) {
        return transactionService.getTransactionsByUserId(userId);
    }
}

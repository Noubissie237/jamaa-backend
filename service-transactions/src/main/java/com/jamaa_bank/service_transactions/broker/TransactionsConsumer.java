package com.jamaa_bank.service_transactions.broker;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jamaa_bank.service_transactions.event.TransactionEvent;
import com.jamaa_bank.service_transactions.event.TransactionTemplate;
import com.jamaa_bank.service_transactions.model.TransactionType;
import com.jamaa_bank.service_transactions.services.TransactionService;

@Component
public class TransactionsConsumer {
    
    @Autowired
    private TransactionService transactionService;

    @RabbitListener(queues = "transfertDoneQueue")
    public void receiveTransfertEvent(TransactionTemplate event) {

        TransactionEvent transac = new TransactionEvent();

        transac.setIdAccountSender(event.getIdAccountSender());
        transac.setIdAccountReceiver(event.getIdAccountReceiver());
        transac.setAmount(event.getAmount());
        transac.setCreatedAt(event.getCreatedAt());
        transac.setStatus(event.getStatus());
        transac.setTransactionType(TransactionType.TRANSFERT);
        transac.setDateEvent(LocalDateTime.now());

        transactionService.saveTransaction(transac);
    }
}

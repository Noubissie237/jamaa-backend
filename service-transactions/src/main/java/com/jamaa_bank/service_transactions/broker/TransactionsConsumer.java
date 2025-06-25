package com.jamaa_bank.service_transactions.broker;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jamaa_bank.service_transactions.event.RechargeRetraitEventTemplate;
import com.jamaa_bank.service_transactions.event.TransactionEvent;
import com.jamaa_bank.service_transactions.event.TransactionTemplate;
import com.jamaa_bank.service_transactions.model.TransactionStatus;
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
        transac.setIdBankSender(event.getIdBankSender());
        transac.setTransactionType(TransactionType.TRANSFERT);
        transac.setDateEvent(LocalDateTime.now());

        transactionService.saveTransaction(transac);
    }

    @RabbitListener(queues = "virementDoneQueue")
    public void receiveVirementEvent(TransactionTemplate event) {

        TransactionEvent transac = new TransactionEvent();

        transac.setIdAccountSender(event.getIdAccountSender());
        transac.setIdAccountReceiver(event.getIdAccountReceiver());
        transac.setAmount(event.getAmount());
        transac.setCreatedAt(event.getCreatedAt());
        transac.setStatus(event.getStatus());
        transac.setIdBankSender(event.getIdBankSender());
        transac.setTransactionType(TransactionType.VIREMENT);
        transac.setDateEvent(LocalDateTime.now());

        transactionService.saveTransaction(transac);
    }

    @RabbitListener(queues = "recharge.queue.transaction")
    public void receiveRechargeEvent(RechargeRetraitEventTemplate event) {

        TransactionEvent transac = new TransactionEvent();

        // Pour une recharge : l'account est le sender (débité) et la card est le receiver
        transac.setIdAccountSender(event.getAccountId());
        transac.setIdAccountReceiver(event.getCardId()); // On utilise cardId comme receiver
        transac.setAmount(event.getAmount());
        transac.setCreatedAt(event.getCreatedAt());
        transac.setStatus(TransactionStatus.valueOf(event.getStatus()));
        transac.setIdBankSender(event.getBankId());
        transac.setTransactionType(TransactionType.RECHARGE);
        transac.setDateEvent(LocalDateTime.now());

        transactionService.saveTransaction(transac);
    }

    @RabbitListener(queues = "retrait.queue.transaction")
    public void receiveRetraitEvent(RechargeRetraitEventTemplate event) {

        TransactionEvent transac = new TransactionEvent();

        // Pour un retrait : la card est le sender (débitée) et l'account est le receiver
        transac.setIdAccountSender(event.getCardId()); // On utilise cardId comme sender
        transac.setIdAccountReceiver(event.getAccountId());
        transac.setAmount(event.getAmount());
        transac.setCreatedAt(event.getCreatedAt());
        transac.setStatus(TransactionStatus.valueOf(event.getStatus()));
        transac.setIdBankSender(event.getBankId());
        transac.setTransactionType(TransactionType.RETRAIT);
        transac.setDateEvent(LocalDateTime.now());

        transactionService.saveTransaction(transac);
    }
}

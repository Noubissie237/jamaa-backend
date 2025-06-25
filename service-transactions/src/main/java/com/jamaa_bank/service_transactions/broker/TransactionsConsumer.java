package com.jamaa_bank.service_transactions.broker;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionsConsumer.class);
    
    @Autowired
    private TransactionService transactionService;

    @RabbitListener(queues = "transfertDoneQueue")
    public void receiveTransfertEvent(TransactionTemplate event) {
        logger.info("Received TRANSFERT event: sender={}, receiver={}, amount={}, bankId={}", 
                   event.getIdAccountSender(), event.getIdAccountReceiver(), 
                   event.getAmount(), event.getBankId());
        
        try {
            TransactionEvent transac = createTransactionEvent(event, TransactionType.TRANSFERT);
            transactionService.saveTransaction(transac);
            logger.info("TRANSFERT transaction saved successfully for sender={}, receiver={}", 
                       event.getIdAccountSender(), event.getIdAccountReceiver());
        } catch (Exception e) {
            logger.error("Failed to process TRANSFERT event for sender={}, receiver={}: {}", 
                        event.getIdAccountSender(), event.getIdAccountReceiver(), e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "virementDoneQueue")
    public void receiveVirementEvent(TransactionTemplate event) {
        logger.info("Received VIREMENT event: sender={}, receiver={}, amount={}, bankId={}", 
                   event.getIdAccountSender(), event.getIdAccountReceiver(), 
                   event.getAmount(), event.getBankId());
        
        try {
            TransactionEvent transac = createTransactionEvent(event, TransactionType.VIREMENT);
            transactionService.saveTransaction(transac);
            logger.info("VIREMENT transaction saved successfully for sender={}, receiver={}", 
                       event.getIdAccountSender(), event.getIdAccountReceiver());
        } catch (Exception e) {
            logger.error("Failed to process VIREMENT event for sender={}, receiver={}: {}", 
                        event.getIdAccountSender(), event.getIdAccountReceiver(), e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "recharge.queue.transaction")
    public void receiveRechargeEvent(RechargeRetraitEventTemplate event) {
        logger.info("Received RECHARGE event: accountId={}, cardId={}, amount={}, status={}, bankId={}", 
                   event.getAccountId(), event.getCardId(), event.getAmount(), 
                   event.getStatus(), event.getBankId());
        
        try {
            TransactionEvent transac = createRechargeRetraitEvent(event, TransactionType.RECHARGE);
            transactionService.saveTransaction(transac);
            logger.info("RECHARGE transaction saved successfully for account={}, card={}", 
                       event.getAccountId(), event.getCardId());
        } catch (Exception e) {
            logger.error("Failed to process RECHARGE event for account={}, card={}: {}", 
                        event.getAccountId(), event.getCardId(), e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "retrait.queue.transaction")
    public void receiveRetraitEvent(RechargeRetraitEventTemplate event) {
        logger.info("Received RETRAIT event: accountId={}, cardId={}, amount={}, status={}, bankId={}", 
                   event.getAccountId(), event.getCardId(), event.getAmount(), 
                   event.getStatus(), event.getBankId());
        
        try {
            TransactionEvent transac = createRechargeRetraitEvent(event, TransactionType.RETRAIT);
            transactionService.saveTransaction(transac);
            logger.info("RETRAIT transaction saved successfully for account={}, card={}", 
                       event.getAccountId(), event.getCardId());
        } catch (Exception e) {
            logger.error("Failed to process RETRAIT event for account={}, card={}: {}", 
                        event.getAccountId(), event.getCardId(), e.getMessage(), e);
        }
    }

    private TransactionEvent createTransactionEvent(TransactionTemplate event, TransactionType type) {
        logger.debug("Creating transaction event of type: {}", type);
        
        TransactionEvent transac = new TransactionEvent();
        transac.setIdAccountSender(event.getIdAccountSender());
        transac.setIdAccountReceiver(event.getIdAccountReceiver());
        transac.setAmount(event.getAmount());
        transac.setCreatedAt(event.getCreatedAt());
        transac.setStatus(event.getStatus());
        transac.setBankId(event.getBankId());
        transac.setTransactionType(type);
        transac.setDateEvent(LocalDateTime.now());
        
        return transac;
    }


    private TransactionEvent createRechargeRetraitEvent(RechargeRetraitEventTemplate event, TransactionType type) {
        logger.debug("Creating recharge/retrait transaction event of type: {}", type);
        
        TransactionEvent transac = new TransactionEvent();
        transac.setAmount(event.getAmount());
        transac.setCreatedAt(event.getCreatedAt());
        transac.setBankId(event.getBankId());
        transac.setTransactionType(type);
        transac.setDateEvent(LocalDateTime.now());
        
        // Gestion sécurisée du status
        try {
            transac.setStatus(TransactionStatus.valueOf(event.getStatus()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid transaction status '{}' received, defaulting to PENDING", event.getStatus());
            transac.setStatus(TransactionStatus.PENDING); 
        }
        
        // Logique spécifique selon le type de transaction
        if (type == TransactionType.RECHARGE) {
            // Pour une recharge : l'account est le sender (débité) et la card est le receiver
            transac.setIdAccountSender(event.getAccountId());
            transac.setIdAccountReceiver(event.getCardId());
            logger.debug("RECHARGE: account {} charges card {}", event.getAccountId(), event.getCardId());
        } else if (type == TransactionType.RETRAIT) {
            // Pour un retrait : la card est le sender (débitée) et l'account est le receiver
            transac.setIdAccountSender(event.getCardId());
            transac.setIdAccountReceiver(event.getAccountId());
            logger.debug("RETRAIT: card {} withdraws to account {}", event.getCardId(), event.getAccountId());
        }
        
        return transac;
    }
}
package com.jamaa_bank.service_transfert.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jamaa_bank.service_transfert.dto.AccountDTO;
import com.jamaa_bank.service_transfert.events.TransfertEvent;
import com.jamaa_bank.service_transfert.exception.InsufficientBalanceException;
import com.jamaa_bank.service_transfert.exception.TransfertException;
import com.jamaa_bank.service_transfert.model.Transfert;
import com.jamaa_bank.service_transfert.repository.TransfertRepository;
import com.jamaa_bank.service_transfert.utils.Util;

@Service
public class TransfertService {
    
    @Autowired
    private TransfertRepository transfertRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Util util;

    @Transactional(rollbackFor = {IOException.class, RuntimeException.class})
    public Transfert transfertAppAccounts(Long idSenderAccount, Long idReceiverAccount, BigDecimal amount) {
        if (idSenderAccount == null || idReceiverAccount == null || amount == null) {
            throw new IllegalArgumentException("Les paramètres du transfert ne peuvent pas être null");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à zéro");
        }
        
        if (idSenderAccount.equals(idReceiverAccount)) {
            throw new IllegalArgumentException("Le compte émetteur et destinataire ne peuvent pas être identiques");
        }
        
        AccountDTO senderAccount = util.getAccount(idSenderAccount);
        if (senderAccount == null) {
            throw new TransfertException("Compte émetteur introuvable");
        }

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Solde insuffisant pour effectuer le transfert");
        }
        
        AccountDTO receiverAccount = util.getAccount(idReceiverAccount);
        if (receiverAccount == null) {
            throw new TransfertException("Compte destinataire introuvable");
        }

        util.decrementBalance(idSenderAccount, amount);
        util.incrementBalance(idReceiverAccount, amount);
        
        Transfert transfert = new Transfert();
        transfert.setSenderAccountId(idSenderAccount);
        transfert.setReceiverAccountId(idReceiverAccount);
        transfert.setAmount(amount);
        transfert.setCreateAt(LocalDateTime.now());

        transfert = transfertRepository.save(transfert);

        publishTransfertEvents(idSenderAccount, idReceiverAccount, amount);
        
        return transfert;
    }
    
    public List<Transfert> getAllTransferts() {
        return transfertRepository.findAll();
    }

    private void publishTransfertEvents(Long idSenderAccount, Long idReceiverAccount, BigDecimal amount) {
        TransfertEvent event = new TransfertEvent();
        event.setSenderAccountId(idSenderAccount);
        event.setReceiverAccountId(idReceiverAccount);
        event.setAmount(amount);
        event.setCreateAt(LocalDateTime.now());

        rabbitTemplate.convertAndSend("AccountExchange", "notification.tranfer.done", event);
        rabbitTemplate.convertAndSend("AccountExchange", "transaction.tranfer.done", event);
    }
}
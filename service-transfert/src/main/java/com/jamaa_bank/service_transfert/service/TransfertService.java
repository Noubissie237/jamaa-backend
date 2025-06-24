package com.jamaa_bank.service_transfert.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jamaa_bank.service_transfert.dto.AccountDTO;
import com.jamaa_bank.service_transfert.dto.BankAccountDTO;
import com.jamaa_bank.service_transfert.dto.BankDTO;
import com.jamaa_bank.service_transfert.dto.CardDTO;
import com.jamaa_bank.service_transfert.events.TransfertEvent;
import com.jamaa_bank.service_transfert.exception.InsufficientBalanceException;
import com.jamaa_bank.service_transfert.exception.TransfertException;
import com.jamaa_bank.service_transfert.model.TransactionStatus;
import com.jamaa_bank.service_transfert.model.Transfert;
import com.jamaa_bank.service_transfert.repository.TransfertRepository;
import com.jamaa_bank.service_transfert.utils.BankAccountUtil;
import com.jamaa_bank.service_transfert.utils.BankUtil;
import com.jamaa_bank.service_transfert.utils.CardUtil;
import com.jamaa_bank.service_transfert.utils.Util;

@Service
public class TransfertService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransfertService.class);

    @Autowired
    private TransfertRepository transfertRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Util util;

    @Autowired
    private CardUtil cardUtil;

    @Autowired
    private BankUtil bankUtil;

    @Autowired
    private BankAccountUtil bankAccountUtil;

    @Transactional(rollbackFor = { IOException.class, RuntimeException.class })
    public Transfert transfertAppAccounts(Long idSenderAccount, Long idReceiverAccount, BigDecimal amount) {
        logger.info("Début de transfert: de compte {} vers compte {}, montant: {}", 
                    idSenderAccount, idReceiverAccount, amount);
        
        try {
            if (idSenderAccount == null || idReceiverAccount == null || amount == null) {
                logger.error("Transfert impossible: paramètres null détectés");
                throw new IllegalArgumentException("Les paramètres du transfert ne peuvent pas être null");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("Transfert impossible: montant invalide {}", amount);
                throw new IllegalArgumentException("Le montant doit être supérieur à zéro");
            }

            if (idSenderAccount.equals(idReceiverAccount)) {
                logger.error("Transfert impossible: comptes identiques {}", idSenderAccount);
                throw new IllegalArgumentException("Le compte émetteur et destinataire ne peuvent pas être identiques");
            }

            logger.debug("Récupération des informations du compte émetteur {}", idSenderAccount);
            AccountDTO senderAccount = util.getAccount(idSenderAccount);
            if (senderAccount == null) {
                logger.error("Transfert impossible: compte émetteur {} introuvable", idSenderAccount);
                throw new TransfertException("Compte émetteur introuvable");
            }

            if (senderAccount.getBalance().compareTo(amount) < 0) {
                logger.error("Transfert impossible: solde insuffisant sur compte {} (solde: {}, montant demandé: {})", 
                            idSenderAccount, senderAccount.getBalance(), amount);
                throw new InsufficientBalanceException("Solde insuffisant pour effectuer le transfert");
            }

            logger.debug("Récupération des informations du compte destinataire {}", idReceiverAccount);
            AccountDTO receiverAccount = util.getAccount(idReceiverAccount);
            if (receiverAccount == null) {
                logger.error("Transfert impossible: compte destinataire {} introuvable", idReceiverAccount);
                throw new TransfertException("Compte destinataire introuvable");
            }

            logger.debug("Débit du compte {} d'un montant de {}", idSenderAccount, amount);
            util.decrementBalance(idSenderAccount, amount);
            
            logger.debug("Crédit du compte {} d'un montant de {}", idReceiverAccount, amount);
            util.incrementBalance(idReceiverAccount, amount);

            Transfert transfert = new Transfert();
            transfert.setSenderAccountId(idSenderAccount);
            transfert.setReceiverAccountId(idReceiverAccount);
            transfert.setAmount(amount);
            transfert.setCreateAt(LocalDateTime.now());

            logger.debug("Enregistrement du transfert en base de données");
            transfert = transfertRepository.save(transfert);

            logger.info("Transfert réussi: ID={}, de compte {} vers compte {}, montant: {}", 
                        transfert.getId(), idSenderAccount, idReceiverAccount, amount);
            
            logger.debug("Publication des événements de transfert réussi");
            publishTransfertEvents(idSenderAccount, idReceiverAccount, amount, TransactionStatus.SUCCESS, "APP", (long) 0);
            return transfert;

        } catch (Exception e) {
            logger.error("Erreur lors du transfert de {} vers {}, montant: {}: {}", 
                        idSenderAccount, idReceiverAccount, amount, e.getMessage(), e);
            
            logger.debug("Publication des événements de transfert échoué");
            publishTransfertEvents(idSenderAccount, idReceiverAccount, amount, TransactionStatus.FAILED, "APP", (long) 0);
            throw e; 
        }
    }

    @Transactional(rollbackFor = { IOException.class, RuntimeException.class })
    public Transfert transfertBank(Long idSenderBank, Long idReceiverBank, BigDecimal amount) {
        logger.info("Début de transfert bancaire: de banque {} vers banque {}, montant: {}", 
                    idSenderBank, idReceiverBank, amount);
        
        try {
            if (idSenderBank == null || idReceiverBank == null || amount == null) {
                logger.error("Transfert bancaire impossible: paramètres null détectés");
                throw new IllegalArgumentException("Les paramètres du transfert ne peuvent pas être null");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("Transfert bancaire impossible: montant invalide {}", amount);
                throw new IllegalArgumentException("Le montant doit être supérieur à zéro");
            }

            if (idSenderBank.equals(idReceiverBank)) {
                logger.error("Transfert bancaire impossible: banques identiques {}", idSenderBank);
                throw new IllegalArgumentException("La banque émettrice et destinataire ne peuvent pas être identiques");
            }

            logger.debug("Récupération de la carte de la banque émettrice {}", idSenderBank);
            CardDTO senderCard = cardUtil.getCardByBankId(idSenderBank);
            if (senderCard == null) {
                logger.error("Transfert bancaire impossible: carte de la banque émettrice {} introuvable", idSenderBank);
                throw new TransfertException("Carte de la banque émettrice introuvable");
            }

            logger.debug("Récupération de la carte de la banque destinataire {}", idReceiverBank);
            CardDTO receiverCard = cardUtil.getCardByBankId(idReceiverBank);
            if (receiverCard == null) {
                logger.error("Transfert bancaire impossible: carte de la banque destinataire {} introuvable", idReceiverBank);
                throw new TransfertException("Carte de la banque destinataire introuvable");
            }

            // Déterminer le type de transfert et calculer les frais
            boolean isSameBank = senderCard.getBankId().equals(receiverCard.getBankId());
            logger.debug("Type de transfert: {} (même banque: {})", isSameBank ? "INTERNE" : "EXTERNE", isSameBank);
            
            // Récupérer les informations de la banque émettrice pour les frais
            BankDTO senderBankInfo = bankUtil.getBankById(senderCard.getBankId());
            BigDecimal feeRate = isSameBank ? senderBankInfo.getInternalTransferFees() : senderBankInfo.getExternalTransferFees();
            BigDecimal feeAmount = amount.multiply(feeRate).divide(BigDecimal.valueOf(100));
            BigDecimal totalAmountWithFees = amount.add(feeAmount);
            
            logger.debug("Frais calculés: taux={}%, montant des frais={}, montant total={}", 
                        feeRate, feeAmount, totalAmountWithFees);

            if (senderCard.getCurrentBalance().compareTo(totalAmountWithFees) < 0) {
                logger.error("Transfert bancaire impossible: solde insuffisant sur la carte de la banque {} (solde: {}, montant total requis: {})", 
                            idSenderBank, senderCard.getCurrentBalance(), totalAmountWithFees);
                throw new InsufficientBalanceException("Solde insuffisant pour effectuer le transfert bancaire (frais inclus)");
            }

            // Effectuer le transfert principal
            logger.debug("Débit de la carte {} (banque {}) d'un montant de {} (montant + frais)", 
                        senderCard.getId(), idSenderBank, totalAmountWithFees);
            cardUtil.decrementCardBalance(senderCard.getId(), totalAmountWithFees);
            
            logger.debug("Crédit de la carte {} (banque {}) d'un montant de {}", 
                        receiverCard.getId(), idReceiverBank, amount);
            cardUtil.incrementCardBalance(receiverCard.getId(), amount);

            // Verser les frais dans le compte bancaire approprié
            BankAccountDTO senderBankAccount = bankAccountUtil.getBankAccountByBankId(senderCard.getBankId());
            if (isSameBank) {
                logger.debug("Versement des frais internes ({}) dans le compte bancaire ID: {}", 
                            feeAmount, senderBankAccount.getId());
                bankAccountUtil.addInternalTransferFees(senderBankAccount.getId(), feeAmount);
            } else {
                logger.debug("Versement des frais externes ({}) dans le compte bancaire ID: {}", 
                            feeAmount, senderBankAccount.getId());
                bankAccountUtil.addExternalTransferFees(senderBankAccount.getId(), feeAmount);
            }

            Transfert transfert = new Transfert();
            transfert.setSenderAccountId(idSenderBank); // Utilise bankId comme identifiant
            transfert.setReceiverAccountId(idReceiverBank); // Utilise bankId comme identifiant
            transfert.setAmount(amount);
            transfert.setCreateAt(LocalDateTime.now());

            logger.debug("Enregistrement du transfert bancaire en base de données");
            transfert = transfertRepository.save(transfert);

            logger.info("Transfert bancaire réussi: ID={}, de banque {} vers banque {}, montant: {}, frais: {}, type: {}", 
                        transfert.getId(), idSenderBank, idReceiverBank, amount, feeAmount, isSameBank ? "INTERNE" : "EXTERNE");
            
            logger.debug("Publication des événements de transfert bancaire réussi");
            publishTransfertEvents(idSenderBank, idReceiverBank, amount, TransactionStatus.SUCCESS, "BANK", senderCard.getBankId());
            return transfert;

        } catch (Exception e) {
            logger.error("Erreur lors du transfert bancaire de {} vers {}, montant: {}: {}", 
                        idSenderBank, idReceiverBank, amount, e.getMessage(), e);
            
            CardDTO senderCard = cardUtil.getCardByBankId(idSenderBank);


            logger.debug("Publication des événements de transfert bancaire échoué");
            publishTransfertEvents(idSenderBank, idReceiverBank, amount, TransactionStatus.FAILED, "BANK", senderCard.getBankId());
            throw e; 
        }
    }

    public List<Transfert> getAllTransferts() {
        logger.info("Récupération de tous les transferts");
        List<Transfert> transferts = transfertRepository.findAll();
        logger.debug("{} transferts récupérés", transferts.size());
        return transferts;
    }

    private void publishTransfertEvents(Long idSenderAccount, Long idReceiverAccount, BigDecimal amount,
            TransactionStatus status, String type, Long bankId) {
        logger.debug("Préparation de l'événement de transfert: {} -> {}, montant: {}, statut: {}", 
                    idSenderAccount, idReceiverAccount, amount, status);
        
        TransfertEvent event = new TransfertEvent();
        event.setIdAccountSender(idSenderAccount);
        event.setIdAccountReceiver(idReceiverAccount);
        event.setAmount(amount);
        event.setStatus(status);
        event.setIdBankSender(bankId);
        event.setCreatedAt(LocalDateTime.now());

        String routingKey = type == "BANK" ? "transactions.virement.done" : "transactions.transfer.done";

        try {
            logger.debug("Envoi de l'événement à la queue notification");
            rabbitTemplate.convertAndSend("AccountExchange", "notification.transfer.done", event);
            
            logger.debug("Envoi de l'événement à la queue transactions");
            rabbitTemplate.convertAndSend("TransactionExchange", routingKey, event);
            
            logger.info("Événements de transfert publiés avec succès: statut={}", status);
        } catch (Exception e) {
            logger.error("Erreur lors de la publication des événements de transfert: {}", e.getMessage(), e);
        }
    }
}
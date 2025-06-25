package com.jamaa_bank.service_recharge_retrait.service;

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

import com.jamaa_bank.service_recharge_retrait.dto.AccountDTO;
import com.jamaa_bank.service_recharge_retrait.dto.BankAccountDTO;
import com.jamaa_bank.service_recharge_retrait.dto.BankDTO;
import com.jamaa_bank.service_recharge_retrait.dto.CardDTO;
import com.jamaa_bank.service_recharge_retrait.events.RechargeRetraitEvent;
import com.jamaa_bank.service_recharge_retrait.exception.InsufficientBalanceException;
import com.jamaa_bank.service_recharge_retrait.exception.RechargeRetraitException;
import com.jamaa_bank.service_recharge_retrait.model.OperationType;
import com.jamaa_bank.service_recharge_retrait.model.RechargeRetrait;
import com.jamaa_bank.service_recharge_retrait.model.TransactionStatus;
import com.jamaa_bank.service_recharge_retrait.repository.RechargeRetraitRepository;
import com.jamaa_bank.service_recharge_retrait.utils.AccountUtil;
import com.jamaa_bank.service_recharge_retrait.utils.BankUtil;
import com.jamaa_bank.service_recharge_retrait.utils.CardUtil;

@Service
public class RechargeRetraitService {
    
    private static final Logger logger = LoggerFactory.getLogger(RechargeRetraitService.class);

    @Autowired
    private RechargeRetraitRepository rechargeRetraitRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AccountUtil accountUtil;

    @Autowired
    private CardUtil cardUtil;

    @Autowired
    private BankUtil bankUtil;

    @Transactional(rollbackFor = { IOException.class, RuntimeException.class })
    public RechargeRetrait recharge(Long accountId, Long cardId, BigDecimal amount) {
        logger.info("Début de recharge: compte {} vers carte {}, montant: {}", 
                    accountId, cardId, amount);
        
        try {
            // Validation des paramètres
            if (accountId == null || cardId == null || amount == null) {
                logger.error("Recharge impossible: paramètres null détectés");
                throw new IllegalArgumentException("Les paramètres de la recharge ne peuvent pas être null");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("Recharge impossible: montant invalide {}", amount);
                throw new IllegalArgumentException("Le montant doit être supérieur à zéro");
            }

            // Récupération et validation du compte
            logger.debug("Récupération des informations du compte {}", accountId);
            AccountDTO account = accountUtil.getAccount(accountId);
            if (account == null) {
                logger.error("Recharge impossible: compte {} introuvable", accountId);
                throw new RechargeRetraitException("Compte introuvable");
            }

            if (account.getBalance().compareTo(amount) < 0) {
                logger.error("Recharge impossible: solde insuffisant sur compte {} (solde: {}, montant demandé: {})", 
                            accountId, account.getBalance(), amount);
                throw new InsufficientBalanceException("Solde insuffisant pour effectuer la recharge");
            }

            // Récupération et validation de la carte
            logger.debug("Récupération des informations de la carte {}", cardId);
            CardDTO card = cardUtil.getCard(cardId);
            if (card == null) {
                logger.error("Recharge impossible: carte {} introuvable", cardId);
                throw new RechargeRetraitException("Carte introuvable");
            }

            // Effectuer les opérations de débit/crédit
            logger.debug("Débit du compte {} d'un montant de {}", accountId, amount);
            accountUtil.decrementBalance(accountId, amount);
            
            logger.debug("Crédit de la carte {} d'un montant de {}", cardId, amount);
            cardUtil.incrementCardBalance(cardId, amount);

            // Enregistrement de l'opération
            RechargeRetrait recharge = new RechargeRetrait();
            recharge.setAccountId(accountId);
            recharge.setCardId(cardId);
            recharge.setAmount(amount);
            recharge.setOperationType(OperationType.RECHARGE);
            recharge.setStatus(TransactionStatus.SUCCESS);
            recharge.setCreatedAt(LocalDateTime.now());

            logger.debug("Enregistrement de la recharge en base de données");
            recharge = rechargeRetraitRepository.save(recharge);

            logger.info("Recharge réussie: ID={}, compte {} vers carte {}, montant: {}", 
                        recharge.getId(), accountId, cardId, amount);
            
            // Publication de l'événement
            logger.debug("Publication de l'événement de recharge réussie");
            publishRechargeRetraitEvent(accountId, cardId, amount, OperationType.RECHARGE, TransactionStatus.SUCCESS, card.getBankId());
            
            return recharge;

        } catch (Exception e) {
            logger.error("Erreur lors de la recharge du compte {} vers carte {}, montant: {}: {}", 
                        accountId, cardId, amount, e.getMessage(), e);
            CardDTO card = cardUtil.getCard(cardId);
            // Publication de l'événement d'échec
            logger.debug("Publication de l'événement de recharge échouée");
            publishRechargeRetraitEvent(accountId, cardId, amount, OperationType.RECHARGE, TransactionStatus.FAILED, card.getBankId());
            throw e; 
        }
    }

    @Transactional(rollbackFor = { IOException.class, RuntimeException.class })
    public RechargeRetrait retrait(Long cardId, Long accountId, BigDecimal amount) {
        logger.info("Début de retrait: carte {} vers compte {}, montant: {}", 
                    cardId, accountId, amount);
        
        try {
            // Validation des paramètres
            if (cardId == null || accountId == null || amount == null) {
                logger.error("Retrait impossible: paramètres null détectés");
                throw new IllegalArgumentException("Les paramètres du retrait ne peuvent pas être null");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("Retrait impossible: montant invalide {}", amount);
                throw new IllegalArgumentException("Le montant doit être supérieur à zéro");
            }

            // Récupération et validation de la carte
            logger.debug("Récupération des informations de la carte {}", cardId);
            CardDTO card = cardUtil.getCard(cardId);
            if (card == null) {
                logger.error("Retrait impossible: carte {} introuvable", cardId);
                throw new RechargeRetraitException("Carte introuvable");
            }

            if (card.getCurrentBalance().compareTo(amount) < 0) {
                logger.error("Retrait impossible: solde insuffisant sur carte {} (solde: {}, montant demandé: {})", 
                            cardId, card.getCurrentBalance(), amount);
                throw new InsufficientBalanceException("Solde insuffisant sur la carte pour effectuer le retrait");
            }

            // Récupération et validation du compte
            logger.debug("Récupération des informations du compte {}", accountId);
            AccountDTO account = accountUtil.getAccount(accountId);
            if (account == null) {
                logger.error("Retrait impossible: compte {} introuvable", accountId);
                throw new RechargeRetraitException("Compte introuvable");
            }

            // ==================== NOUVELLE LOGIQUE POUR LES FRAIS BANCAIRES ====================
            
            // Extraction du bankId depuis la carte
            Long bankId = card.getBankId();
            logger.debug("BankId extrait de la carte: {}", bankId);
            
            // Récupération des informations de la banque pour obtenir les frais de retrait
            logger.debug("Récupération des informations de la banque {}", bankId);
            BankDTO bank = bankUtil.getBank(bankId);
            if (bank == null) {
                logger.error("Retrait impossible: banque {} introuvable", bankId);
                throw new RechargeRetraitException("Banque introuvable");
            }
            
            BigDecimal withdrawFeesPercentage = bank.getWithdrawFees();
            logger.debug("Pourcentage de frais de retrait pour la banque {}: {}%", bankId, withdrawFeesPercentage);
            
            // Calcul des frais en montant : (amount * withdrawFeesPercentage) / 100
            @SuppressWarnings("deprecation")
            BigDecimal feesAmount = amount.multiply(withdrawFeesPercentage)
                                        .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            
            // Calcul du montant net pour l'utilisateur : amount - feesAmount
            BigDecimal netAmount = amount.subtract(feesAmount);
            
            logger.debug("Montant original: {}, Frais calculés: {}, Montant net pour l'utilisateur: {}", 
                        amount, feesAmount, netAmount);
            
            // Récupération du compte bancaire pour cette banque
            logger.debug("Récupération du compte bancaire pour bankId: {}", bankId);
            BankAccountDTO bankAccount = bankUtil.getBankAccount(bankId);
            if (bankAccount == null) {
                logger.error("Retrait impossible: compte bancaire introuvable pour bankId: {}", bankId);
                throw new RechargeRetraitException("Compte bancaire introuvable");
            }
            
            // ==================== FIN NOUVELLE LOGIQUE ====================

            // Effectuer les opérations de débit/crédit
            logger.debug("Débit de la carte {} du montant total: {}", cardId, amount);
            cardUtil.decrementCardBalance(cardId, amount);
            
            logger.debug("Crédit du compte utilisateur {} du montant net: {} (après déduction des frais: {})", 
                        accountId, netAmount, feesAmount);
            accountUtil.incrementBalance(accountId, netAmount);

            // ==================== AJOUT DES FRAIS AU COMPTE BANCAIRE ====================
            
            // Incrémenter le solde total du compte bancaire avec les frais de retrait
            logger.debug("Ajout des frais de retrait ({}) au compte bancaire ID: {}", feesAmount, bankAccount.getId());
            bankUtil.incrementTotalBalance(bankAccount.getId(), feesAmount);
            logger.info("Frais de retrait de {} ({}%) prélevés sur {} et ajoutés au compte bancaire de la banque {}", 
                    feesAmount, withdrawFeesPercentage, amount, bankId);
            
            // ==================== FIN AJOUT DES FRAIS ====================

            // Enregistrement de l'opération
            RechargeRetrait retrait = new RechargeRetrait();
            retrait.setAccountId(accountId);
            retrait.setCardId(cardId);
            retrait.setAmount(amount);
            retrait.setOperationType(OperationType.RETRAIT);
            retrait.setStatus(TransactionStatus.SUCCESS);
            retrait.setCreatedAt(LocalDateTime.now());

            logger.debug("Enregistrement du retrait en base de données");
            retrait = rechargeRetraitRepository.save(retrait);

            logger.info("Retrait réussi: ID={}, carte {} vers compte {}, montant débité: {}, montant net crédité: {}, frais bancaires: {}", 
                        retrait.getId(), cardId, accountId, amount, netAmount, feesAmount);
            
            // Publication de l'événement
            logger.debug("Publication de l'événement de retrait réussi");
            publishRechargeRetraitEvent(accountId, cardId, amount, OperationType.RETRAIT, TransactionStatus.SUCCESS, card.getBankId());
            
            return retrait;

        } catch (Exception e) {
            logger.error("Erreur lors du retrait de la carte {} vers compte {}, montant: {}: {}", 
                        cardId, accountId, amount, e.getMessage(), e);
            
            // Récupération sécurisée de la carte pour l'événement d'échec
            try {
                CardDTO card = cardUtil.getCard(cardId);
                logger.debug("Publication de l'événement de retrait échoué");
                publishRechargeRetraitEvent(accountId, cardId, amount, OperationType.RETRAIT, TransactionStatus.FAILED, 
                                        card != null ? card.getBankId() : null);
            } catch (Exception eventException) {
                logger.warn("Impossible de publier l'événement d'échec: {}", eventException.getMessage());
            }
            
            throw e; 
        }
    }

    
    public List<RechargeRetrait> getAllOperations() {
        logger.info("Récupération de toutes les opérations recharge/retrait");
        List<RechargeRetrait> operations = rechargeRetraitRepository.findAll();
        logger.debug("{} opérations récupérées", operations.size());
        return operations;
    }

    public List<RechargeRetrait> getOperationsByAccount(Long accountId) {
        logger.info("Récupération des opérations pour le compte {}", accountId);
        List<RechargeRetrait> operations = rechargeRetraitRepository.findByAccountId(accountId);
        logger.debug("{} opérations trouvées pour le compte {}", operations.size(), accountId);
        return operations;
    }

    public List<RechargeRetrait> getOperationsByCard(Long cardId) {
        logger.info("Récupération des opérations pour la carte {}", cardId);
        List<RechargeRetrait> operations = rechargeRetraitRepository.findByCardId(cardId);
        logger.debug("{} opérations trouvées pour la carte {}", operations.size(), cardId);
        return operations;
    }

    public List<RechargeRetrait> getOperationsByType(OperationType operationType) {
        logger.info("Récupération des opérations de type {}", operationType);
        List<RechargeRetrait> operations = rechargeRetraitRepository.findByOperationType(operationType);
        logger.debug("{} opérations de type {} trouvées", operations.size(), operationType);
        return operations;
    }

    private void publishRechargeRetraitEvent(Long accountId, Long cardId, BigDecimal amount,
            OperationType operationType, TransactionStatus status, Long bankId) {
        logger.debug("Préparation de l'événement: compte {} <-> carte {}, montant: {}, type: {}, statut: {}", 
                    accountId, cardId, amount, operationType, status);
        
        RechargeRetraitEvent event = new RechargeRetraitEvent();
        event.setAccountId(accountId);
        event.setCardId(cardId);
        event.setAmount(amount);
        event.setOperationType(operationType);
        event.setStatus(status);
        event.setCreatedAt(LocalDateTime.now());
        event.setBankId(bankId);
        String routingKeyNotification = operationType == OperationType.RECHARGE ? 
            "notification.recharge.done" : "notification.retrait.done";

        String routingKeyTransaction = operationType == OperationType.RECHARGE ? 
            "transactions.recharge.done" : "transactions.retrait.done";

        
            

        try {
            logger.debug("Envoi de l'événement à la queue notification avec routing key: {}", routingKeyNotification);
            rabbitTemplate.convertAndSend("AccountExchange", routingKeyNotification, event);
            logger.debug("Événement envoyé avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'événement: {}", e.getMessage(), e);
            // Ne pas faire échouer la transaction pour un problème de notification
        }

        try {
            logger.debug("Envoi de l'événement à la queue transactions avec routing key: {}", routingKeyTransaction);
            rabbitTemplate.convertAndSend("TransactionExchange", routingKeyTransaction, event);
            logger.debug("Événement envoyé avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'événement: {}", e.getMessage(), e);
            // Ne pas faire échouer la transaction pour un problème de notification
        }
    }
}

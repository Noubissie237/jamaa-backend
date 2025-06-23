package com.jamaa_bank.service_recharge_retrait.resolver;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.jamaa_bank.service_recharge_retrait.model.OperationType;
import com.jamaa_bank.service_recharge_retrait.model.RechargeRetrait;
import com.jamaa_bank.service_recharge_retrait.service.RechargeRetraitService;

@Controller
public class RechargeRetraitQueryResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(RechargeRetraitQueryResolver.class);

    @Autowired
    private RechargeRetraitService rechargeRetraitService;

    @QueryMapping
    public List<RechargeRetrait> getAllOperations() {
        logger.info("Query getAllOperations appelée");
        
        try {
            List<RechargeRetrait> operations = rechargeRetraitService.getAllOperations();
            logger.info("Récupération de {} opérations", operations.size());
            return operations;
        } catch (Exception e) {
            logger.error("Erreur lors de la query getAllOperations: {}", e.getMessage(), e);
            throw e;
        }
    }

    @QueryMapping
    public List<RechargeRetrait> getOperationsByAccount(@Argument Long accountId) {
        logger.info("Query getOperationsByAccount appelée avec accountId: {}", accountId);
        
        try {
            List<RechargeRetrait> operations = rechargeRetraitService.getOperationsByAccount(accountId);
            logger.info("Récupération de {} opérations pour le compte {}", operations.size(), accountId);
            return operations;
        } catch (Exception e) {
            logger.error("Erreur lors de la query getOperationsByAccount: {}", e.getMessage(), e);
            throw e;
        }
    }

    @QueryMapping
    public List<RechargeRetrait> getOperationsByCard(@Argument Long cardId) {
        logger.info("Query getOperationsByCard appelée avec cardId: {}", cardId);
        
        try {
            List<RechargeRetrait> operations = rechargeRetraitService.getOperationsByCard(cardId);
            logger.info("Récupération de {} opérations pour la carte {}", operations.size(), cardId);
            return operations;
        } catch (Exception e) {
            logger.error("Erreur lors de la query getOperationsByCard: {}", e.getMessage(), e);
            throw e;
        }
    }

    @QueryMapping
    public List<RechargeRetrait> getOperationsByType(@Argument OperationType operationType) {
        logger.info("Query getOperationsByType appelée avec operationType: {}", operationType);
        
        try {
            List<RechargeRetrait> operations = rechargeRetraitService.getOperationsByType(operationType);
            logger.info("Récupération de {} opérations de type {}", operations.size(), operationType);
            return operations;
        } catch (Exception e) {
            logger.error("Erreur lors de la query getOperationsByType: {}", e.getMessage(), e);
            throw e;
        }
    }

    @QueryMapping
    public List<RechargeRetrait> getRecharges() {
        logger.info("Query getRecharges appelée");
        
        try {
            List<RechargeRetrait> recharges = rechargeRetraitService.getOperationsByType(OperationType.RECHARGE);
            logger.info("Récupération de {} recharges", recharges.size());
            return recharges;
        } catch (Exception e) {
            logger.error("Erreur lors de la query getRecharges: {}", e.getMessage(), e);
            throw e;
        }
    }

    @QueryMapping
    public List<RechargeRetrait> getRetraits() {
        logger.info("Query getRetraits appelée");
        
        try {
            List<RechargeRetrait> retraits = rechargeRetraitService.getOperationsByType(OperationType.RETRAIT);
            logger.info("Récupération de {} retraits", retraits.size());
            return retraits;
        } catch (Exception e) {
            logger.error("Erreur lors de la query getRetraits: {}", e.getMessage(), e);
            throw e;
        }
    }
}

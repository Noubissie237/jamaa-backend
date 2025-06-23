package com.jamaa_bank.service_recharge_retrait.resolver;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.jamaa_bank.service_recharge_retrait.dto.RechargeRequest;
import com.jamaa_bank.service_recharge_retrait.dto.RetraitRequest;
import com.jamaa_bank.service_recharge_retrait.model.RechargeRetrait;
import com.jamaa_bank.service_recharge_retrait.service.RechargeRetraitService;

@Controller
public class RechargeRetraitMutationResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(RechargeRetraitMutationResolver.class);

    @Autowired
    private RechargeRetraitService rechargeRetraitService;

    @MutationMapping
    public RechargeRetrait recharge(@Argument Long accountId, @Argument Long cardId, @Argument BigDecimal amount) {
        logger.info("Mutation recharge appelée: accountId={}, cardId={}, amount={}", accountId, cardId, amount);
        
        try {
            RechargeRetrait result = rechargeRetraitService.recharge(accountId, cardId, amount);
            logger.info("Recharge réussie avec ID: {}", result.getId());
            return result;
        } catch (Exception e) {
            logger.error("Erreur lors de la mutation recharge: {}", e.getMessage(), e);
            throw e;
        }
    }

    @MutationMapping
    public RechargeRetrait rechargeWithRequest(@Argument RechargeRequest request) {
        logger.info("Mutation rechargeWithRequest appelée: {}", request);
        
        try {
            RechargeRetrait result = rechargeRetraitService.recharge(
                request.getAccountId(), 
                request.getCardId(), 
                request.getAmount()
            );
            logger.info("Recharge avec request réussie avec ID: {}", result.getId());
            return result;
        } catch (Exception e) {
            logger.error("Erreur lors de la mutation rechargeWithRequest: {}", e.getMessage(), e);
            throw e;
        }
    }

    @MutationMapping
    public RechargeRetrait retrait(@Argument Long cardId, @Argument Long accountId, @Argument BigDecimal amount) {
        logger.info("Mutation retrait appelée: cardId={}, accountId={}, amount={}", cardId, accountId, amount);
        
        try {
            RechargeRetrait result = rechargeRetraitService.retrait(cardId, accountId, amount);
            logger.info("Retrait réussi avec ID: {}", result.getId());
            return result;
        } catch (Exception e) {
            logger.error("Erreur lors de la mutation retrait: {}", e.getMessage(), e);
            throw e;
        }
    }

    @MutationMapping
    public RechargeRetrait retraitWithRequest(@Argument RetraitRequest request) {
        logger.info("Mutation retraitWithRequest appelée: {}", request);
        
        try {
            RechargeRetrait result = rechargeRetraitService.retrait(
                request.getCardId(), 
                request.getAccountId(), 
                request.getAmount()
            );
            logger.info("Retrait avec request réussi avec ID: {}", result.getId());
            return result;
        } catch (Exception e) {
            logger.error("Erreur lors de la mutation retraitWithRequest: {}", e.getMessage(), e);
            throw e;
        }
    }
}

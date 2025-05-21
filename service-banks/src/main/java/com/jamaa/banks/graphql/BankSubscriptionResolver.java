package com.jamaa.banks.graphql;

import com.jamaa.banks.dto.BankSubscriptionDTO;
import com.jamaa.banks.entity.SubscriptionStatus;
import com.jamaa.banks.service.BankSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;

/**
 * Resolver GraphQL pour les opérations sur les souscriptions aux banques.
 * Gère les requêtes et mutations liées aux souscriptions.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class BankSubscriptionResolver {
    private final BankSubscriptionService subscriptionService;

    /**
     * Crée une nouvelle souscription à une banque.
     * @param subscription DTO contenant les informations de la souscription
     * @return DTO de la souscription créée
     */
    @MutationMapping
    public BankSubscriptionDTO subscribeToBank(@Argument @Valid BankSubscriptionDTO subscription) {
        log.info("Mutation GraphQL: création d'une nouvelle souscription pour l'utilisateur {} à la banque {}", 
                subscription.getUserId(), subscription.getBankId());
        return subscriptionService.subscribeToBank(subscription);
    }

    /**
     * Met à jour le statut d'une souscription.
     
     */
    @MutationMapping
    public BankSubscriptionDTO updateSubscriptionStatus(
            @Argument("id") Long id,
            @Argument("status") SubscriptionStatus status) {
        return subscriptionService.updateSubscriptionStatus(id, status);
    }

    /**
     * Récupère toutes les souscriptions d'une banque.
     * @param bankId Identifiant de la banque
     * @return Liste des DTOs des souscriptions
     */
    @QueryMapping
    public List<BankSubscriptionDTO> bankSubscriptions(@Argument Long bankId) {
        log.debug("Requête GraphQL: récupération des souscriptions pour la banque {}", bankId);
        return subscriptionService.getSubscriptionsByBankId(bankId);
    }

    /**
     * Récupère toutes les souscriptions d'un utilisateur.
     * @param userId Identifiant de l'utilisateur
     * @return Liste des DTOs des souscriptions
     */
    @QueryMapping
    public List<BankSubscriptionDTO> userSubscriptions(@Argument Long userId) {
        log.debug("Requête GraphQL: récupération des souscriptions pour l'utilisateur {}", userId);
        return subscriptionService.getSubscriptionsByUserId(userId);
    }

    /**
     * Récupère les souscriptions actives d'une banque.
     * @param bankId Identifiant de la banque
     * @return Liste des DTOs des souscriptions actives
     */
    @QueryMapping
    public List<BankSubscriptionDTO> activeBankSubscriptions(@Argument Long bankId) {
        log.debug("Requête GraphQL: récupération des souscriptions actives pour la banque {}", bankId);
        return subscriptionService.getActiveSubscriptionsByBankId(bankId);
    }
} 
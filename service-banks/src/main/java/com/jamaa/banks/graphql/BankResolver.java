package com.jamaa.banks.graphql;

import com.jamaa.banks.dto.BankDTO;
import com.jamaa.banks.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;

/**
 * Resolver GraphQL pour les opérations sur les banques.
 * Gère les requêtes et mutations liées aux banques.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class BankResolver {
    private final BankService bankService;

    /**
     * Récupère une banque par son identifiant.
     * @param id Identifiant de la banque
     * @return DTO de la banque
     */
    @QueryMapping
    public BankDTO bank(@Argument Long id) {
        log.debug("Requête GraphQL: récupération de la banque {}", id);
        return bankService.getBankById(id);
    }

    /**
     * Récupère la liste de toutes les banques.
     * @return Liste des DTOs des banques
     */
    @QueryMapping
    public List<BankDTO> banks() {
        log.debug("Requête GraphQL: récupération de toutes les banques");
        return bankService.getAllBanks();
    }

    /**
     * Crée une nouvelle banque.
     * @param bank DTO contenant les informations de la banque à créer
     * @return DTO de la banque créée
     */
    @MutationMapping
    public BankDTO createBank(@Argument @Valid BankDTO bank) {
        log.info("Mutation GraphQL: création d'une nouvelle banque");
        return bankService.createBank(bank);
    }

    /**
     * Met à jour une banque existante.
     * @param id Identifiant de la banque à mettre à jour
     * @param bank DTO contenant les nouvelles informations de la banque
     * @return DTO de la banque mise à jour
     */
    @MutationMapping
    public BankDTO updateBank(@Argument Long id, @Argument @Valid BankDTO bank) {
        log.info("Mutation GraphQL: mise à jour de la banque {}", id);
        return bankService.updateBank(id, bank);
    }

    /**
     * Supprime une banque.
     * @param id Identifiant de la banque à supprimer
     * @return true si la suppression a réussi
     */
    @MutationMapping
    public Boolean deleteBank(@Argument Long id) {
        log.info("Mutation GraphQL: suppression de la banque {}", id);
        bankService.deleteBank(id);
        return true;
    }
} 
package com.jamaa.banks.repository;

import com.jamaa.banks.model.entities.Bank;
import com.jamaa.banks.model.enums.BankServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

/**
 * Repository pour la gestion des opérations de persistance des banques.
 * Étend JpaRepository pour hériter des opérations CRUD de base.
 */
@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    
    /**
     * Trouve toutes les banques actives.
     * Une banque active peut accepter de nouvelles souscriptions.
     *
     * @return Liste des banques actives
     */
    List<Bank> findByActiveTrue();

    /**
     * Vérifie si une banque existe avec le nom donné.
     * Utilisé pour garantir l'unicité des noms de banque.
     *
     * @param name Le nom de la banque à vérifier
     * @return true si une banque existe avec ce nom, false sinon
     */
    boolean existsByName(String name);

    /**
     * Recherche des banques selon des critères spécifiques.
     *
     * @param services Liste des services requis
     * @param maxMinimumBalance Solde minimum maximum accepté
     * @return Liste des banques correspondant aux critères
     */
    @Query("SELECT DISTINCT b FROM Bank b " +
           "WHERE (:services IS NULL OR EXISTS (SELECT 1 FROM b.services s WHERE s IN :services)) " +
           "AND (:maxMinimumBalance IS NULL OR b.minimumBalance <= :maxMinimumBalance) " +
           "AND b.active = true")
    List<Bank> searchBanks(
            @Param("services") Set<BankServiceType> services,
            @Param("maxMinimumBalance") Double maxMinimumBalance
    );
} 
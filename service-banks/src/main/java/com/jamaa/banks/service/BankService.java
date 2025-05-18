package com.jamaa.banks.service;

import com.jamaa.banks.dto.BankResponse;
import com.jamaa.banks.dto.CreateBankInput;
import com.jamaa.banks.dto.UpdateBankInput;
import com.jamaa.banks.dto.BankFeeInput;
import com.jamaa.banks.model.entities.Bank;
import com.jamaa.banks.model.entities.BankFee;
import com.jamaa.banks.model.enums.BankServiceType;
import com.jamaa.banks.repository.BankRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

/**
 * Service pour la gestion des banques et leurs services associés.
 * Cette classe gère toutes les opérations liées aux banques, incluant :
 * - La création et mise à jour des banques
 * - La gestion des services bancaires
 */
@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {
    private static final Logger logger = LoggerFactory.getLogger(BankService.class);
    
    private final BankRepository bankRepository;

    /**
     * Crée une nouvelle banque.
     *
     * @param input DTO contenant toutes les informations nécessaires pour créer une banque
     * @return BankResponse contenant les informations de la banque créée
     * @throws IllegalStateException si une banque avec le même nom existe déjà
     */
    @Transactional
    public Bank createBank(@Valid CreateBankInput input) {
        logger.info("Création d'une nouvelle banque : {}", input.getName());
        
        if (bankRepository.existsByName(input.getName())) {
            logger.error("Une banque avec le nom {} existe déjà", input.getName());
            throw new IllegalStateException("Une banque avec ce nom existe déjà");
        }

        Bank bank = new Bank();
        bank.setName(input.getName());
        bank.setDescription(input.getDescription());
        bank.setLogoUrl(input.getLogoUrl());
        bank.setCustomerServicePhone(input.getCustomerServicePhone());
        bank.setCustomerServiceEmail(input.getCustomerServiceEmail());
        bank.setMinimumBalance(input.getMinimumBalance());
        bank.setMinimumBalanceDescription(input.getMinimumBalanceDescription());
        bank.setActive(true);

        // Initialisation des services
        if (input.getServices() != null && !input.getServices().isEmpty()) {
            logger.debug("Ajout de {} services à la banque", input.getServices().size());
            bank.setServices(new HashSet<>(input.getServices()));
        } else {
            logger.warn("Aucun service spécifié pour la banque {}", input.getName());
            bank.setServices(new HashSet<>());
        }

        // Sauvegarde initiale de la banque
        try {
            bank = bankRepository.save(bank);
            logger.debug("Banque sauvegardée avec l'ID : {}", bank.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde de la banque : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la création de la banque", e);
        }

        // Ajout des frais si présents
        if (input.getFees() != null && !input.getFees().isEmpty()) {
            logger.debug("Ajout de {} frais à la banque", input.getFees().size());
            try {
                for (BankFeeInput feeInput : input.getFees()) {
                    BankFee fee = convertToBankFee(feeInput);
                    fee.setBank(bank);
                    bank.getFees().add(fee);
                }
                // Sauvegarde finale avec les frais
                bank = bankRepository.save(bank);
                logger.debug("Frais bancaires sauvegardés avec succès");
            } catch (Exception e) {
                logger.error("Erreur lors de l'ajout des frais : {}", e.getMessage());
                throw new RuntimeException("Erreur lors de l'ajout des frais bancaires", e);
            }
        }

        return bank;
    }

    /**
     * Met à jour une banque existante.
     *
     * @param id Identifiant de la banque
     * @param input DTO contenant les informations à mettre à jour
     * @return BankResponse contenant les informations mises à jour
     * @throws EntityNotFoundException si la banque n'est pas trouvée
     */
    @Transactional
    public Bank updateBank(Long id, @Valid UpdateBankInput input) {
        logger.info("Mise à jour de la banque : {}", id);
        
        Bank bank = getBank(id);

        if (input.getName() != null) bank.setName(input.getName());
        if (input.getDescription() != null) bank.setDescription(input.getDescription());
        if (input.getLogoUrl() != null) bank.setLogoUrl(input.getLogoUrl());
        if (input.getCustomerServicePhone() != null) bank.setCustomerServicePhone(input.getCustomerServicePhone());
        if (input.getCustomerServiceEmail() != null) bank.setCustomerServiceEmail(input.getCustomerServiceEmail());
        if (input.getMinimumBalance() != null) bank.setMinimumBalance(input.getMinimumBalance());
        if (input.getMinimumBalanceDescription() != null) bank.setMinimumBalanceDescription(input.getMinimumBalanceDescription());
        if (input.getActive() != null) bank.setActive(input.getActive());

        // Mettre à jour les services si fournis
        if (input.getServices() != null) {
            bank.getServices().clear();
            bank.getServices().addAll(input.getServices());
        }

        // Mettre à jour les frais si fournis
        if (input.getFees() != null) {
            bank.getFees().clear();
            input.getFees().stream()
                .map(this::convertToBankFee)
                .peek(fee -> fee.setBank(bank))
                .forEach(fee -> bank.getFees().add(fee));
        }

        return bankRepository.save(bank);
    }

    /**
     * Ajoute un nouveau frais bancaire.
     */
    @Transactional
    public Bank addBankFee(Long bankId, @Valid BankFeeInput input) {
        logger.info("Ajout d'un nouveau frais pour la banque : {}", bankId);
        
        Bank bank = getBank(bankId);
        BankFee fee = convertToBankFee(input);
        fee.setBank(bank);
        bank.getFees().add(fee);
        
        return bankRepository.save(bank);
    }

    /**
     * Met à jour un frais bancaire existant.
     */
    @Transactional
    public Bank updateBankFee(Long bankId, Long feeId, @Valid BankFeeInput input) {
        logger.info("Mise à jour du frais {} pour la banque {}", feeId, bankId);
        
        Bank bank = getBank(bankId);
        bank.getFees().stream()
            .filter(fee -> fee.getId().equals(feeId))
            .findFirst()
            .ifPresent(fee -> {
                fee.setType(input.getType());
                fee.setAmount(input.getAmount());
                fee.setFrequency(input.getFrequency());
                fee.setDescription(input.getDescription());
            });
        
        return bankRepository.save(bank);
    }

    /**
     * Supprime un frais bancaire.
     */
    @Transactional
    public Bank removeBankFee(Long bankId, Long feeId) {
        logger.info("Suppression du frais {} de la banque {}", feeId, bankId);
        
        Bank bank = getBank(bankId);
        bank.getFees().removeIf(fee -> fee.getId().equals(feeId));
        
        return bankRepository.save(bank);
    }

    /**
     * Met à jour plusieurs frais bancaires en une seule opération.
     */
    @Transactional
    public Bank updateBankFees(Long bankId, List<@Valid BankFeeInput> inputs) {
        logger.info("Mise à jour en masse des frais pour la banque {}", bankId);
        
        Bank bank = getBank(bankId);
        bank.getFees().clear();
        
        List<BankFee> fees = inputs.stream()
            .map(this::convertToBankFee)
            .peek(fee -> fee.setBank(bank))
            .collect(Collectors.toList());
        
        bank.getFees().addAll(fees);
        return bankRepository.save(bank);
    }

    /**
     * Récupère une banque par son ID.
     *
     * @param id Identifiant de la banque
     * @return BankResponse contenant les informations de la banque
     * @throws EntityNotFoundException si la banque n'est pas trouvée
     */
    public Bank getBank(Long id) {
        logger.debug("Recherche de la banque avec l'ID : {}", id);
        return bankRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Banque non trouvée avec l'ID : {}", id);
                    return new EntityNotFoundException("Banque non trouvée");
                });
    }

    /**
     * Récupère toutes les banques.
     *
     * @return Liste des banques
     */
    public List<Bank> getAllBanks() {
        return bankRepository.findAll();
    }

    /**
     * Récupère toutes les banques actives.
     */
    public List<BankResponse> getActiveBanks() {
        logger.debug("Récupération des banques actives");
        return bankRepository.findByActiveTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Active ou désactive une banque.
     *
     * @param id Identifiant de la banque
     * @return BankResponse contenant les informations mises à jour
     * @throws EntityNotFoundException si la banque n'est pas trouvée
     */
    @Transactional
    public Bank toggleBankStatus(Long id) {
        Bank bank = getBank(id);
        bank.setActive(!bank.isActive());
        return bankRepository.save(bank);
    }

    /**
     * Recherche des banques selon des critères spécifiques.
     */
    public List<BankResponse> searchBanks(
            Set<BankServiceType> services,
            Double maxMinimumBalance
    ) {
        logger.debug("Recherche de banques avec critères - services: {}, maxMinimumBalance: {}", services, maxMinimumBalance);
        return bankRepository.searchBanks(services, maxMinimumBalance).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une entité Bank en DTO BankResponse.
     *
     * @param bank Entité à convertir
     * @return BankResponse contenant les informations formatées
     */
    private BankResponse convertToResponse(Bank bank) {
        return BankResponse.builder()
                .id(bank.getId())
                .name(bank.getName())
                .description(bank.getDescription())
                .logoUrl(bank.getLogoUrl())
                .customerServicePhone(bank.getCustomerServicePhone())
                .customerServiceEmail(bank.getCustomerServiceEmail())
                .services(bank.getServices())
                .minimumBalance(bank.getMinimumBalance())
                .minimumBalanceDescription(bank.getMinimumBalanceDescription())
                .active(bank.isActive())
                .createdAt(bank.getCreatedAt())
                .updatedAt(bank.getUpdatedAt())
                .build();
    }

    private BankFee convertToBankFee(BankFeeInput input) {
        BankFee fee = new BankFee();
        fee.setType(input.getType());
        fee.setAmount(input.getAmount());
        fee.setFrequency(input.getFrequency());
        fee.setDescription(input.getDescription());
        return fee;
    }
} 
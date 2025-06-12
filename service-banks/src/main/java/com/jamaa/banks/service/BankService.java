package com.jamaa.banks.service;

import com.jamaa.banks.dto.BankDTO;
import com.jamaa.banks.entity.Bank;
import com.jamaa.banks.exception.CustomException;
import com.jamaa.banks.messaging.BankInfoProducer;
import com.jamaa.banks.repository.BankRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BankService {

    private final BankInfoProducer bankInfoProducer;
    private final BankRepository bankRepository;

   

    @Transactional
    public BankDTO createBank(@Valid BankDTO bankDTO) {
        log.info("Création d'une nouvelle banque: {}", bankDTO.getName());
        
        validateBankUniqueness(bankDTO);
        
        Bank bank = new Bank();
        updateBankFromDTO(bank, bankDTO);
        Bank savedBank = bankRepository.save(bank);
        log.info("Banque créée avec succès, id: {}", savedBank.getId());

        // Publier l'événement
        bankInfoProducer.sendBankCreatedEvent(savedBank);
        
        return convertToDTO(savedBank);
    }

    @Transactional
    public BankDTO updateBank(Long id, @Valid BankDTO bankDTO) {
        log.info("Mise à jour de la banque avec l'id: {}", id);
        
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Banque", id));
        
        validateBankUniquenessForUpdate(bankDTO, id);
        
        updateBankFromDTO(bank, bankDTO);
        Bank updatedBank = bankRepository.save(bank);
        log.info("Banque mise à jour avec succès, id: {}", id);
        
        return convertToDTO(updatedBank);
    }

    @Transactional
    public void deleteBank(Long id) {
        log.info("Suppression de la banque avec l'id: {}", id);
        
        if (!bankRepository.existsById(id)) {
            throw CustomException.notFound("Banque", id);
        }
        
        bankRepository.deleteById(id);
        log.info("Banque supprimée avec succès, id: {}", id);
    }

    public BankDTO getBankById(Long id) {
        log.debug("Recherche de la banque avec l'id: {}", id);
        
        return bankRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> CustomException.notFound("Banque", id));
    }

    public List<BankDTO> getAllBanks() {
        log.debug("Récupération de toutes les banques");
        
        return bankRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validateBankUniqueness(BankDTO bankDTO) {
        if (bankRepository.existsByName(bankDTO.getName())) {
            throw CustomException.alreadyExists("Banque", "nom", bankDTO.getName());
        }
        if (bankDTO.getSlogan() != null && bankRepository.existsBySlogan(bankDTO.getSlogan())) {
            throw CustomException.alreadyExists("Banque", "slogan", bankDTO.getSlogan());
        }
    }

    private void validateBankUniquenessForUpdate(BankDTO bankDTO, Long currentBankId) {
        bankRepository.findByName(bankDTO.getName())
                .ifPresent(bank -> {
                    if (!bank.getId().equals(currentBankId)) {
                        throw CustomException.alreadyExists("Banque", "nom", bankDTO.getName());
                    }
                });

        if (bankDTO.getSlogan() != null) {
            bankRepository.findBySlogan(bankDTO.getSlogan())
                    .ifPresent(bank -> {
                        if (!bank.getId().equals(currentBankId)) {
                            throw CustomException.alreadyExists("Banque", "slogan", bankDTO.getSlogan());
                        }
                    });
        }
    }

    private BankDTO convertToDTO(Bank bank) {
        BankDTO dto = new BankDTO();
        dto.setId(bank.getId());
        dto.setName(bank.getName());
        dto.setSlogan(bank.getSlogan());
        dto.setLogoUrl(bank.getLogoUrl());
        dto.setCreatedAt(bank.getCreatedAt());
        dto.setUpdatedAt(bank.getUpdatedAt());
        dto.setMinimumBalance(bank.getMinimumBalance());
        dto.setWithdrawFees(bank.getWithdrawFees());
        dto.setInternalTransferFees(bank.getInternalTransferFees());
        dto.setExternalTransferFees(bank.getExternalTransferFees());
        dto.setIsActive(bank.getIsActive());
        return dto;
    }

    private void updateBankFromDTO(Bank bank, BankDTO dto) {
        bank.setName(dto.getName());
        bank.setSlogan(dto.getSlogan());
        bank.setLogoUrl(dto.getLogoUrl());
        bank.setMinimumBalance(dto.getMinimumBalance());
        bank.setWithdrawFees(dto.getWithdrawFees());
        bank.setInternalTransferFees(dto.getInternalTransferFees());
        bank.setExternalTransferFees(dto.getExternalTransferFees());
        bank.setIsActive(dto.getIsActive());
    }
} 
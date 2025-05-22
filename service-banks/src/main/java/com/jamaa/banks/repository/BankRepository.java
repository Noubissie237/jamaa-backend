package com.jamaa.banks.repository;

import com.jamaa.banks.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findByName(String name);
    Optional<Bank> findBySlogan(String slogan);
    boolean existsByName(String name);
    boolean existsBySlogan(String slogan);
} 
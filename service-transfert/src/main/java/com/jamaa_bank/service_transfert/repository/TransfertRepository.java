package com.jamaa_bank.service_transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jamaa_bank.service_transfert.model.Transfert;

public interface TransfertRepository extends JpaRepository<Transfert, Long>{
    
}

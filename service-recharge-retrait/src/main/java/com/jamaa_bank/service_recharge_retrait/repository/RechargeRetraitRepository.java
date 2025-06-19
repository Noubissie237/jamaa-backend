package com.jamaa_bank.service_recharge_retrait.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jamaa_bank.service_recharge_retrait.model.OperationType;
import com.jamaa_bank.service_recharge_retrait.model.RechargeRetrait;
import com.jamaa_bank.service_recharge_retrait.model.TransactionStatus;

@Repository
public interface RechargeRetraitRepository extends JpaRepository<RechargeRetrait, Long> {
    
    List<RechargeRetrait> findByAccountId(Long accountId);
    
    List<RechargeRetrait> findByCardId(Long cardId);
    
    List<RechargeRetrait> findByOperationType(OperationType operationType);
    
    List<RechargeRetrait> findByStatus(TransactionStatus status);
    
    @Query("SELECT r FROM RechargeRetrait r WHERE r.accountId = :accountId AND r.operationType = :operationType")
    List<RechargeRetrait> findByAccountIdAndOperationType(@Param("accountId") Long accountId, @Param("operationType") OperationType operationType);
    
    @Query("SELECT r FROM RechargeRetrait r WHERE r.cardId = :cardId AND r.operationType = :operationType")
    List<RechargeRetrait> findByCardIdAndOperationType(@Param("cardId") Long cardId, @Param("operationType") OperationType operationType);
}

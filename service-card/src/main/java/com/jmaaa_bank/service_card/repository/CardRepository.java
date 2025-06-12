package com.jmaaa_bank.service_card.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jmaaa_bank.service_card.enums.CardStatus;
import com.jmaaa_bank.service_card.model.Card;

@Repository
public interface CardRepository extends JpaRepository<Card , Long>{

    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findByCustomerId(Long customerId);
    List<Card> findByCustomerIdAndStatus(Long customerId , CardStatus status);


    @Query("SELECT c FROM Card c WHERE c.customerId = :customerId AND c.status = :status")
    List<Card> findActiveCardsByCustomer(@Param("customerId") Long customerId, 
                                       @Param("status") CardStatus status);


    boolean existsByCardNumber(String cardNumber);

    long countByCustomerId(Long customerId);




    
}
 
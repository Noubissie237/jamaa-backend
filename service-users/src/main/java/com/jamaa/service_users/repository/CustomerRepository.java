package com.jamaa.service_users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jamaa.service_users.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    public Optional<Customer> findByEmail(String email);
}

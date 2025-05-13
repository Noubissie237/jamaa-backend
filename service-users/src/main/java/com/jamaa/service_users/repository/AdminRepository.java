package com.jamaa.service_users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jamaa.service_users.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}

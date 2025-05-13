package com.jamaa.service_users.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jamaa.service_users.model.SuperAdmin;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
}

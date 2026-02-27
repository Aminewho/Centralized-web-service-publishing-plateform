package com.rne.apiCatalog.v_2_0.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rne.apiCatalog.v_2_0.entity.UserAppEntity;

public interface UserAppRepository extends JpaRepository<UserAppEntity, Long> {
    // Additional query methods can be added here if needed
}
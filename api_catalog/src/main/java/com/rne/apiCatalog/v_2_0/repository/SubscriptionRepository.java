package com.rne.apiCatalog.v_2_0.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rne.apiCatalog.v_2_0.entity.SubscriptionEntity;


@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, String> {
    // Recherche standard par ID (héritée de JpaRepository)
}
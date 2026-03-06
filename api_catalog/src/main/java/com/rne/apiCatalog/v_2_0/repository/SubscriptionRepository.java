package com.rne.apiCatalog.v_2_0.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rne.apiCatalog.v_2_0.entity.SubscriptionEntity;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, String> {
    // Recherche standard par ID (héritée de JpaRepository)
Optional<SubscriptionEntity> findByApiNameAndApplicationNameAndActiveTrue(String apiName, String applicationName);
// Trouve toutes les souscriptions actives pour une application donnée
// Récupère toutes les souscriptions actives pour une application (par son nom)
    List<SubscriptionEntity> findByApplicationNameAndActiveTrue(String applicationName);

    // Récupère toutes les souscriptions inactives pour une application (par son nom)
    List<SubscriptionEntity> findByApplicationNameAndActiveFalse(String applicationName);
    List<SubscriptionEntity> findByApiNameAndActiveTrue(String apiName);

}

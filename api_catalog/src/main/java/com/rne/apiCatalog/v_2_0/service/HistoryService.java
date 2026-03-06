package com.rne.apiCatalog.v_2_0.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rne.apiCatalog.v_2_0.entity.HistoryEntity;
import com.rne.apiCatalog.v_2_0.repository.HistoryRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class HistoryService {
    @Autowired
    private HistoryRepository repository;

    public List<HistoryEntity> searchLogs(String appName, 
                                          String apiName, 
                                          String subscriptionId, 
                                          LocalDateTime startDate, 
                                          LocalDateTime endDate) {
                                          
        return repository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtre par Nom d'Application
            if (appName != null && !appName.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("applicationName"), appName));
            }

            // 2. Filtre par Nom d'API
            if (apiName != null && !apiName.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("apiName"), apiName));
            }

            // 3. NOUVEAU : Filtre par Subscription ID
            // Très utile pour isoler la consommation d'un cycle de vie spécifique
            if (subscriptionId != null && !subscriptionId.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("subscriptionId"), subscriptionId));
            }

            // 4. Filtre par Date de début
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("requestDate"), startDate));
            }

            // 5. Filtre par Date de fin
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("requestDate"), endDate));
            }

            // Tri par date décroissante par défaut pour voir les derniers appels en premier
            query.orderBy(criteriaBuilder.desc(root.get("requestDate")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
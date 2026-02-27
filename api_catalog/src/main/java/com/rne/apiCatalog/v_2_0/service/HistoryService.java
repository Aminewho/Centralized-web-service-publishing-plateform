package com.rne.apiCatalog.v_2_0.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.rne.apiCatalog.v_2_0.entity.HistoryEntity;
import com.rne.apiCatalog.v_2_0.repository.HistoryRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class HistoryService {

    @Autowired
    private HistoryRepository repository;

    public List<HistoryEntity> searchLogs(String appName, String apiName, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findAll((Specification<HistoryEntity>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtre par Nom d'Application
            if (appName != null && !appName.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("applicationName"), appName));
            }

            // Filtre par Nom d'API
            if (apiName != null && !apiName.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("apiName"), apiName));
            }

            // Filtre par Date de début (supérieur ou égal)
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("requestDate"), startDate));
            }

            // Filtre par Date de fin (inférieur ou égal)
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("requestDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
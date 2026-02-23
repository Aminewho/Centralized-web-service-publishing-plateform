package com.rne.apiCatalog.v_2_0.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rne.apiCatalog.v_2_0.entity.HistoryEntity;

public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {
    Optional<HistoryEntity> findByCorrelationId(String correlationId);
    List<HistoryEntity> findByApplicationName(String applicationName);

    // Filtre par nom d'API
    List<HistoryEntity> findByApiName(String apiName);

    // Filtre par date (si ton entité possède un champ requestDate)
    List<HistoryEntity> findByRequestDateBetween(LocalDateTime start, LocalDateTime end);

    // Recherche multicritères (Ex: App + API)
    List<HistoryEntity> findByApplicationNameAndApiName(String appName, String apiName);
}
package com.rne.apiCatalog.v_2_0.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.rne.apiCatalog.v_2_0.entity.HistoryEntity;
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long>, JpaSpecificationExecutor<HistoryEntity> { 
    List<HistoryEntity> findBySubscriptionIdOrderByRequestDateDesc(String subscriptionId);
}
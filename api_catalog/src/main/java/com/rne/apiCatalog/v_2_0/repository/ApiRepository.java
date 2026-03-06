package com.rne.apiCatalog.v_2_0.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rne.apiCatalog.v_2_0.entity.ApiEntity;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.rne.apiCatalog.v_2_0.entity.ApiOperationEntity;
public interface ApiRepository extends JpaRepository<ApiEntity, String> {
    // Additional query methods can be added here if needed
    @Query("SELECT a.policies FROM ApiEntity a WHERE a.name = :apiName")
    List<String> findPoliciesByApiName(@Param("apiName") String apiName);
   @Query("SELECT a.operations FROM ApiEntity a WHERE a.name = :apiName")
    List<ApiOperationEntity> findOperationsByApiName(@Param("apiName") String apiName);

    @Query("SELECT a FROM ApiEntity a WHERE a.name = :apiName")
    java.util.Optional<ApiEntity> findByApiName(@Param("apiName") String apiName);
}
package com.rne.apiCatalog.v_2_0.entity;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;

import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "api_operations")
@Getter @Setter
public class ApiOperationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String target;
    private String verb;
    private String authType;
    private String throttlingPolicy;
    // Note: Pour les OperationPolicies complexes, on peut soit rajouter une table, 
    // soit s'arrêter là si tu n'as pas besoin de les requêter en SQL.
}
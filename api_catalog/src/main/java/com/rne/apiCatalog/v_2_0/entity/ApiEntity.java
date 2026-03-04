package com.rne.apiCatalog.v_2_0.entity;    
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.rne.apiCatalog.v_2_0.DTOs.ApiRequestDto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
    
import jakarta.persistence.Embedded;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;
@Entity
@Table(name = "apis")
@Getter @Setter
public class ApiEntity {
    @Id
    private String id;
@Column(unique = true, nullable = false)   
 private String name;
    private String context;
    private String version;
    private String description;
    private String status;
    private String apiThrottlingPolicy;

    // --- OBJETS IMBRIQUÉS (Colonnes dans la même table) ---
    @Embedded
    private EndpointConfigColumns endpointConfig;


    // --- LISTES DE CHAINES (Tables de jointure automatiques) ---
    @ElementCollection
    @CollectionTable(name = "api_transports", joinColumns = @JoinColumn(name = "api_id"))
    private List<String> transport;

    @ElementCollection
    @CollectionTable(name = "api_policies", joinColumns = @JoinColumn(name = "api_id"))
    private List<String> policies;

    // --- LISTE D'OBJETS COMPLEXES (Table dédiée) ---
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "api_id")
    private List<ApiOperationEntity> operations;

    private LocalDateTime onboardedAt;
    
}
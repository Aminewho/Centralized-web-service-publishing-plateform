package com.rne.apiCatalog.v_2_0.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscriptions")
@Getter @Setter
@NoArgsConstructor
public class SubscriptionEntity {

    @Id
    private String id; // UUID de WSO2

    private String applicationId;
    private String apiId;
    private String throttlingPolicy;
    private int requestCount = 0;
    private int maxRequestLimit;
    
    // Le statut textuel de WSO2 (ex: "UNBLOCKED", "BLOCKED")
    private String status; 

    // Ton nouvel attribut : est-ce que l'abonnement est utilisable ?
    private boolean active; 

    private LocalDateTime subscriptionDate;
}
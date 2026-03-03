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
    private String id; 

    private String applicationId;
    private String apiId;
    private String throttlingPolicy;
    private int requestCount = 0;
    private int maxRequestLimit;
    private boolean active; 

    private LocalDateTime subscriptionDate;
    private LocalDateTime expirationDate; // Sera NULL tant que le quota n'est pas atteint
}
package com.rne.apiCatalog.v_2_0.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscriptions", uniqueConstraints = {
    @UniqueConstraint(
        name = "UK_APP_API_ACTIVE", 
        columnNames = {"applicationName", "apiName", "active"}
    )
})@Getter @Setter
@NoArgsConstructor
public class SubscriptionEntity {

    @Id
    private String id; 
    @Column(nullable = false)
        private String applicationName; // Doit correspondre à UserAppEntity.applicationName

    @Column(nullable = false)
    private String apiName;
    private String applicationId;
    private String apiId;
    private String throttlingPolicy;
    private int requestCount = 0;
    private int maxRequestLimit;
@Column(nullable = false)
    private boolean active = true;
    private LocalDateTime subscriptionDate;
    private LocalDateTime expirationDate; // Sera NULL tant que le quota n'est pas atteint
}
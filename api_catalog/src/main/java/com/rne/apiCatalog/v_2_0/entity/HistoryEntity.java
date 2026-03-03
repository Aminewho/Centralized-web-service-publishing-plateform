package com.rne.apiCatalog.v_2_0.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "API_CONSUMPTION_HISTORY")
public class HistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Oracle gérera via une séquence
    private Long id;

    private String applicationName;
    private String apiName;
    private String subscriptionId; // Pour faire le lien avec la souscription
    private String verb;
    private String path;
    private String status; // PENDING, SUCCESS, ERROR
    @Column(name = "REQUEST_DATE")
    private LocalDateTime requestDate;
    @Column(name = "CORRELATION_ID", unique = true)
    private String correlationId;
private Long duration; // Durée en millisecondes
    // Constructeurs
    public HistoryEntity() {}

    public HistoryEntity(String applicationName, String apiName, String verb) {
        this.applicationName = applicationName;
        this.apiName = apiName;
        this.verb = verb;
    }
@PrePersist
    protected void onCreate() {
        if (this.requestDate == null) {
            this.requestDate = LocalDateTime.now();
        }
    }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public String getCorrelationId() {
        return correlationId;
    }
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }
public String getSubscriptionId() {
        return subscriptionId;
    }
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }
public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
    
    public String getPath() {
        return path;
    }   
    public void setPath(String path) {
        this.path = path;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

  
}
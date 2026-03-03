package com.rne.apiCatalog.v_2_0.service;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rne.apiCatalog.v_2_0.entity.HistoryEntity;
import com.rne.apiCatalog.v_2_0.repository.HistoryRepository;
import com.rne.apiCatalog.v_2_0.repository.SubscriptionRepository;


@Service
public class LogConsumer {

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "api_logs")
@Transactional
public void receiveLog(String message) {
    try {
        Map<String, Object> payload = objectMapper.readValue(message, Map.class);
        
        // 1. Enregistrement dans l'historique avec le SUB ID
        HistoryEntity history = new HistoryEntity();
        history.setApiName((String) payload.get("apiName"));
        history.setStatus((String) payload.get("status"));
        history.setDuration(Long.valueOf(payload.get("duration").toString()));
        history.setVerb((String) payload.get("verb"));
        history.setPath((String) payload.get("path"));
        history.setApplicationName((String) payload.get("applicationName"));
        history.setCorrelationId((String) payload.get("correlationId"));
        
        // On récupère le subId envoyé par le médiateur WSO2
        String subId = (String) payload.get("subscriptionId"); 
        history.setSubscriptionId(subId); 
        
        historyRepository.save(history);

        // 2. Logique de Quota et Validité
        if (subId != null) {
            subscriptionRepository.findById(subId).ifPresent(sub -> {
                LocalDateTime now = LocalDateTime.now();

                // Vérification Quota OU Expiration
                if (sub.isActive()) {
                    boolean expired = sub.getExpirationDate() != null && now.isAfter(sub.getExpirationDate());
                    boolean quotaReached = sub.getRequestCount() >= sub.getMaxRequestLimit();

                    if (expired || quotaReached) {
                        sub.setActive(false);
                    } else {
                        sub.setRequestCount(sub.getRequestCount() + 1);
                    }
                    subscriptionRepository.save(sub);
                }
            });
        }
        
    } catch (Exception e) {
}
}}
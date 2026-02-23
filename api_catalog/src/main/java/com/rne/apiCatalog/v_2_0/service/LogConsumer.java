package com.rne.apiCatalog.v_2_0.service;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rne.apiCatalog.v_2_0.entity.HistoryEntity;
import com.rne.apiCatalog.v_2_0.repository.HistoryRepository;

@Service
public class LogConsumer {

    @Autowired
    private HistoryRepository historyRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "api_logs")
    public void receiveLog(String message) {
        try {
            // Désérialisation du JSON reçu de WSO2
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            
            HistoryEntity entity = new HistoryEntity();
            entity.setApiName((String) payload.get("apiName"));
            entity.setStatus((String) payload.get("status"));
            entity.setDuration(Long.valueOf(payload.get("duration").toString()));
            entity.setVerb((String) payload.get("verb"));
            entity.setPath((String) payload.get("path"));
            entity.setApplicationName((String) payload.get("applicationName"));
            entity.setCorrelationId((String) payload.get("correlationId"));
            historyRepository.save(entity);
            System.out.println("Log enregistré depuis RabbitMQ !");
            
        } catch (Exception e) {
            System.err.println("Erreur de traitement du log : " + e.getMessage());
        }
    }
}
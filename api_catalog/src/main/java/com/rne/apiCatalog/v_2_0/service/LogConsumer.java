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
        String appName = (String) payload.get("applicationName");
        String apiName = (String) payload.get("apiName");

        // Recherche de la SEULE souscription active
        subscriptionRepository.findByApiNameAndApplicationNameAndActiveTrue(apiName, appName)
            .ifPresentOrElse(sub -> {
                // On met à jour le quota de la souscription actuelle
                sub.setRequestCount(sub.getRequestCount() + 1);
                
                if (sub.getRequestCount() >= sub.getMaxRequestLimit()) {
                    sub.setActive(false); // On désactive car quota atteint
                    sub.setExpirationDate(LocalDateTime.now());
                }
                subscriptionRepository.save(sub);
                
                // Sauvegarde historique liée à l'ID technique de la souscription active
                saveHistory(payload, sub.getId());
                
            }, () -> {
                // Cas où l'app a été supprimée de WSO2 mais le log arrive encore (ou désynchronisation)
                saveHistory(payload, null);
            });

    } catch (Exception e) {
    }
}

private void saveHistory(Map<String, Object> payload, String subId) {
    HistoryEntity history = new HistoryEntity();
    history.setApiName((String) payload.get("apiName"));
    history.setApplicationName((String) payload.get("applicationName"));
    history.setSubscriptionId(subId); // Sera null si aucune souscription active trouvée
    // ... autres champs ...
    historyRepository.save(history);
}
}
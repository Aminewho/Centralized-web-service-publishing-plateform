package com.rne.apiCatalog.v_2_0.service;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rne.apiCatalog.v_2_0.entity.HistoryEntity;
import com.rne.apiCatalog.v_2_0.repository.HistoryRepository;
import com.rne.apiCatalog.v_2_0.repository.SubscriptionRepository;


@Service
public class LogConsumer {

    private final Wso2AuthService authService;
    private final HistoryRepository historyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wso2.base-url}") // Correction : ajout de la fermeture '}'
    private String wso2BaseUrl;

    private static final String DEVPORTAL_PATH = "/api/am/devportal/v3.3";

    // Utilisation de l'injection par constructeur (recommandé)
    public LogConsumer(Wso2AuthService authService, 
                       HistoryRepository historyRepository, 
                       SubscriptionRepository subscriptionRepository, 
                       RestClient.Builder restClientBuilder,
                       @Value("${wso2.base-url}") String wso2BaseUrl) {
        this.authService = authService;
        this.historyRepository = historyRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.restClient = restClientBuilder.baseUrl(wso2BaseUrl).build();
        this.wso2BaseUrl = wso2BaseUrl;
    }

    private String getDevPortalPath() {
        return wso2BaseUrl + DEVPORTAL_PATH;
    }

    @RabbitListener(queues = "api_logs")
    @Transactional
    public void receiveLog(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            String appName = (String) payload.get("applicationName");
            String apiName = (String) payload.get("apiName");
            
            System.out.println("Processing log - API: " + apiName + " | App: " + appName);

            subscriptionRepository.findByApiNameAndApplicationNameAndActiveTrue(apiName, appName)
                .ifPresentOrElse(sub -> {
                    // 1. Incrémentation
                    sub.setRequestCount(sub.getRequestCount() + 1);
                    
                    // 2. Vérification du Quota
                    if (sub.getRequestCount() >= sub.getMaxRequestLimit()) {
                        System.out.println("!!! Quota atteint pour " + appName + ". Désactivation en cours...");
                        
                        sub.setActive(false);
                        sub.setExpirationDate(LocalDateTime.now());
                        
                        // APPEL DE SUPPRESSION WSO2
                        deleteSubscriptionFromWso2(sub.getId());
                    }
                    
                    // 3. Sauvegarde de l'état
                    subscriptionRepository.save(sub);
                    saveHistory(payload, sub.getId());
                    
                }, () -> {
                    // Cas où aucune souscription active n'est trouvée (déjà supprimée ou inconnue)
                    saveHistory(payload, null);
                });

        } catch (Exception e) {
            System.err.println("Erreur critique lors du traitement du log: " + e.getMessage());
        }
    }

    private void deleteSubscriptionFromWso2(String subscriptionId) {
        try {
            String token = authService.getAccessToken();
            
            restClient.delete()
                    .uri(getDevPortalPath() + "/subscriptions/" + subscriptionId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();

            System.out.println("SUCCÈS : Souscription " + subscriptionId + " supprimée du DevPortal WSO2.");
        } catch (Exception e) {
            // On log l'erreur mais on ne bloque pas la transaction de la base de données
            System.err.println("ÉCHEC de suppression WSO2 pour l'ID " + subscriptionId + " : " + e.getMessage());
        }
    }

    private void saveHistory(Map<String, Object> payload, String subId) {
        try {
            HistoryEntity history = new HistoryEntity();
            history.setApiName((String) payload.get("apiName"));
            history.setApplicationName((String) payload.get("applicationName"));
            history.setSubscriptionId(subId);
            
            // Gestion sécurisée de la durée (évite les NullPointerException)
            Object duration = payload.get("duration");
            history.setDuration(duration != null ? Long.valueOf(duration.toString()) : 0L);
            
            history.setVerb((String) payload.get("verb"));
            history.setPath((String) payload.get("path"));
            history.setCorrelationId((String) payload.get("correlationId"));
            history.setStatus(String.valueOf(payload.get("status")));
            
            historyRepository.save(history);
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde de l'historique : " + e.getMessage());
        }
    }
}
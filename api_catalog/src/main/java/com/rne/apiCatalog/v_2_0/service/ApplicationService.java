package com.rne.apiCatalog.v_2_0.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.rne.apiCatalog.v_2_0.DTOs.AppDetailsDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApplicationBriefDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApplicationRequestDto;
@Service
public class ApplicationService {

    private final RestClient devPortalClient;
    private final Wso2AuthService authService;

    public ApplicationService(RestClient.Builder builder, Wso2AuthService authService,
                              @Value("${wso2.base-url}") String baseUrl) {
        this.authService = authService;
        // On cible l'API DevPortal pour la gestion des applications
        this.devPortalClient = builder.baseUrl(baseUrl + "/api/am/devportal/v3.3").build();
    }

  public ApplicationRequestDto.CombinedResponse createApplicationWithKeys(ApplicationRequestDto request) {
        String token = authService.getAccessToken();

        // ÉTAPE 1 : Création de l'application
        Map<String, Object> appResponse = devPortalClient.post()
                .uri("/applications")
                .header("Authorization", "Bearer " + token)
                .body(request)
                .retrieve()
                .body(Map.class);

        String appId = (String) appResponse.get("applicationId");

        // ÉTAPE 2 : Préparation de la demande de génération de clés
        var keyGenBody = new ApplicationRequestDto.KeyGenRequest(
                "PRODUCTION",
                "Resident Key Manager",
                List.of("password", "client_credentials"),
                "http://sample.com/callback/url",
                List.of("am_application_scope", "default"),
                3600
        );

        // ÉTAPE 3 : Génération des clés
        Map<String, Object> keyResponse = devPortalClient.post()
                .uri("/applications/{applicationId}/generate-keys", appId)
                .header("Authorization", "Bearer " + token)
                .body(keyGenBody)
                .retrieve()
                .body(Map.class);

        // ÉTAPE 4 : Assemblage de la réponse finale
        return new ApplicationRequestDto.CombinedResponse(
                appId,
                (String) appResponse.get("name"),
                (String) keyResponse.get("consumerKey"),
                (String) keyResponse.get("consumerSecret"),
                (String) keyResponse.get("keyState")
        );
    }
    public AppDetailsDto getAppDetails(String appId) {
        String token = authService.getAccessToken();

        // 1. Récupérer les Clés OAuth (Consumer Key & Secret)
        var keysResponse = devPortalClient.get()
                .uri("/applications/{appId}/oauth-keys", appId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        String consumerKey = null;
        String consumerSecret = null;
        
        List<Map<String, Object>> keyList = (List<Map<String, Object>>) keysResponse.get("list");
        if (keyList != null && !keyList.isEmpty()) {
            consumerKey = (String) keyList.get(0).get("consumerKey");
            consumerSecret = (String) keyList.get(0).get("consumerSecret");
        }

        // 2. Récupérer les Subscriptions (APIs liées à l'app)
        var subResponse = devPortalClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/subscriptions")
                        .queryParam("applicationId", appId)
                        .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> subListRaw = (List<Map<String, Object>>) subResponse.get("list");
        
        // 3. Transformation des données pour le mapping
        List<AppDetailsDto.SubscriptionInfo> subscriptions = subListRaw.stream().map(sub -> {
            Map<String, Object> apiRaw = (Map<String, Object>) sub.get("apiInfo");
            
            var apiInfo = new AppDetailsDto.ApiBriefInfo(
                (String) apiRaw.get("id"),
                (String) apiRaw.get("name"),
                (String) apiRaw.get("description"),
                (String) apiRaw.get("context"),
                (String) apiRaw.get("version")
            );

            return new AppDetailsDto.SubscriptionInfo(
                (String) sub.get("subscriptionId"),
                (String) sub.get("apiId"),
                apiInfo,
                (String) sub.get("throttlingPolicy"),
                (String) sub.get("status")
            );
        }).toList();

        // On récupère les infos de l'application depuis la première subscription si disponible
        String appName = "N/A";
        String appPolicy = "N/A";
        String appStatus = "N/A";
        
        if (!subListRaw.isEmpty()) {
            Map<String, Object> appRaw = (Map<String, Object>) subListRaw.get(0).get("applicationInfo");
            appName = (String) appRaw.get("name");
            appPolicy = (String) appRaw.get("throttlingPolicy");
            appStatus = (String) appRaw.get("status");
        }

        // 4. Retourner l'objet complet
        return new AppDetailsDto(
                appId,
                appName,
                consumerKey,
                consumerSecret,
                appPolicy,
                appStatus,
                subscriptions
        );
    }
    public ApplicationBriefDto.ListResponse getAllApplications() {
        String token = authService.getAccessToken();

        // 1. Fetch all applications from WSO2
        var response = devPortalClient.get()
                .uri("/applications")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        // 2. Extract the 'list' and 'pagination' data
        List<Map<String, Object>> rawList = (List<Map<String, Object>>) response.get("list");
        Map<String, Object> pagination = (Map<String, Object>) response.get("pagination");
        int total = (int) pagination.get("total");

        // 3. Map to our lightweight DTO
        List<ApplicationBriefDto> applications = rawList.stream().map(app -> 
            new ApplicationBriefDto(
                (String) app.get("applicationId"),
                (String) app.get("name"),
                (String) app.get("throttlingPolicy"),
                (String) app.get("description"),
                (String) app.get("status"),
                (int) app.get("subscriptionCount"),
                (String) app.get("createdTime")
            )
        ).toList();

        return new ApplicationBriefDto.ListResponse(total, applications);
    }
}
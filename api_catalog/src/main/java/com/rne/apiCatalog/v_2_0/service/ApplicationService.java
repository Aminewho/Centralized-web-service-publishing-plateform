package com.rne.apiCatalog.v_2_0.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.rne.apiCatalog.v_2_0.DTOs.AppDetailsDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApplicationBriefDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApplicationRequestDto;
import com.rne.apiCatalog.v_2_0.entity.UserAppEntity;
import com.rne.apiCatalog.v_2_0.repository.UserAppRepository;

@Service
public class ApplicationService {

    private final RestClient devPortalClient;
    private final Wso2AuthService authService;
    private final UserAppRepository userAppRepository;

    public ApplicationService(RestClient.Builder builder, Wso2AuthService authService,
                              UserAppRepository userAppRepository,
                              @Value("${wso2.base-url}") String baseUrl) {
        this.authService = authService;
        this.userAppRepository = userAppRepository;
        // On cible l'API DevPortal pour la gestion des applications
        this.devPortalClient = builder.baseUrl(baseUrl + "/api/am/devportal/v3.3").build();
    }

  @Transactional
public ApplicationRequestDto.CombinedResponse createApplicationWithKeys( ApplicationRequestDto request) {
    String token = authService.getAccessToken();

    // ÉTAPE 1 : Création de l'application côté WSO2
    Map<String, Object> appResponse = devPortalClient.post()
            .uri("/applications")
            .header("Authorization", "Bearer " + token)
            .body(request)
            .retrieve()
            .body(Map.class);

    String appId = (String) appResponse.get("applicationId");

    // ÉTAPE 2 & 3 : Génération des clés
    var keyGenBody = new ApplicationRequestDto.KeyGenRequest(
            "PRODUCTION", "Resident Key Manager",
            List.of("password", "client_credentials"),
            "http://sample.com/callback/url",
            List.of("am_application_scope", "default"),
            3600
    );

    Map<String, Object> keyResponse = devPortalClient.post()
            .uri("/applications/{applicationId}/generate-keys", appId)
            .header("Authorization", "Bearer " + token)
            .body(keyGenBody)
            .retrieve()
            .body(Map.class);

    // ÉTAPE 4 : Enregistrement dans la table unique
    UserAppEntity userApp = new UserAppEntity();
    
    userApp.setApplicationId(appId);
    userApp.setApplicationName((String) appResponse.get("name"));
    userApp.setConsumerKey((String) keyResponse.get("consumerKey"));
    userApp.setConsumerSecret((String) keyResponse.get("consumerSecret"));
    userApp.setKeyState((String) keyResponse.get("keyState"));

    userAppRepository.save(userApp); // Sauvegarde locale

    return new ApplicationRequestDto.CombinedResponse(
            appId,
            userApp.getApplicationName(),
            userApp.getConsumerKey(),
            userApp.getConsumerSecret(),
            userApp.getKeyState()
    );
}
  public AppDetailsDto getAppDetails(String appId) {
    String token = authService.getAccessToken();

    // 1. APPEL EXPLICITE : Récupérer les infos de base de l'Application (Source de vérité)
    // Cet appel garantit d'avoir le nom et le statut même sans souscription
    var appResponse = devPortalClient.get()
            .uri("/applications/{appId}", appId)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

    String appName = (String) appResponse.get("name");
    String appPolicy = (String) appResponse.get("throttlingPolicy");
    String appStatus = (String) appResponse.get("status");

    // 2. Récupérer les Clés OAuth
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

    // 3. Récupérer les Subscriptions
    var subResponse = devPortalClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/subscriptions")
                    .queryParam("applicationId", appId)
                    .build())
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

    List<Map<String, Object>> subListRaw = (List<Map<String, Object>>) subResponse.get("list");
    List<AppDetailsDto.SubscriptionInfo> subscriptions = (subListRaw == null) ? List.of() : subListRaw.stream().map(sub -> {
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

    // 4. Retourner l'objet complet avec les infos de l'étape 1
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
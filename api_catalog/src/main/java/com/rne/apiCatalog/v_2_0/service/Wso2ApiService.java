package com.rne.apiCatalog.v_2_0.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.rne.apiCatalog.v_2_0.DTOs.ApiBriefDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApiFullDetailsDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApiRequestDto;
import com.rne.apiCatalog.v_2_0.DTOs.PolicySelectionDto;

@Service
public class Wso2ApiService {

    private final RestClient restClient;
    private final Wso2AuthService authService;

    public Wso2ApiService(RestClient.Builder builder, Wso2AuthService authService, 
                          @Value("${wso2.base-url}") String baseUrl) {
        this.authService = authService;
        this.restClient = builder.baseUrl(baseUrl + "/api/am/publisher/v4.3").build();
    }

    public String onboardFullApi(ApiRequestDto apiRequest) {
        String token = authService.getAccessToken();

        // 1. CREATE API
        var apiResponse = restClient.post()
                .uri("/apis")
                .header("Authorization", "Bearer " + token)
                .body(apiRequest) // Sends the full JSON
                .retrieve()
                .body(Map.class);

        String apiId = (String) apiResponse.get("id");

        // 2. CREATE REVISION
        var revisionBody = Map.of("description", "Auto-generated revision");
        var revResponse = restClient.post()
                .uri("/apis/{apiId}/revisions", apiId)
                .header("Authorization", "Bearer " + token)
                .body(revisionBody)
                .retrieve()
                .body(Map.class);

        String revisionId = (String) revResponse.get("id");

        // 3. DEPLOY REVISION
        var deployBody = List.of(Map.of(
                "name", "Default",
                "vhost", "localhost",
                "displayOnDevportal", true
        ));
        
        restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/apis/{apiId}/deploy-revision")
                        .queryParam("revisionId", revisionId)
                        .build(apiId))
                .header("Authorization", "Bearer " + token)
                .body(deployBody)
                .retrieve()
                .toBodilessEntity();

        // 4. PUBLISH
        restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/apis/change-lifecycle")
                        .queryParam("action", "Publish")
                        .queryParam("apiId", apiId)
                        .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();

        return apiId;
    }
    public List<PolicySelectionDto> getCombinedPolicies(String apiId) {
        String token = authService.getAccessToken();

        // 1. Récupérer TOUTES les policies disponibles (Global)
        var allPoliciesResponse = restClient.get()
                .uri("/throttling-policies/subscription")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        // Extraire la liste des noms (depuis le JSON "list")
        List<Map<String, Object>> allList = (List<Map<String, Object>>) allPoliciesResponse.get("list");
        List<String> allNames = allList.stream()
                .map(p -> (String) p.get("name"))
                .toList();

        // 2. Récupérer les policies attachées à l'API spécifique
        var attachedPoliciesResponse = restClient.get()
                .uri("/apis/{apiId}/subscription-policies", apiId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(List.class); // C'est un tableau direct []

        // Extraire la liste des noms attachés
        List<String> attachedNames = ((List<Map<String, Object>>) attachedPoliciesResponse).stream()
                .map(p -> (String) p.get("name"))
                .toList();

        // 3. Combiner les deux : pour chaque policy globale, vérifier si elle est dans la liste "attached"
        return allNames.stream()
                .map(name -> new PolicySelectionDto(name, attachedNames.contains(name)))
                .toList();
    }
    public void updateApiSubscriptionPolicies(String apiId, List<String> newPolicies) {
        String token = authService.getAccessToken();

        // 1. Fetch current API details (to preserve context, endpoints, operations, etc.)
        Map<String, Object> currentApi = restClient.get()
                .uri("/apis/{apiId}", apiId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        // 2. Validation: Get all available policies in WSO2
        var allPoliciesResponse = restClient.get()
                .uri("/throttling-policies/subscription")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> allList = (List<Map<String, Object>>) allPoliciesResponse.get("list");
        List<String> availableNames = allList.stream()
                .map(p -> (String) p.get("name"))
                .toList();

        // Check if all requested policies actually exist
        for (String policy : newPolicies) {
            if (!availableNames.contains(policy)) {
                throw new RuntimeException("Policy not found in WSO2: " + policy);
            }
        }

        // 3. Merge: Update only the 'policies' field in the existing map
        currentApi.put("policies", newPolicies);

        // 4. PUT: Send the full body back to WSO2
        restClient.put()
                .uri("/apis/{apiId}", apiId)
                .header("Authorization", "Bearer " + token)
                .body(currentApi)
                .retrieve()
                .toBodilessEntity();
    }
    public ApiFullDetailsDto getApiFullDetails(String apiId) {
        String token = authService.getAccessToken();

        // 1. Appel pour les détails de l'API
        Map<String, Object> apiResponse = restClient.get()
                .uri("/apis/{apiId}", apiId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        // 2. Appel pour les abonnements (Subscriptions)
        Map<String, Object> subResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/subscriptions")
                        .queryParam("apiId", apiId)
                        .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        // 3. Mapping des abonnements vers notre DTO
        List<Map<String, Object>> subListRaw = (List<Map<String, Object>>) subResponse.get("list");
        
        List<ApiFullDetailsDto.SubscriptionDto> subscriptions = subListRaw.stream().map(sub -> {
            Map<String, Object> appRaw = (Map<String, Object>) sub.get("applicationInfo");
            
            var appInfo = new ApiFullDetailsDto.ApplicationInfoDto(
                (String) appRaw.get("applicationId"),
                (String) appRaw.get("name"),
                (String) appRaw.get("subscriber"),
                (String) appRaw.get("description")
            );

            return new ApiFullDetailsDto.SubscriptionDto(
                (String) sub.get("subscriptionId"),
                appInfo,
                (String) sub.get("throttlingPolicy"),
                (String) sub.get("subscriptionStatus")
            );
        }).toList();

        // 4. Construction de l'objet final combiné
        return new ApiFullDetailsDto(
            (String) apiResponse.get("id"),
            (String) apiResponse.get("name"),
            (String) apiResponse.get("description"),
            (String) apiResponse.get("context"),
            (String) apiResponse.get("version"),
            (String) apiResponse.get("lifeCycleStatus"),
            (List<String>) apiResponse.get("policies"),
            (String) apiResponse.get("createdTime"),
            apiResponse.get("endpointConfig"),
            subscriptions
        );
    }
    public ApiBriefDto.ListResponse getAllApis() {
        String token = authService.getAccessToken();

        // 1. Fetch the list of APIs from the Publisher
        var response = restClient.get()
                .uri("/apis")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        // 2. Extract raw list and pagination info
        List<Map<String, Object>> rawList = (List<Map<String, Object>>) response.get("list");
        Map<String, Object> pagination = (Map<String, Object>) response.get("pagination");
        int total = (int) pagination.get("total");

        // 3. Map to our lightweight DTO
        List<ApiBriefDto> apis = rawList.stream().map(api -> 
            new ApiBriefDto(
                (String) api.get("id"),
                (String) api.get("name"),
                (String) api.get("description"),
                (String) api.get("context"),
                (String) api.get("version"),
                (String) api.get("lifeCycleStatus")
            )
        ).toList();

        return new ApiBriefDto.ListResponse(total, apis);
    }
}
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
    
    private static final String PUBLISHER_PATH = "/api/am/publisher/v4.3";
    private static final String DEVPORTAL_PATH = "/api/am/devportal/v3.3";

    public Wso2ApiService(RestClient.Builder builder, Wso2AuthService authService, 
                          @Value("${wso2.base-url}") String baseUrl) {
        this.authService = authService;
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    // --- CORRECTION: getAllApis doit retourner ListResponse ---
    public ApiBriefDto.ListResponse getAllApis() {
        String token = authService.getAccessToken();
        var response = restClient.get()
                .uri(PUBLISHER_PATH + "/apis")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> rawList = (List<Map<String, Object>>) response.get("list");
        Map<String, Object> pagination = (Map<String, Object>) response.get("pagination");
        int total = (int) pagination.get("total");

        List<ApiBriefDto> apis = rawList.stream().map(api -> new ApiBriefDto(
                (String) api.get("id"), (String) api.get("name"), (String) api.get("description"),
                (String) api.get("context"), (String) api.get("version"), (String) api.get("lifeCycleStatus")
        )).toList();

        return new ApiBriefDto.ListResponse(total, apis);
    }

    // --- AJOUT: getCombinedPolicies (Requis par ApiPolicyController) ---
    public List<PolicySelectionDto> getCombinedPolicies(String apiId) {
        String token = authService.getAccessToken();

        var allPoliciesResponse = restClient.get()
                .uri(PUBLISHER_PATH + "/throttling-policies/subscription")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> allList = (List<Map<String, Object>>) allPoliciesResponse.get("list");
        List<String> allNames = allList.stream().map(p -> (String) p.get("name")).toList();

        var attachedPoliciesResponse = restClient.get()
                .uri(PUBLISHER_PATH + "/apis/{apiId}/subscription-policies", apiId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(List.class);

        List<String> attachedNames = ((List<Map<String, Object>>) attachedPoliciesResponse).stream()
                .map(p -> (String) p.get("name")).toList();

        return allNames.stream()
                .map(name -> new PolicySelectionDto(name, attachedNames.contains(name)))
                .toList();
    }

    // --- AJOUT: updateApiSubscriptionPolicies (Requis par ApiPolicyController) ---
    public void updateApiSubscriptionPolicies(String apiId, List<String> newPolicies) {
        String token = authService.getAccessToken();

        Map<String, Object> currentApi = restClient.get()
                .uri(PUBLISHER_PATH + "/apis/{apiId}", apiId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        currentApi.put("policies", newPolicies);

        restClient.put()
                .uri(PUBLISHER_PATH + "/apis/{apiId}", apiId)
                .header("Authorization", "Bearer " + token)
                .body(currentApi)
                .retrieve()
                .toBodilessEntity();
    }

    // --- getApiFullDetails (Optimisé avec DevPortal fallback) ---
  public ApiFullDetailsDto getApiFullDetails(String apiId) {
    String token = authService.getAccessToken();

    // 1. Appel Publisher pour les détails de l'API
    Map<String, Object> apiResponse = restClient.get()
            .uri(PUBLISHER_PATH + "/apis/{apiId}", apiId)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

    String status = (String) apiResponse.get("lifeCycleStatus");
    
    // --- NOUVELLE LOGIQUE POUR LES POLICIES (Comme ta requête Postman) ---
    
    // A. Récupérer TOUTES les politiques de souscription globales
    var allPoliciesResponse = restClient.get()
            .uri(PUBLISHER_PATH + "/throttling-policies/subscription")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

    List<Map<String, Object>> allList = (List<Map<String, Object>>) allPoliciesResponse.get("list");
    
    // B. Extraire les politiques déjà attachées à cette API
    List<Map<String, Object>> attachedTiers = (List<Map<String, Object>>) apiResponse.get("tiers");
    List<String> attachedNames = (attachedTiers != null) ? 
            attachedTiers.stream().map(t -> (String) t.get("tierName")).toList() : List.of();

    // C. Construire la liste de PolicySelectionDto [ {name: "Gold", attached: true}, ... ]
    List<PolicySelectionDto> policies = allList.stream()
            .map(p -> {
                String name = (String) p.get("name");
                return new PolicySelectionDto(name, attachedNames.contains(name));
            })
            .sorted((p1, p2) -> p1.name().compareToIgnoreCase(p2.name()))
            .toList();

    // --- FIN DE LA LOGIQUE POLICIES ---

    // 2. Gateway URL (DevPortal Fallback)
    String gatewayHttpsUrl = null;
    if ("PUBLISHED".equals(status)) {
        try {
            Map<String, Object> devResponse = restClient.get()
                    .uri(DEVPORTAL_PATH + "/apis/{apiId}", apiId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Map.class);
            gatewayHttpsUrl = extractHttpsUrlFromDevPortal(devResponse);
        } catch (Exception e) {
            System.out.println("URL Gateway non récupérable via DevPortal.");
        }
    }

    // 3. Subscriptions
    Map<String, Object> subResponse = restClient.get()
            .uri(uriBuilder -> uriBuilder.path(PUBLISHER_PATH + "/subscriptions").queryParam("apiId", apiId).build())
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

    List<Map<String, Object>> subListRaw = (List<Map<String, Object>>) subResponse.get("list");
    List<ApiFullDetailsDto.SubscriptionDto> subscriptions = (subListRaw == null) ? List.of() : subListRaw.stream().map(sub -> {
        Map<String, Object> appRaw = (Map<String, Object>) sub.get("applicationInfo");
        return new ApiFullDetailsDto.SubscriptionDto(
            (String) sub.get("subscriptionId"),
            new ApiFullDetailsDto.ApplicationInfoDto(
                (String) appRaw.get("applicationId"), (String) appRaw.get("name"),
                (String) appRaw.get("subscriber"), (String) appRaw.get("description")
            ),
            (String) sub.get("throttlingPolicy"), (String) sub.get("subscriptionStatus")
        );
    }).toList();

    // 4. Retourner le DTO complet
    return new ApiFullDetailsDto(
        (String) apiResponse.get("id"),
        (String) apiResponse.get("name"),
        (String) apiResponse.get("description"),
        (String) apiResponse.get("context"),
        (String) apiResponse.get("version"),
        status,
        policies, // <--- On passe maintenant la liste de DTOs au lieu de Strings
        (String) apiResponse.get("createdTime"),
        apiResponse.get("endpointConfig"),
        gatewayHttpsUrl,
        subscriptions
    );
}

    private String extractHttpsUrlFromDevPortal(Map<String, Object> devResponse) {
        if (devResponse.get("endpointURLs") instanceof List<?> list && !list.isEmpty()) {
            Map<String, Object> firstEnv = (Map<String, Object>) list.get(0);
            Map<String, Object> urls = (Map<String, Object>) firstEnv.get("URLs");
            if (urls != null) return (String) urls.get("https");
        }
        return null;
    }

    public String onboardFullApi(ApiRequestDto apiRequest) {
        String token = authService.getAccessToken();
        var apiResponse = restClient.post().uri(PUBLISHER_PATH + "/apis").header("Authorization", "Bearer " + token).body(apiRequest).retrieve().body(Map.class);
        String apiId = (String) apiResponse.get("id");

        var revResponse = restClient.post().uri(PUBLISHER_PATH + "/apis/{apiId}/revisions", apiId).header("Authorization", "Bearer " + token).body(Map.of("description", "Auto-gen")).retrieve().body(Map.class);
        String revisionId = (String) revResponse.get("id");

        restClient.post().uri(uriBuilder -> uriBuilder.path(PUBLISHER_PATH + "/apis/{apiId}/deploy-revision").queryParam("revisionId", revisionId).build(apiId))
                .header("Authorization", "Bearer " + token).body(List.of(Map.of("name", "Default", "vhost", "localhost", "displayOnDevportal", true))).retrieve().toBodilessEntity();

        restClient.post().uri(uriBuilder -> uriBuilder.path(PUBLISHER_PATH + "/apis/change-lifecycle").queryParam("action", "Publish").queryParam("apiId", apiId).build())
                .header("Authorization", "Bearer " + token).retrieve().toBodilessEntity();

        return apiId;
    }
}
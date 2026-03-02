package com.rne.apiCatalog.v_2_0.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.transaction.annotation.Transactional;
import com.rne.apiCatalog.v_2_0.DTOs.ApiBriefDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApiFullDetailsDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApiRequestDto;
import com.rne.apiCatalog.v_2_0.DTOs.PolicySelectionDto;
import com.rne.apiCatalog.v_2_0.DTOs.PolicySummaryDto;
import com.rne.apiCatalog.v_2_0.DTOs.SubscriptionPolicyRequest;
import com.rne.apiCatalog.v_2_0.DTOs.SubscriptionRequestDto;
import com.rne.apiCatalog.v_2_0.DTOs.TokenRequestDto;
import com.rne.apiCatalog.v_2_0.entity.ApiEntity;
import com.rne.apiCatalog.v_2_0.entity.ApiOperationEntity;
import com.rne.apiCatalog.v_2_0.entity.EndpointConfigColumns;
import com.rne.apiCatalog.v_2_0.repository.ApiRepository;
@Service
public class Wso2ApiService {

    private final RestClient restClient;
    private final Wso2AuthService authService;
    private final ApiRepository apiRepository;
    private static final String PUBLISHER_PATH = "/api/am/publisher/v4.3";
    private static final String DEVPORTAL_PATH = "/api/am/devportal/v3.3";

    public Wso2ApiService(RestClient.Builder builder, Wso2AuthService authService, 
                          @Value("${wso2.base-url}") String baseUrl, ApiRepository apiRepository) {
        this.authService = authService;
        this.apiRepository = apiRepository;
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

    // Extraction des noms avec FILTRAGE pour exclure 'DefaultSubscriptionless'
    List<String> allNames = allList.stream()
            .map(p -> (String) p.get("name"))
            .filter(name -> !"DefaultSubscriptionless".equalsIgnoreCase(name)) // <-- Filtre ajouté ici
            .toList();

    var attachedPoliciesResponse = restClient.get()
            .uri(PUBLISHER_PATH + "/apis/{apiId}/subscription-policies", apiId)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(List.class);

    List<String> attachedNames = ((List<Map<String, Object>>) attachedPoliciesResponse).stream()
            .map(p -> (String) p.get("name")).toList();

    return allNames.stream()
            .map(name -> new PolicySelectionDto(name, attachedNames.contains(name)))
            .sorted((p1, p2) -> p1.name().compareToIgnoreCase(p2.name())) // Optionnel: Tri alphabétique
            .toList();
}
    // --- AJOUT: updateApiSubscriptionPolicies (Requis par ApiPolicyController) ---
 @Transactional // Crucial pour s'assurer que si la DB échoue, on peut gérer l'état
public void updateApiSubscriptionPolicies(String apiId, List<String> newPolicies) {
    // 1. Vérifier d'abord si l'API existe dans notre DB locale
    ApiEntity apiEntity = apiRepository.findById(apiId)
            .orElseThrow(() -> new RuntimeException("API non trouvée dans la base locale : " + apiId));

    String token = authService.getAccessToken();
    String authHeader = "Bearer " + token;

    // 2. Récupération de l'état actuel sur WSO2
    Map<String, Object> currentApi = restClient.get()
            .uri(PUBLISHER_PATH + "/apis/{apiId}", apiId)
            .header("Authorization", authHeader)
            .retrieve()
            .body(Map.class);

    // 3. Mise à jour de l'objet pour WSO2
    currentApi.put("policies", newPolicies);

    // 4. Envoi de la mise à jour à WSO2
    restClient.put()
            .uri(PUBLISHER_PATH + "/apis/{apiId}", apiId)
            .header("Authorization", authHeader)
            .body(currentApi)
            .retrieve()
            .toBodilessEntity();

    // 5. Mise à jour de la base de données locale
    // Comme c'est une @ElementCollection, on remplace simplement la liste
    apiEntity.setPolicies(newPolicies);
    
    // Pas besoin de save() explicite si tu es dans une méthode @Transactional,
    // mais on le met pour la clarté.
    apiRepository.save(apiEntity); 
}

    // --- getApiFullDetails (Optimisé avec DevPortal fallback) ---
 public ApiFullDetailsDto getApiFullDetails(String apiId) {
    String token = authService.getAccessToken();

    // 1. Appel Publisher
    Map<String, Object> apiResponse = restClient.get()
            .uri(PUBLISHER_PATH + "/apis/{apiId}", apiId)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

    // --- EXTRACTION DES OPÉRATIONS (NOUVEAU) ---
    List<Map<String, Object>> opsRaw = (List<Map<String, Object>>) apiResponse.get("operations");
    List<ApiFullDetailsDto.OperationDto> operations = (opsRaw == null) ? List.of() : opsRaw.stream().map(op -> 
        new ApiFullDetailsDto.OperationDto(
            (String) op.get("id"),
            (String) op.get("target"),
            (String) op.get("verb"),
            (String) op.get("authType"),
            (String) op.get("throttlingPolicy"),
            (List<String>) op.get("scopes"),
            op.get("operationPolicies")
        )
    ).toList();

    // --- LOGIQUE DES POLICIES (EXISTANTE) ---
    var allPoliciesResponse = restClient.get()
            .uri(PUBLISHER_PATH + "/throttling-policies/subscription")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

    List<Map<String, Object>> allList = (List<Map<String, Object>>) allPoliciesResponse.get("list");
    List<Map<String, Object>> attachedTiers = (List<Map<String, Object>>) apiResponse.get("tiers");
    List<String> attachedNames = (attachedTiers != null) ? 
            attachedTiers.stream().map(t -> (String) t.get("tierName")).toList() : List.of();

    List<PolicySelectionDto> policies = allList.stream()
            .map(p -> (String) p.get("name"))
            .filter(name -> !"DefaultSubscriptionless".equalsIgnoreCase(name))
            .map(name -> new PolicySelectionDto(name, attachedNames.contains(name)))
            .sorted((p1, p2) -> p1.name().compareToIgnoreCase(p2.name()))
            .toList();

    // --- LOGIQUE GATEWAY URL & SUBSCRIPTIONS (EXISTANTE) ---
    String status = (String) apiResponse.get("lifeCycleStatus");
    String gatewayHttpsUrl = null;
    if ("PUBLISHED".equals(status)) {
        try {
            Map<String, Object> devResponse = restClient.get()
                    .uri(DEVPORTAL_PATH + "/apis/{apiId}", apiId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Map.class);
            gatewayHttpsUrl = extractHttpsUrlFromDevPortal(devResponse);
        } catch (Exception e) { /* ... */ }
    }

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

    // 4. Retourner le DTO final avec les opérations
    return new ApiFullDetailsDto(
        (String) apiResponse.get("id"),
        (String) apiResponse.get("name"),
        (String) apiResponse.get("description"),
        (String) apiResponse.get("context"),
        (String) apiResponse.get("version"),
        status,
        policies,
        (String) apiResponse.get("createdTime"),
        apiResponse.get("endpointConfig"),
        gatewayHttpsUrl,
        subscriptions,
        operations // <--- Ajouté ici
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
    String authHeader = "Bearer " + token;


    try {
        // 1. Étape 1 : Création de l'API (POST)
        // WSO2 crée l'API et nous renvoie l'objet avec TOUS les attributs par défaut (ID, provider, etc.)
        var apiResponse = restClient.post()
                .uri(PUBLISHER_PATH + "/apis")
                .header("Authorization", authHeader)
                .body(apiRequest)
                .retrieve()
                .body(Map.class);
        
        String apiId = (String) apiResponse.get("id");

        // 2. Étape 2 : Préparation du corps du PUT
        // On récupère la réponse complète du POST pour être sûr de n'oublier aucun attribut (Metadata, Business Info, etc.)
        // On fusionne ensuite avec les attributs spécifiques du front-end
        Map<String, Object> putBody = new HashMap<>(apiResponse);

        // On écrase/ajoute les champs envoyés par le front-end pour le PUT
        putBody.put("name", apiRequest.name());
        putBody.put("context", apiRequest.context());
        putBody.put("version", apiRequest.version());
        putBody.put("transport", apiRequest.transport());
        putBody.put("policies", apiRequest.policies());
        putBody.put("apiThrottlingPolicy", apiRequest.apiThrottlingPolicy());
        putBody.put("securityScheme", apiRequest.securityScheme());
        putBody.put("visibility", apiRequest.visibility());
        putBody.put("endpointConfig", apiRequest.endpointConfig());
        putBody.put("operations", apiRequest.operations()); // Contient les OperationPolicies (HistoryLogging)

        // 3. Étape 3 : Mise à jour complète (PUT)
        // C'est ici que l'on enregistre les politiques d'opérations
        restClient.put()
                .uri(PUBLISHER_PATH + "/apis/{apiId}", apiId)
                .header("Authorization", authHeader)
                .body(putBody)
                .retrieve()
                .toBodilessEntity();


        // 4. Étape 4 : Création de la Révision
        var revResponse = restClient.post()
                .uri(PUBLISHER_PATH + "/apis/{apiId}/revisions", apiId)
                .header("Authorization", authHeader)
                .body(Map.of("description", "Révision avec politiques de log"))
                .retrieve()
                .body(Map.class);
        
        String revisionId = (String) revResponse.get("id");

        // 5. Étape 5 : Déploiement de la Révision
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path(PUBLISHER_PATH + "/apis/{apiId}/deploy-revision")
                        .queryParam("revisionId", revisionId).build(apiId))
                .header("Authorization", authHeader)
                .body(List.of(Map.of(
                        "name", "Default", 
                        "vhost", "localhost", 
                        "displayOnDevportal", true
                )))
                .retrieve()
                .toBodilessEntity();

        // 6. Étape 6 : Publication (Change Lifecycle)
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path(PUBLISHER_PATH + "/apis/change-lifecycle")
                        .queryParam("action", "Publish")
                        .queryParam("apiId", apiId).build())
                .header("Authorization", authHeader)
                .retrieve()
                .toBodilessEntity();

      String wso2Uuid = (String) apiResponse.get("id");

        // ... (Exécution des étapes PUT, Revision, Deploy, Publish) ...
ApiEntity localApi = new ApiEntity();
localApi.setId(wso2Uuid);
localApi.setName(apiRequest.name());
// ... mapping des champs simples ...

// Mapping EndpointConfig
EndpointConfigColumns config = new EndpointConfigColumns();
config.setEndpointType(apiRequest.endpointConfig().endpoint_type());
config.setProductionUrl(apiRequest.endpointConfig().production_endpoints().url());
config.setSandboxUrl(apiRequest.endpointConfig().sandbox_endpoints().url());
localApi.setEndpointConfig(config);

// Mapping Operations (Transformation Record -> Entity)
List<ApiOperationEntity> opEntities = apiRequest.operations().stream().map(op -> {
    ApiOperationEntity entity = new ApiOperationEntity();
    entity.setTarget(op.target());
    entity.setVerb(op.verb());
    entity.setAuthType(op.authType());
    entity.setThrottlingPolicy(op.throttlingPolicy());
    return entity;
}).collect(Collectors.toList());
localApi.setOperations(opEntities);

localApi.setTransport(apiRequest.transport());
localApi.setPolicies(apiRequest.policies());
localApi.setOnboardedAt(LocalDateTime.now());

apiRepository.save(localApi);

        return wso2Uuid;
    } catch (Exception e) {
        throw new RuntimeException("Erreur critique d'onboarding : " + e.getMessage());
    }
}
public List<PolicySummaryDto> getAvailablePolicies() {
    String token = authService.getAccessToken();

    // Récupération de la liste brute depuis WSO2
    Map<String, Object> response = restClient.get()
            .uri(PUBLISHER_PATH + "/operation-policies")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

    List<Map<String, Object>> rawList = (List<Map<String, Object>>) response.get("list");

    if (rawList == null) return List.of();

    // Mapping vers notre DTO minimaliste
    return rawList.stream()
            .map(p -> new PolicySummaryDto(
                    (String) p.get("id"),
                    (String) p.get("name"),
                    (String) p.get("version"),
                    (String) p.get("displayName")
            ))
            .toList();
}
public Map<String, Object> createSubscription(SubscriptionRequestDto request) {
    String token = authService.getAccessToken();

    // On doit utiliser le chemin complet du DevPortal
    return restClient.post()
            .uri(DEVPORTAL_PATH + "/subscriptions")
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json") // Bonne pratique
            .body(request)
            .retrieve()
            .body(Map.class); 
}

public Map<String, Object> generateAppToken(TokenRequestDto request) {
    // 1. Préparation de l'authentification Basic (Base64)
    String credentials = request.consumerKey() + ":" + request.consumerSecret();
    String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

    // 2. Préparation du corps (form-urlencoded)
    // Même si vous avez dit "vide", WSO2 nécessite au moins le grant_type
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("grant_type", "client_credentials");

    // 3. Appel à l'endpoint OAuth2 de WSO2
    return restClient.post()
            .uri("/oauth2/token")
            .header("Authorization", "Basic " + encodedCredentials)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(formData)
            .retrieve()
            .body(Map.class);
}
public List<SubscriptionPolicyRequest> getAllSubscriptionPolicies() {
    String token = authService.getAccessToken();

    // Appel à l'API Publisher de WSO2
    var response = restClient.get()
            .uri(PUBLISHER_PATH + "/throttling-policies/subscription")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

    // Extraction de la liste
    List<Map<String, Object>> policyListRaw = (List<Map<String, Object>>) response.get("list");

    if (policyListRaw == null) return List.of();

    // Mapping vers votre DTO simplifié
    return policyListRaw.stream()
            .map(p -> new SubscriptionPolicyRequest(
                (String) p.get("name"),
                (String) p.get("description"),
                (Integer) p.get("requestCount"),
                (String) p.get("timeUnit"),
                (Integer) p.get("unitTime")
            ))
            .filter(p -> !"DefaultSubscriptionless".equalsIgnoreCase(p.policyName()))
            .toList();
}
}
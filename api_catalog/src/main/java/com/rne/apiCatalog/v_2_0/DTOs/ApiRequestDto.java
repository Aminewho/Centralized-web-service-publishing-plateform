package com.rne.apiCatalog.v_2_0.DTOs;

import java.util.List;
import java.util.Map;

public record ApiRequestDto(
    String name,
    String context,
    String version,
    String description,
    String provider,
    String lifeCycleStatus,
    List<String> transport,
    List<String> tags,
    List<String> policies,
    String apiThrottlingPolicy,
    List<String> securityScheme,
    MaxTps maxTps,
    String visibility,
    EndpointConfig endpointConfig,
    List<Operation> operations,
    BusinessInformation businessInformation,
    List<AdditionalProperty> additionalProperties
) {
    // Structures imbriquées mises à jour
    public record EndpointConfig(String endpoint_type, EndpointDetails production_endpoints, EndpointDetails sandbox_endpoints) {}
    public record EndpointDetails(String url) {}
    
    // Mise à jour de Operation pour inclure les politiques
    public record Operation(
        String target, 
        String verb, 
        String authType, 
        String throttlingPolicy,
        OperationPolicies operationPolicies // Ajouté ici
    ) {}

    // Nouvelles structures pour les politiques d'opération
    public record OperationPolicies(
        List<PolicyReference> request,
        List<PolicyReference> response,
        List<PolicyReference> fault
    ) {}

    public record PolicyReference(
        String policyName,
        String policyVersion,
        String policyId,
        Map<String, Object> parameters
    ) {}

    public record AdditionalProperty(String name, String value, boolean display) {}
    public record MaxTps(Integer production, Integer sandbox) {}
    public record BusinessInformation(String businessOwner, String businessOwnerEmail, String technicalOwner, String technicalOwnerEmail) {}

    // Constructor pour les valeurs par défaut
    public ApiRequestDto {
        if (provider == null) provider = "admin";
        if (lifeCycleStatus == null) lifeCycleStatus = "CREATED";
        if (transport == null) transport = List.of("http", "https");
        if (policies == null) policies = List.of("Unlimited");
        if (apiThrottlingPolicy == null) apiThrottlingPolicy = "Unlimited";
        if (securityScheme == null) securityScheme = List.of("oauth2", "oauth_basic_auth_api_key_mandatory");
        if (visibility == null) visibility = "PUBLIC";
    }
}
package com.rne.apiCatalog.v_2_0.DTOs;

import java.util.List;


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
    // These are static nested records. They CAN stay in this file.
    public record EndpointConfig(String endpoint_type, EndpointDetails production_endpoints, EndpointDetails sandbox_endpoints) {}
    public record EndpointDetails(String url) {}
    public record Operation(String target, String verb, String authType, String throttlingPolicy) {}
    public record AdditionalProperty(String name, String value, boolean display) {}
    public record MaxTps(Integer production, Integer sandbox) {}
    public record BusinessInformation(String businessOwner, String businessOwnerEmail, String technicalOwner, String technicalOwnerEmail) {}

    // Constructor for defaults
    public ApiRequestDto {
        if (provider == null) provider = "admin";
        if (lifeCycleStatus == null) lifeCycleStatus = "CREATED";
        if (transport == null) transport = List.of("http", "https");
        if (policies == null) policies = List.of("Unlimited");
        if (apiThrottlingPolicy == null) apiThrottlingPolicy = "Unlimited";
        if (securityScheme == null) securityScheme = List.of("oauth2");
        if (visibility == null) visibility = "PUBLIC";
    }
}
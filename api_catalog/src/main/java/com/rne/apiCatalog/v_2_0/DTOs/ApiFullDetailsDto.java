package com.rne.apiCatalog.v_2_0.DTOs;

import java.util.List;

public record ApiFullDetailsDto(
      String id,
    String name,
    String description,
    String context,
    String version,
    String status,
    List<PolicySelectionDto> policies, // Changement ici
    String createdTime,
    Object endpointConfig,
    String gatewayUrl,
    List<SubscriptionDto> subscriptions,
    List<OperationDto> operations // <--- Nouveau champ
) {
    public record OperationDto(
        String id,
        String target,
        String verb,
        String authType,
        String throttlingPolicy,
        List<String> scopes,
        Object operationPolicies // On peut le garder en Object pour simplifier
    ) {}
    public record SubscriptionDto(
        String subscriptionId,
        ApplicationInfoDto applicationInfo,
        String throttlingPolicy,
        String subscriptionStatus
    ) {}

    public record ApplicationInfoDto(
        String applicationId,
        String name,
        String subscriber,
        String description
    ) {}
}
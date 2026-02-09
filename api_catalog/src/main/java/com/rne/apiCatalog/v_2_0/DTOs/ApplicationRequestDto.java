package com.rne.apiCatalog.v_2_0.DTOs;

import java.util.List;


public record ApplicationRequestDto(
    String name,
    String throttlingPolicy,
    String description,
    String tokenType
) {
    public ApplicationRequestDto {
        if (throttlingPolicy == null) throttlingPolicy = "Unlimited";
        if (tokenType == null) tokenType = "JWT";
        if(description == null) description = "";
    }

    // DTO interne pour la génération de clés (WSO2)
    public record KeyGenRequest(
        String keyType,
        String keyManager,
        List<String> grantTypesToBeSupported,
        String callbackUrl,
        List<String> scopes,
        int validityTime
    ) {}

    // DTO de réponse finale pour le Frontend
    public record CombinedResponse(
        String applicationId,
        String name,
        String consumerKey,
        String consumerSecret,
        String keyState
    ) {}
}
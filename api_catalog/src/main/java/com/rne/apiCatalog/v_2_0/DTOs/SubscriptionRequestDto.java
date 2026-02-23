package com.rne.apiCatalog.v_2_0.DTOs;
public record SubscriptionRequestDto(
    String applicationId,
    String apiId,
    String throttlingPolicy,
    String requestedThrottlingPolicy
) {}
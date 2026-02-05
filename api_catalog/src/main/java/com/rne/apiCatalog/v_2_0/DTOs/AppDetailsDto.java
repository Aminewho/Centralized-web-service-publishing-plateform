package com.rne.apiCatalog.v_2_0.DTOs;

import java.util.List;

public record AppDetailsDto(
    String applicationId,
    String name,
    String consumerKey,
    String consumerSecret,
    String throttlingPolicy,
    String status,
    List<SubscriptionInfo> subscriptions
) {
    public record SubscriptionInfo(
        String subscriptionId,
        String apiId,
        ApiBriefInfo apiInfo,
        String throttlingPolicy,
        String status
    ) {}

    public record ApiBriefInfo(
        String id,
        String name,
        String description,
        String context,
        String version
    ) {}
}
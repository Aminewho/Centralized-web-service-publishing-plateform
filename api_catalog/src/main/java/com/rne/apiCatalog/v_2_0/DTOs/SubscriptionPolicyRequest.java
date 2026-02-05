package com.rne.apiCatalog.v_2_0.DTOs;

public record SubscriptionPolicyRequest(
    String policyName,
    String description,
    int requestCount,
    String timeUnit,
    int unitTime
) {
    // Nested records for the full WSO2 structure
    public record Wso2PolicyBody(
        String policyName,
        String description,
        DefaultLimit defaultLimit,
        String billingPlan,
        boolean stopOnQuotaReach,
        String rateLimitCount,
        String rateLimitTimeUnit,
        Object monetization // Simplified as Object for the default empty structure
    ) {}

    public record DefaultLimit(String type, RequestCount requestCount) {}
    public record RequestCount(int requestCount, String timeUnit, int unitTime) {}
}
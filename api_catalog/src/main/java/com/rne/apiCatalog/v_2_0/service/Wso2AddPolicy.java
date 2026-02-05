
package com.rne.apiCatalog.v_2_0.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.rne.apiCatalog.v_2_0.DTOs.SubscriptionPolicyRequest;

@Service
public class Wso2AddPolicy {

    private final RestClient adminClient;
    private final Wso2AuthService authService;

    public Wso2AddPolicy(RestClient.Builder builder, Wso2AuthService authService,
                            @Value("${wso2.base-url}") String baseUrl) {
        this.authService = authService;
        // Target the Admin V4 API
        this.adminClient = builder.baseUrl(baseUrl + "/api/am/admin/v4").build();
    }

    public void createSubscriptionPolicy(SubscriptionPolicyRequest request) {
        String token = authService.getAccessToken();

        // Construct the full WSO2 body from the simple frontend request
        var fullBody = new SubscriptionPolicyRequest.Wso2PolicyBody(
            request.policyName(),
            request.description(),
            new SubscriptionPolicyRequest.DefaultLimit(
                "REQUESTCOUNTLIMIT", 
                new SubscriptionPolicyRequest.RequestCount(request.requestCount(), request.timeUnit(), request.unitTime())
            ),
            "FREE", // Default
            true,   // Default
            String.valueOf(request.requestCount()), // Default rate limit
            "sec",  // Default
            Map.of("monetizationPlan", "FIXEDRATE", "properties", Map.of("billingCycle", "week"))
        );

        adminClient.post()
                .uri("/throttling/policies/subscription")
                .header("Authorization", "Bearer " + token)
                .body(fullBody)
                .retrieve()
                .toBodilessEntity();
    }
}
// Simplified request for your Frontend to send
package com.rne.apiCatalog.v_2_0.DTOs;

public record ApiOnboardingRequest(
    String name,
    String context,
    String version,
    String endpointUrl,
    String description
) {}

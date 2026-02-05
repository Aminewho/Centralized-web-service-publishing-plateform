package com.rne.apiCatalog.v_2_0.DTOs;

import java.util.List;

public record ApplicationBriefDto(
    String applicationId,
    String name,
    String throttlingPolicy,
    String description,
    String status,
    int subscriptionCount,
    String createdTime
) {
    // Wrapper for the list response
    public record ListResponse(
        int total,
        List<ApplicationBriefDto> applications
    ) {}
}
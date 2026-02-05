package com.rne.apiCatalog.v_2_0.DTOs;

import java.util.List;

public record ApiBriefDto(
    String id,
    String name,
    String description,
    String context,
    String version,
    String lifeCycleStatus
) {
    // Wrapper for the paginated list response
    public record ListResponse(
        int total,
        List<ApiBriefDto> apis
    ) {}
}
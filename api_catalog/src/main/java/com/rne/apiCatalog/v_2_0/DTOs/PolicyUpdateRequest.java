package com.rne.apiCatalog.v_2_0.DTOs;

import java.util.List;

public record PolicyUpdateRequest(
    List<String> policies
) {}
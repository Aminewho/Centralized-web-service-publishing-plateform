
package com.rne.apiCatalog.v_2_0.DTOs;

public record DeploymentConfig(
    String name,
    String vhost,
    boolean displayOnDevportal
) {}
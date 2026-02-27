package com.rne.apiCatalog.v_2_0.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Embeddable;



@Embeddable
@Getter @Setter
public class EndpointConfigColumns {
    private String endpointType;
    private String productionUrl;
    private String sandboxUrl;
}
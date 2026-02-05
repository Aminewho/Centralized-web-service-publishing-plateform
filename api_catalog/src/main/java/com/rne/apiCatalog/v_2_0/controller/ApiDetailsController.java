
package com.rne.apiCatalog.v_2_0.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rne.apiCatalog.v_2_0.DTOs.ApiFullDetailsDto;
import com.rne.apiCatalog.v_2_0.service.Wso2ApiService;

@RestController
@RequestMapping("/api/v1/publisher/apis")
public class ApiDetailsController {

    private final Wso2ApiService wso2Service;

    public ApiDetailsController(Wso2ApiService wso2Service) {
        this.wso2Service = wso2Service;
    }

    @GetMapping("/{apiId}/full-details")
    public ResponseEntity<ApiFullDetailsDto> getFullDetails(@PathVariable String apiId) {
        return ResponseEntity.ok(wso2Service.getApiFullDetails(apiId));
    }
}
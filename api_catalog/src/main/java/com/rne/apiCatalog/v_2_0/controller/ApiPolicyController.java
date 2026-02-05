package com.rne.apiCatalog.v_2_0.controller;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rne.apiCatalog.v_2_0.DTOs.PolicySelectionDto;
import com.rne.apiCatalog.v_2_0.DTOs.PolicyUpdateRequest;
import com.rne.apiCatalog.v_2_0.service.Wso2ApiService;
@RestController
@RequestMapping("/api/v1/publisher/apis")
public class ApiPolicyController {

    private final Wso2ApiService wso2Service;

    public ApiPolicyController(Wso2ApiService wso2Service) {
        this.wso2Service = wso2Service;
    }

    @GetMapping("/{apiId}/policies-selection")
    public ResponseEntity<List<PolicySelectionDto>> getPoliciesSelection(@PathVariable String apiId) {
        List<PolicySelectionDto> selection = wso2Service.getCombinedPolicies(apiId);
        return ResponseEntity.ok(selection);
    }
     @PutMapping("/{apiId}/policies")
    public ResponseEntity<String> updatePolicies(
            @PathVariable String apiId, 
            @RequestBody PolicyUpdateRequest request) {
        
        try {
            wso2Service.updateApiSubscriptionPolicies(apiId, request.policies());
            return ResponseEntity.ok("API policies updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
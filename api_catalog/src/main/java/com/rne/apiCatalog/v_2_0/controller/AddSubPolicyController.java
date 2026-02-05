  package com.rne.apiCatalog.v_2_0.controller;

    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    import com.rne.apiCatalog.v_2_0.DTOs.SubscriptionPolicyRequest;
    import com.rne.apiCatalog.v_2_0.service.Wso2AddPolicy;
    
@RestController
@RequestMapping("/api/v1/admin/policies")
public class AddSubPolicyController {

    private final Wso2AddPolicy addPolicyService;

    public AddSubPolicyController(Wso2AddPolicy addPolicyService) {
        this.addPolicyService = addPolicyService;
    }

    @PostMapping
    public ResponseEntity<String> createSubscriptionPolicy(@RequestBody SubscriptionPolicyRequest request) {
        addPolicyService.createSubscriptionPolicy(request);
        return ResponseEntity.ok("Subscription policy '" + request.policyName() + "' created successfully.");
    }
}
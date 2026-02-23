package com.rne.apiCatalog.v_2_0.controller;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rne.apiCatalog.v_2_0.DTOs.SubscriptionRequestDto;
import com.rne.apiCatalog.v_2_0.service.Wso2ApiService;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final Wso2ApiService wso2Service;

    public SubscriptionController(Wso2ApiService wso2Service) {
        this.wso2Service = wso2Service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSubscription(@RequestBody SubscriptionRequestDto request) {
        Map<String, Object> response = wso2Service.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
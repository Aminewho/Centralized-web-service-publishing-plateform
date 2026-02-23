package com.rne.apiCatalog.v_2_0.controller;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rne.apiCatalog.v_2_0.DTOs.ApiBriefDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApiRequestDto;
import com.rne.apiCatalog.v_2_0.DTOs.TokenRequestDto;
import com.rne.apiCatalog.v_2_0.service.Wso2ApiService;

@RestController
@RequestMapping("/apis")
public class ApiPublisherController {

    private final Wso2ApiService wso2Service;

    public ApiPublisherController(Wso2ApiService wso2Service) {
        this.wso2Service = wso2Service;
    }

    @PostMapping("/create-and-publish")
    public ResponseEntity<Map<String, String>> deployFullApi(@RequestBody ApiRequestDto request) {
        String apiId = wso2Service.onboardFullApi(request);
        return ResponseEntity.ok(Map.of(
            "apiId", apiId,
            "status", "PUBLISHED",
            "message", "API created and deployed to gateway successfully."
        ));
    }
    @GetMapping
    public ResponseEntity<ApiBriefDto.ListResponse> listAllApis() {
        return ResponseEntity.ok(wso2Service.getAllApis());
    }
    @PostMapping("/generate-token")
public ResponseEntity<Map<String, Object>> generateToken(@RequestBody TokenRequestDto request) {
    try {
        Map<String, Object> tokenResponse = wso2Service.generateAppToken(request);
        return ResponseEntity.ok(tokenResponse);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(Map.of("error", "Invalid credentials or WSO2 unreachable"));
    }
}
}
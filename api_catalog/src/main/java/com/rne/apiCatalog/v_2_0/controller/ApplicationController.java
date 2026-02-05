package com.rne.apiCatalog.v_2_0.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rne.apiCatalog.v_2_0.DTOs.AppDetailsDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApplicationBriefDto;
import com.rne.apiCatalog.v_2_0.DTOs.ApplicationRequestDto;
import com.rne.apiCatalog.v_2_0.service.ApplicationService;
@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }
    @PostMapping("/create-full")
    public ResponseEntity<ApplicationRequestDto.CombinedResponse> createFullApplication(
            @RequestBody ApplicationRequestDto request) {
        
        ApplicationRequestDto.CombinedResponse response = applicationService.createApplicationWithKeys(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/{appId}/details")
    public ResponseEntity<AppDetailsDto> getDetails(@PathVariable String appId) {
        return ResponseEntity.ok(applicationService.getAppDetails(appId));
    }
    @GetMapping
    public ResponseEntity<ApplicationBriefDto.ListResponse> listAll() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }
}
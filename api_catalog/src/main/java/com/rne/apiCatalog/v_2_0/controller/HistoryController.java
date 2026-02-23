package com.rne.apiCatalog.v_2_0.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rne.apiCatalog.v_2_0.entity.HistoryEntity;
import com.rne.apiCatalog.v_2_0.service.HistoryService;




@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping("/search")
    public ResponseEntity<List<HistoryEntity>> getHistory(
            @RequestParam(required = false) String appName,
            @RequestParam(required = false) String apiName) {
        
        List<HistoryEntity> logs = historyService.searchLogs(appName, apiName);
        return ResponseEntity.ok(logs);
    }
}

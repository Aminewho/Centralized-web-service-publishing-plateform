package com.rne.apiCatalog.v_2_0.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rne.apiCatalog.v_2_0.entity.HistoryEntity;
import com.rne.apiCatalog.v_2_0.repository.HistoryRepository;





@Service
public class HistoryService {

    @Autowired
    private HistoryRepository repository;

    public List<HistoryEntity> searchLogs(String appName, String apiName) {
        if (appName != null && apiName != null) {
            return repository.findByApplicationNameAndApiName(appName, apiName);
        } else if (appName != null) {
            return repository.findByApplicationName(appName);
        } else if (apiName != null) {
            return repository.findByApiName(apiName);
        }
        return repository.findAll();
    }
}
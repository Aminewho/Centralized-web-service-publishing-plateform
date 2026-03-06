package com.rne.apiCatalog.v_2_0.service;   
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rne.apiCatalog.v_2_0.entity.SubscriptionEntity;
import com.rne.apiCatalog.v_2_0.repository.SubscriptionRepository;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public List<SubscriptionEntity> getActiveSubscriptionsByAppName(String appName) {
        return subscriptionRepository.findByApplicationNameAndActiveTrue(appName);
    }

    public List<SubscriptionEntity> getInactiveSubscriptionsByAppName(String appName) {
        return subscriptionRepository.findByApplicationNameAndActiveFalse(appName);
    }
    public List<SubscriptionEntity> getActiveSubscribers(String apiName) {
        return subscriptionRepository.findByApiNameAndActiveTrue(apiName);
    }
    
}
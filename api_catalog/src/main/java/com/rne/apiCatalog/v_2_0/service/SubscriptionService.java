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

    /**
     * Récupère les souscriptions en cours de validité (quota non atteint)
     */
    public List<SubscriptionEntity> getActiveSubscriptionsByApp(String applicationId) {
        return subscriptionRepository.findByApplicationIdAndActiveTrue(applicationId);
    }

    /**
     * Récupère les souscriptions expirées ou supprimées
     */
    public List<SubscriptionEntity> getInactiveSubscriptionsByApp(String applicationId) {
        return subscriptionRepository.findByApplicationIdAndActiveFalse(applicationId);
    }
}
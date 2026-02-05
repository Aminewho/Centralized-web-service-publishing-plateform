package com.rne.apiCatalog.v_2_0.service;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class Wso2AuthService {

    private final RestClient authClient;

    @Value("${wso2.basic-auth-header}")
    private String basicAuthHeader;

    @Value("${wso2.admin-username}")
    private String username;

    @Value("${wso2.admin-password}")
    private String password;

    public Wso2AuthService(RestClient.Builder builder, @Value("${wso2.base-url}") String baseUrl) {
        this.authClient = builder.baseUrl(baseUrl).build();
    }

    public String getAccessToken() {
        Map<String, Object> body = new HashMap<>();
        body.put("grant_type", "password");
        body.put("username", username);
        body.put("password", password);
        // Add that long list of scopes here
        body.put("scope", "apim:api_view apim:api_create apim:api_manage apim:api_publish apim:subscription_view apim:subscription_block apim:subscription_manage apim:external_services_discover apim:threat_protection_policy_create apim:threat_protection_policy_manage apim:document_create apim:document_manage apim:mediation_policy_view apim:mediation_policy_create apim:mediation_policy_manage apim:client_certificates_view apim:client_certificates_add apim:client_certificates_update apim:ep_certificates_view apim:ep_certificates_add apim:ep_certificates_update apim:publisher_settings apim:pub_alert_manage apim:shared_scope_manage apim:app_import_export apim:api_import_export apim:api_product_import_export apim:api_generate_key apim:common_operation_policy_view apim:common_operation_policy_manage apim:comment_write apim:comment_view apim:admin apim:subscription_approval_view apim:subscription_approval_manage apim:llm_provider_read:apim:admin apim:api_key apim:app_import_export apim:app_manage apim:store_settings apim:sub_alert_manage apim:sub_manage apim:subscribe openid");

        Map response = authClient.post()
                .uri("/oauth2/token")
                .header("Authorization", basicAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        return (String) response.get("access_token");
    }
}
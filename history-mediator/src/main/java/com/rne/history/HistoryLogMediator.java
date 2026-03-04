package com.rne.history;

import java.nio.charset.StandardCharsets;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class HistoryLogMediator extends AbstractMediator {

    private final static String QUEUE_NAME = "api_logs";
    // Remplace par l'IP de ton serveur RabbitMQ
private final static String RABBIT_HOST = "192.168.74.67";

@Override
public boolean mediate(MessageContext context) {
    // --- ÉTAPE 1 : CAPTURE À L'ALLER (Requête) ---
if (!context.isResponse()) {
    AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(context);
    if (authContext != null) {
        context.setProperty("SAVED_APP_NAME", authContext.getApplicationName());
    }
    // Le nom de l'API est généralement déjà dans le contexte
    context.setProperty("SAVED_API_NAME", context.getProperty("API_NAME"));
    return true;
}

    // --- ÉTAPE 2 : LOG AU RETOUR (Réponse) ---
    try {
        long duration = 0;
        String startProperty = (String) context.getProperty("am.request.time");
        if (startProperty != null) {
            duration = System.currentTimeMillis() - Long.parseLong(startProperty);
        }

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) context).getAxis2MessageContext();
        Object s = axis2MC.getProperty("HTTP_SC");
        if (s == null) s = axis2MC.getProperty("HTTP_RESPONSE_STATUS_CODE");
        String statusCode = (s != null) ? s.toString() : "200";

        // Récupération des données sauvegardées
     
        String policy = (String) context.getProperty("SAVED_POLICY");
String appName = (String) context.getProperty("SAVED_APP_NAME");
String apiName = (String) context.getProperty("SAVED_API_NAME");
        String verb = (String) context.getProperty("REST_METHOD");
        if (verb == null) verb = (String) axis2MC.getProperty("HTTP_METHOD");
        String path = (String) context.getProperty("REST_FULL_REQUEST_PATH");
        if (path == null) path = (String) axis2MC.getProperty("REST_URL_POSTFIX"); 


String payload = String.format(
    "{\"correlationId\":\"%s\", \"applicationName\":\"%s\", \"apiName\":\"%s\", \"verb\":\"%s\", \"path\":\"%s\", \"status\":\"%s\", \"duration\":%d, \"throttlingPolicy\":\"%s\"}",
    context.getMessageID(),
    (appName != null ? appName : "UnknownApp"),
    (apiName != null ? apiName : "UnknownAPI"),
    (verb != null ? verb : "UNKNOWN"),
    (path != null ? path : "/"),
    statusCode,
    duration,
    (policy != null ? policy : "UnknownPolicy")
);

        new Thread(() -> sendToRabbitMQ(payload)).start();

    } catch (Exception e) {
        log.error("HistoryLogMediator Error: " + e.getMessage());
    }
    return true;
}

private void sendToRabbitMQ(String message) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RABBIT_HOST);
    factory.setUsername("guest");
    factory.setPassword("guest");

    Connection connection = null;
    Channel channel = null;

    try {
        connection = factory.newConnection();
        channel = connection.createChannel();
        
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
        
    } catch (Exception e) {
        log.error("Failed to send log to RabbitMQ: " + e.getMessage());
    } finally {
        // Fermeture manuelle pour éviter les fuites de mémoire
        try {
            if (channel != null && channel.isOpen()) channel.close();
            if (connection != null && connection.isOpen()) connection.close();
        } catch (Exception e) {
            log.error("Error closing RabbitMQ resources: " + e.getMessage());
    }
}
}
}
package com.rne.history;

import java.nio.charset.StandardCharsets;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

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
    // On intercepte le subscriptionId ici car il est présent juste après l'authentification
    if (!context.isResponse()) {
        String subId = (String) context.getProperty("api.ut.subscription.id");
        if (subId != null) {
            // On sauvegarde l'ID dans une propriété personnalisée pour la retrouver au retour
            context.setProperty("SAVED_SUB_ID", subId);
        }
        return true; // On laisse passer la requête vers le backend
    }

    // --- ÉTAPE 2 : LOG AU RETOUR (Réponse) ---
    try {
        // 1. Calcul de la latence
        long currentTime = System.currentTimeMillis();
        String startProperty = (String) context.getProperty("am.request.time");
        long duration = 0;
        if (startProperty != null) {
            duration = currentTime - Long.parseLong(startProperty);
        }

        // 2. Récupération du statut HTTP
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) context).getAxis2MessageContext();
        Object s = axis2MC.getProperty("HTTP_SC");
        if (s == null) s = axis2MC.getProperty("HTTP_RESPONSE_STATUS_CODE");
        String statusCode = (s != null) ? s.toString() : "200";

        // 3. Récupération sécurisée du Subscription ID
        // On cherche d'abord notre propriété sauvegardée à l'aller
        String subscriptionId = (String) context.getProperty("SAVED_SUB_ID");
        
        // Fallback : au cas où elle apparaîtrait quand même dans la réponse
        if (subscriptionId == null) {
            subscriptionId = (String) context.getProperty("api.ut.subscription.id");
        }

        // 4. Récupération des autres métadonnées
        String appId = (String) context.getProperty("api.ut.application.name");
        if (appId == null) appId = (String) context.getProperty("APPLICATION_NAME");

        String apiName = (String) context.getProperty("API_NAME");

        String verb = (String) context.getProperty("REST_METHOD");
        if (verb == null) verb = (String) axis2MC.getProperty("HTTP_METHOD");

        String path = (String) context.getProperty("REST_FULL_REQUEST_PATH");
        if (path == null) path = (String) axis2MC.getProperty("REST_URL_POSTFIX"); 

        // 5. Préparation du JSON final
        String payload = String.format(
            "{\"correlationId\":\"%s\", \"applicationName\":\"%s\", \"apiName\":\"%s\", \"verb\":\"%s\", \"path\":\"%s\", \"status\":\"%s\", \"duration\":%d, \"subscriptionId\":\"%s\"}",
            context.getMessageID(),
            (appId != null ? appId : "UnknownApp"),
            (apiName != null ? apiName : "UnknownAPI"),
            (verb != null ? verb : "UNKNOWN"),
            (path != null ? path : "/"),
            statusCode,
            duration,
            (subscriptionId != null ? subscriptionId : "UnknownSubscription")
        );

        // 6. Envoi asynchrone vers RabbitMQ
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
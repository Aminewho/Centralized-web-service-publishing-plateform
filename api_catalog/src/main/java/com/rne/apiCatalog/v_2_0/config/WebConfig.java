package com.rne.apiCatalog.v_2_0.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Appliquer à tous les endpoints
                        .allowedOrigins("http://localhost:5173") // Ton port Vite/React
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Méthodes autorisées
                        .allowedHeaders("*") // Autoriser tous les headers
                        .allowCredentials(true); // Utile si tu gères des cookies/sessions plus tard
            }
        };
    }
}
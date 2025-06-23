package com.jamaa.service_proxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Autoriser TOUTES les origines
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Autoriser TOUTES les méthodes HTTP
        config.setAllowedMethods(Arrays.asList("*"));
        
        // Autoriser TOUS les headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // IMPORTANT : Avec allowedOriginPatterns("*"), vous devez mettre allowCredentials à false
        config.setAllowCredentials(false);
        
        // Headers exposés (optionnel)
        config.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials",
            "Content-Type",
            "Authorization"
        ));
        
        // Durée de cache pour les requêtes preflight
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
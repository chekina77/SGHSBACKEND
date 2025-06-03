package com.example.SGHS4.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active un broker simple en mémoire, qui gère les destinations commençant par /topic
        config.enableSimpleBroker("/topic");
        // Préfixe pour les destinations envoyées par le client (ex: /app/quelquechose)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée WebSocket accessible via SockJS, avec acceptation de toutes les origines
        registry.addEndpoint("/ws-labresult")
                .setAllowedOriginPatterns("*"); // pas de withSockJS()
    }
}

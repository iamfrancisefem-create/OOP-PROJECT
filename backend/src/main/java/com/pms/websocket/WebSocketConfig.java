package com.pms.websocket;

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
        // Enables a simple in-memory message broker
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix used for client-to-server mappings (endpoints mapped with @MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix used for point-to-point / direct messaging
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket / ws endpoint, allowing any origin for local dev
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
                
        // Register SockJS fallback options for browser compatibility
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

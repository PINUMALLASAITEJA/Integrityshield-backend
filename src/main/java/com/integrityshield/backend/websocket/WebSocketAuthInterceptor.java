package com.integrityshield.backend.websocket;

import com.integrityshield.backend.security.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            List<String> authHeaders =
                    accessor.getNativeHeader("Authorization");

            if (authHeaders != null && !authHeaders.isEmpty()) {

                String token = authHeaders.get(0)
                        .replace("Bearer ", "");

                if (jwtUtil.validateToken(token)) {

                    String username =
                            jwtUtil.extractUserIdentifier(token);

                    String role =
                            jwtUtil.extractRole(token).toUpperCase();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    accessor.setUser(authentication);
                }
            }
        }

        return message;
    }
}
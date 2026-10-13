package com.ucb.sakila.apigateway.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucb.sakila.apigateway.shared.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

// Reemplaza el 401 de cuerpo vacío de Spring Security por el shape del ecosistema.
@Component
@RequiredArgsConstructor
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.of(401, "INVALID_TOKEN", "Invalid or expired token.", request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), body);
    }
}

package com.ucb.sakila.apigateway.shared;

import java.time.Instant;

/** Misma forma de respuesta de error que el resto del ecosistema: {statusCode, error, message, path, timestamp}. */
public record ErrorResponse(int statusCode, String error, String message, String path, String timestamp) {

    public static ErrorResponse of(int statusCode, String error, String message, String path) {
        return new ErrorResponse(statusCode, error, message, path, Instant.now().toString());
    }
}

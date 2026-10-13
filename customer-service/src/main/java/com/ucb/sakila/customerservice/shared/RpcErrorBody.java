package com.ucb.sakila.customerservice.shared;

// Mismo shape de error que el resto del ecosistema.
public record RpcErrorBody(int statusCode, String error, String message) {
}

package com.ucb.sakila.apigateway.auth;

// Solo los campos que este gateway usa (id para "sub", username para mostrar).
public record StaffDto(Integer id, String username) {
}

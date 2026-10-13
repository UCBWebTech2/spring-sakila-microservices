package com.ucb.sakila.apigateway.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Integer addressId,
        @NotNull Integer storeId,
        @NotBlank String username,
        @NotBlank String password,
        @Email String email) {
}

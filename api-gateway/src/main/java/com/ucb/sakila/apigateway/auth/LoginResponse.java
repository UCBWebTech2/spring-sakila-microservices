package com.ucb.sakila.apigateway.auth;

public record LoginResponse(String accessToken, StaffDto staff) {
}

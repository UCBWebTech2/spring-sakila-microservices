package com.ucb.sakila.apigateway.shared;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Error ya clasificado (status + código) — normalmente relayado desde otro microservicio. */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}

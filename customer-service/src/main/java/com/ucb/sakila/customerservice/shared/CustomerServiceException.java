package com.ucb.sakila.customerservice.shared;

import lombok.Getter;

// code viaja como "<code>: <message>" en el status gRPC y como {statusCode, error, message} en AMQP.
@Getter
public abstract class CustomerServiceException extends RuntimeException {

    private final String code;
    private final int statusCode;

    protected CustomerServiceException(String code, int statusCode, String message) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }
}

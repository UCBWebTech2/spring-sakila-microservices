package com.ucb.sakila.businessservice.shared;

import lombok.Getter;

// code viaja en extensions.code de cada GraphQLError (ver GraphQlExceptionResolver).
@Getter
public abstract class BusinessServiceException extends RuntimeException {

    private final String code;
    private final int statusCode;

    protected BusinessServiceException(String code, int statusCode, String message) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }
}

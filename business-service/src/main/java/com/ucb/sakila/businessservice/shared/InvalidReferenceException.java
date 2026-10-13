package com.ucb.sakila.businessservice.shared;

public class InvalidReferenceException extends BusinessServiceException {
    public InvalidReferenceException(String code, String message) {
        super(code, 400, message);
    }
}

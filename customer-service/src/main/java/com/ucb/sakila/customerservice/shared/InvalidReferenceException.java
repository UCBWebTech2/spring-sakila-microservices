package com.ucb.sakila.customerservice.shared;

public class InvalidReferenceException extends CustomerServiceException {
    public InvalidReferenceException(String code, String message) {
        super(code, 400, message);
    }
}

package com.ucb.sakila.customerservice.shared;

public class NotFoundException extends CustomerServiceException {
    public NotFoundException(String code, String message) {
        super(code, 404, message);
    }
}

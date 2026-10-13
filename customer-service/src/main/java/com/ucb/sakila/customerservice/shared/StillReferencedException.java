package com.ucb.sakila.customerservice.shared;

public class StillReferencedException extends CustomerServiceException {
    public StillReferencedException(String message) {
        super("STILL_REFERENCED", 409, message);
    }
}

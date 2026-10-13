package com.ucb.sakila.businessservice.shared;

public class StillReferencedException extends BusinessServiceException {
    public StillReferencedException(String message) {
        super("STILL_REFERENCED", 409, message);
    }
}

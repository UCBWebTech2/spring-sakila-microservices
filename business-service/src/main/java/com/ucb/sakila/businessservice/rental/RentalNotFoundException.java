package com.ucb.sakila.businessservice.rental;

import com.ucb.sakila.businessservice.shared.BusinessServiceException;

public class RentalNotFoundException extends BusinessServiceException {
    public RentalNotFoundException() {
        super("RENTAL_NOT_FOUND", 404, "Rental not found.");
    }
}

package com.ucb.sakila.customerservice.customer;

import com.ucb.sakila.customerservice.shared.InvalidReferenceException;

public class InvalidAddressIdException extends InvalidReferenceException {
    public InvalidAddressIdException(Integer addressId) {
        super("INVALID_ADDRESS_ID", "Address " + addressId + " does not exist.");
    }
}

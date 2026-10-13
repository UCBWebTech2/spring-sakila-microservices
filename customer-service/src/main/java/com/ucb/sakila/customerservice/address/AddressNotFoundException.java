package com.ucb.sakila.customerservice.address;

import com.ucb.sakila.customerservice.shared.NotFoundException;

public class AddressNotFoundException extends NotFoundException {
    public AddressNotFoundException() {
        super("ADDRESS_NOT_FOUND", "Address not found.");
    }
}

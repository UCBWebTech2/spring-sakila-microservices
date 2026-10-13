package com.ucb.sakila.customerservice.customer;

import com.ucb.sakila.customerservice.shared.NotFoundException;

public class CustomerNotFoundException extends NotFoundException {
    public CustomerNotFoundException() {
        super("CUSTOMER_NOT_FOUND", "Customer not found.");
    }
}

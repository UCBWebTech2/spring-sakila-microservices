package com.ucb.sakila.customerservice.country;

import com.ucb.sakila.customerservice.shared.NotFoundException;

public class CountryNotFoundException extends NotFoundException {
    public CountryNotFoundException() {
        super("COUNTRY_NOT_FOUND", "Country not found.");
    }
}

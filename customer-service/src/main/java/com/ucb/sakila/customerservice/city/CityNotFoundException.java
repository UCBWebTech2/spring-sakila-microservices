package com.ucb.sakila.customerservice.city;

import com.ucb.sakila.customerservice.shared.NotFoundException;

public class CityNotFoundException extends NotFoundException {
    public CityNotFoundException() {
        super("CITY_NOT_FOUND", "City not found.");
    }
}

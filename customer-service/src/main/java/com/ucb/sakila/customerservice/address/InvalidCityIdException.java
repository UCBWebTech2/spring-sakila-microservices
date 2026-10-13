package com.ucb.sakila.customerservice.address;

import com.ucb.sakila.customerservice.shared.InvalidReferenceException;

public class InvalidCityIdException extends InvalidReferenceException {
    public InvalidCityIdException(Integer cityId) {
        super("INVALID_CITY_ID", "City " + cityId + " does not exist.");
    }
}

package com.ucb.sakila.customerservice.city;

import com.ucb.sakila.customerservice.shared.InvalidReferenceException;

public class InvalidCountryIdException extends InvalidReferenceException {
    public InvalidCountryIdException(Integer countryId) {
        super("INVALID_COUNTRY_ID", "Country " + countryId + " does not exist.");
    }
}

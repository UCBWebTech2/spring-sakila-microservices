package com.ucb.sakila.businessservice.store;

import com.ucb.sakila.businessservice.shared.BusinessServiceException;

public class StoreNotFoundException extends BusinessServiceException {
    public StoreNotFoundException() {
        super("STORE_NOT_FOUND", 404, "Store not found.");
    }
}

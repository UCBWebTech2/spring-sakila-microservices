package com.ucb.sakila.businessservice.staff;

import com.ucb.sakila.businessservice.shared.BusinessServiceException;

public class StaffNotFoundException extends BusinessServiceException {
    public StaffNotFoundException() {
        super("STAFF_NOT_FOUND", 404, "Staff not found.");
    }
}

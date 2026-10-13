package com.ucb.sakila.businessservice.staff;

import com.ucb.sakila.businessservice.shared.BusinessServiceException;

public class StaffAlreadyExistsException extends BusinessServiceException {
    public StaffAlreadyExistsException() {
        super("STAFF_ALREADY_EXISTS", 409, "A staff member with this username already exists.");
    }
}

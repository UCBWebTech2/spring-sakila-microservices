package com.ucb.sakila.businessservice.staff;

import com.ucb.sakila.businessservice.shared.BusinessServiceException;

public class InvalidCredentialsException extends BusinessServiceException {
    public InvalidCredentialsException() {
        // Mismo mensaje para "no existe", "contraseña incorrecta" e "inactivo", a propósito.
        super("INVALID_CREDENTIALS", 401, "Invalid credentials.");
    }
}

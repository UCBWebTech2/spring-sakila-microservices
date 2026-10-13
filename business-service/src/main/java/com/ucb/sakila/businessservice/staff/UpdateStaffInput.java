package com.ucb.sakila.businessservice.staff;

// Todos opcionales: solo se aplica el campo que venga no-nulo (patch parcial).
public record UpdateStaffInput(
        String firstName,
        String lastName,
        Integer addressId,
        String email,
        Integer storeId,
        Boolean active,
        String username) {
}

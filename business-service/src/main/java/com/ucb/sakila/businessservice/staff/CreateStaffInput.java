package com.ucb.sakila.businessservice.staff;

public record CreateStaffInput(
        String firstName,
        String lastName,
        Integer addressId,
        String email,
        Integer storeId,
        String username,
        String password) {
}

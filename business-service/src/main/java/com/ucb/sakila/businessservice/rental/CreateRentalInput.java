package com.ucb.sakila.businessservice.rental;

// rentalDate es ISO-8601 como String; RentalService la parsea a LocalDateTime.
public record CreateRentalInput(
        String rentalDate,
        Integer inventoryId,
        Integer customerId,
        Integer staffId) {
}

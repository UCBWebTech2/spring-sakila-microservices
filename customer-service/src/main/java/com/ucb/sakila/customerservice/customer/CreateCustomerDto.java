package com.ucb.sakila.customerservice.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomerDto(
        @NotNull Integer storeId,
        @NotBlank @Size(max = 45) String firstName,
        @NotBlank @Size(max = 45) String lastName,
        @Email @Size(max = 50) String email,
        @NotNull Integer addressId) {
}

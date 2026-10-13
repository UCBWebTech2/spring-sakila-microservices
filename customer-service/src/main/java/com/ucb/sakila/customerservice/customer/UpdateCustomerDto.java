package com.ucb.sakila.customerservice.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateCustomerDto(
        Integer id,
        Integer storeId,
        @Size(max = 45) String firstName,
        @Size(max = 45) String lastName,
        @Email @Size(max = 50) String email,
        Integer addressId,
        Boolean active) {
}

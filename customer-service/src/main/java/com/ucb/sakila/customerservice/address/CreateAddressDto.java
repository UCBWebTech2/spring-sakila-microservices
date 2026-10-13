package com.ucb.sakila.customerservice.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAddressDto(
        @NotBlank @Size(max = 50) String address,
        @Size(max = 50) String address2,
        @NotBlank @Size(max = 20) String district,
        @NotNull Integer cityId,
        @Size(max = 10) String postalCode,
        @NotBlank @Size(max = 20) String phone) {
}

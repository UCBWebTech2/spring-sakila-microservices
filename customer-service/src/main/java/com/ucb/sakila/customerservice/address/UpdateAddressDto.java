package com.ucb.sakila.customerservice.address;

import jakarta.validation.constraints.Size;

public record UpdateAddressDto(
        Integer id,
        @Size(max = 50) String address,
        @Size(max = 50) String address2,
        @Size(max = 20) String district,
        Integer cityId,
        @Size(max = 10) String postalCode,
        @Size(max = 20) String phone) {
}

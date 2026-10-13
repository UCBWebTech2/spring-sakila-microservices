package com.ucb.sakila.customerservice.country;

import jakarta.validation.constraints.Size;

public record UpdateCountryDto(Integer id, @Size(max = 50) String country) {
}

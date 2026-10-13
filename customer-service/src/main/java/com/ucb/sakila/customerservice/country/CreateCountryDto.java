package com.ucb.sakila.customerservice.country;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCountryDto(@NotBlank @Size(max = 50) String country) {
}

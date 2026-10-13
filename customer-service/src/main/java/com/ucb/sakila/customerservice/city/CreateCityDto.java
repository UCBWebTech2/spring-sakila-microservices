package com.ucb.sakila.customerservice.city;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCityDto(@NotBlank @Size(max = 50) String city, @NotNull Integer countryId) {
}

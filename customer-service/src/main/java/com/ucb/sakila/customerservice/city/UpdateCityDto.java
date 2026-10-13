package com.ucb.sakila.customerservice.city;

import jakarta.validation.constraints.Size;

public record UpdateCityDto(Integer id, @Size(max = 50) String city, Integer countryId) {
}

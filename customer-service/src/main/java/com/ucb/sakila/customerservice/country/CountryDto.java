package com.ucb.sakila.customerservice.country;

public record CountryDto(Integer id, String country, String lastUpdate) {

    public static CountryDto fromEntity(Country entity) {
        return new CountryDto(entity.getId(), entity.getCountry(), entity.getLastUpdate().toString());
    }
}

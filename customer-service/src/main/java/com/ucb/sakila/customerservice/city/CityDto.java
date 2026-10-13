package com.ucb.sakila.customerservice.city;

public record CityDto(Integer id, String city, Integer countryId, String lastUpdate) {

    public static CityDto fromEntity(City entity) {
        return new CityDto(entity.getId(), entity.getCity(), entity.getCountryId(), entity.getLastUpdate().toString());
    }
}

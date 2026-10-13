package com.ucb.sakila.customerservice.address;

public record AddressDto(Integer id, String address, String address2, String district, Integer cityId,
        String postalCode, String phone, String lastUpdate) {

    public static AddressDto fromEntity(Address entity) {
        return new AddressDto(entity.getId(), entity.getAddress(), entity.getAddress2(), entity.getDistrict(),
                entity.getCityId(), entity.getPostalCode(), entity.getPhone(), entity.getLastUpdate().toString());
    }
}

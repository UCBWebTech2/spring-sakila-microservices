package com.ucb.sakila.customerservice.customer;

public record CustomerDto(Integer id, Integer storeId, String firstName, String lastName, String email,
        Integer addressId, boolean active, String createDate, String lastUpdate) {

    public static CustomerDto fromEntity(Customer entity) {
        return new CustomerDto(entity.getId(), entity.getStoreId(), entity.getFirstName(), entity.getLastName(),
                entity.getEmail(), entity.getAddressId(), entity.isActivebool(), entity.getCreateDate().toString(),
                entity.getLastUpdate() == null ? null : entity.getLastUpdate().toString());
    }
}

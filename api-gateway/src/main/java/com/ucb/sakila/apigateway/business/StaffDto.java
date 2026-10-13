package com.ucb.sakila.apigateway.business;

import java.util.Map;

public record StaffDto(int id, String firstName, String lastName, int addressId, String email,
        int storeId, boolean active, String username, String lastUpdate) {

    @SuppressWarnings("unchecked")
    static StaffDto from(Map<String, Object> staff) {
        Map<String, Object> store = (Map<String, Object>) staff.get("store");
        return new StaffDto(
                Integer.parseInt(staff.get("id").toString()),
                (String) staff.get("firstName"),
                (String) staff.get("lastName"),
                (Integer) staff.get("addressId"),
                (String) staff.get("email"),
                Integer.parseInt(store.get("id").toString()),
                (Boolean) staff.get("active"),
                (String) staff.get("username"),
                (String) staff.get("lastUpdate"));
    }
}

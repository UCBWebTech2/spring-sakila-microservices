package com.ucb.sakila.apigateway.business;

import java.util.Map;

public record StoreDto(int id, int managerStaffId, int addressId, String lastUpdate) {

    @SuppressWarnings("unchecked")
    static StoreDto from(Map<String, Object> store) {
        Map<String, Object> manager = (Map<String, Object>) store.get("manager");
        return new StoreDto(
                Integer.parseInt(store.get("id").toString()),
                Integer.parseInt(manager.get("id").toString()),
                (Integer) store.get("addressId"),
                (String) store.get("lastUpdate"));
    }
}

package com.ucb.sakila.apigateway.inventory;

public record CategoryDto(int id, String name, String lastUpdate) {

    static CategoryDto from(com.ucb.sakila.apigateway.inventory.ws.generated.Category xml) {
        return new CategoryDto(xml.getId(), xml.getName(), xml.getLastUpdate().toString());
    }
}

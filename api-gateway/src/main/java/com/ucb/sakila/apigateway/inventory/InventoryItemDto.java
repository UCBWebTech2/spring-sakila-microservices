package com.ucb.sakila.apigateway.inventory;

public record InventoryItemDto(int id, int filmId, int storeId, String lastUpdate) {

    static InventoryItemDto from(com.ucb.sakila.apigateway.inventory.ws.generated.InventoryItem xml) {
        return new InventoryItemDto(xml.getId(), xml.getFilmId(), xml.getStoreId(), xml.getLastUpdate().toString());
    }
}

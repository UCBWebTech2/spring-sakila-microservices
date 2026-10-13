package com.ucb.sakila.apigateway.inventory;

public record LanguageDto(int id, String name, String lastUpdate) {

    static LanguageDto from(com.ucb.sakila.apigateway.inventory.ws.generated.Language xml) {
        return new LanguageDto(xml.getId(), xml.getName(), xml.getLastUpdate().toString());
    }
}

package com.ucb.sakila.apigateway.inventory;

public record ActorDto(int id, String firstName, String lastName, String lastUpdate) {

    static ActorDto from(com.ucb.sakila.apigateway.inventory.ws.generated.Actor xml) {
        return new ActorDto(xml.getId(), xml.getFirstName(), xml.getLastName(), xml.getLastUpdate().toString());
    }
}

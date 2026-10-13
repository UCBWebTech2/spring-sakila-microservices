package com.ucb.sakila.apigateway.inventory;

import java.math.BigDecimal;
import java.util.List;

public record FilmDto(
        int id, String title, String description, int languageId, Integer originalLanguageId,
        int rentalDuration, BigDecimal rentalRate, Integer length, BigDecimal replacementCost,
        String lastUpdate, List<Integer> actorIds, List<Integer> categoryIds) {

    static FilmDto from(com.ucb.sakila.apigateway.inventory.ws.generated.Film xml) {
        return new FilmDto(xml.getId(), xml.getTitle(), xml.getDescription(), xml.getLanguageId(),
                xml.getOriginalLanguageId(), xml.getRentalDuration(), xml.getRentalRate(), xml.getLength(),
                xml.getReplacementCost(), xml.getLastUpdate().toString(), xml.getActorId(), xml.getCategoryId());
    }
}

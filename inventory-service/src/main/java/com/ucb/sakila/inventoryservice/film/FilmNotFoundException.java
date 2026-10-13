package com.ucb.sakila.inventoryservice.film;

import com.ucb.sakila.inventoryservice.shared.InventoryServiceException;

public class FilmNotFoundException extends InventoryServiceException {
    public FilmNotFoundException() {
        super("FILM_NOT_FOUND", "Film not found.");
    }
}

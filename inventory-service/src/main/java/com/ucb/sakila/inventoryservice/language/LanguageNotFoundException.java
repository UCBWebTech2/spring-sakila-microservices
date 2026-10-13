package com.ucb.sakila.inventoryservice.language;

import com.ucb.sakila.inventoryservice.shared.InventoryServiceException;

public class LanguageNotFoundException extends InventoryServiceException {
    public LanguageNotFoundException() {
        super("LANGUAGE_NOT_FOUND", "Language not found.");
    }
}

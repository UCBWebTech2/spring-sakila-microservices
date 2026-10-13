package com.ucb.sakila.inventoryservice.category;

import com.ucb.sakila.inventoryservice.shared.InventoryServiceException;

public class CategoryNotFoundException extends InventoryServiceException {
    public CategoryNotFoundException() {
        super("CATEGORY_NOT_FOUND", "Category not found.");
    }
}

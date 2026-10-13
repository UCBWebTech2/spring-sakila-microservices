package com.ucb.sakila.inventoryservice.inventory;

import com.ucb.sakila.inventoryservice.shared.InventoryServiceException;

public class InventoryNotFoundException extends InventoryServiceException {
    public InventoryNotFoundException() {
        super("INVENTORY_NOT_FOUND", "Inventory item not found.");
    }
}

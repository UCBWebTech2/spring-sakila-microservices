package com.ucb.sakila.inventoryservice.actor;

import com.ucb.sakila.inventoryservice.shared.InventoryServiceException;

public class ActorNotFoundException extends InventoryServiceException {
    public ActorNotFoundException() {
        super("ACTOR_NOT_FOUND", "Actor not found.");
    }
}

package com.ucb.sakila.inventoryservice.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryItem, Integer> {
}

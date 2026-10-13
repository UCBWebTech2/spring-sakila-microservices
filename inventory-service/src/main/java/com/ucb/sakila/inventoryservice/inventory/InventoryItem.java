package com.ucb.sakila.inventoryservice.inventory;

import com.ucb.sakila.inventoryservice.film.Film;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Nombrada InventoryItem (no Inventory) para no chocar con el paquete/módulo del mismo nombre.
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "film_id", nullable = false)
    private Film film;

    // store vive en business-service — columna plana, nunca @ManyToOne.
    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;
}

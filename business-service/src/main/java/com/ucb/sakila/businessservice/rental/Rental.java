package com.ucb.sakila.businessservice.rental;

import com.ucb.sakila.businessservice.staff.Staff;
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

@Entity
@Table(name = "rental")
@Getter
@Setter
@NoArgsConstructor
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rental_id")
    private Integer id;

    @Column(name = "rental_date", nullable = false)
    private LocalDateTime rentalDate;

    // inventory vive en inventory-service — columna plana, nunca @ManyToOne.
    @Column(name = "inventory_id", nullable = false)
    private Integer inventoryId;

    // customer vive en customer-service — columna plana, nunca @ManyToOne.
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;
}

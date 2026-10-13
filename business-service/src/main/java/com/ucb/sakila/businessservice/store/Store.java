package com.ucb.sakila.businessservice.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store")
@Getter
@Setter
@NoArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Integer id;

    // store<->staff se referencian mutuamente; solo Staff.store es relación JPA real.
    @Column(name = "manager_staff_id", nullable = false)
    private Integer managerStaffId;

    // address vive en otro microservicio (customer-service) — columna plana, nunca @ManyToOne.
    @Column(name = "address_id", nullable = false)
    private Integer addressId;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;
}

package com.ucb.sakila.customerservice.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer")
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer id;

    // store vive en business-service — columna simple, no relación JPA; su FK sigue en el schema.
    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    @Column(name = "first_name", length = 45, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 45, nullable = false)
    private String lastName;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "address_id", nullable = false)
    private Integer addressId;

    @Column(name = "activebool", nullable = false)
    private boolean activebool;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    // flag legacy 0/1 que Sakila mantiene junto a activebool — se sincroniza al escribir.
    @Column(name = "active")
    private Integer active;
}

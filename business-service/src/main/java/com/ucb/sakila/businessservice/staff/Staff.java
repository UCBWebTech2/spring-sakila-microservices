package com.ucb.sakila.businessservice.staff;

import com.ucb.sakila.businessservice.store.Store;
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
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 45)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 45)
    private String lastName;

    // address vive en otro microservicio (customer-service) — columna plana, nunca @ManyToOne.
    @Column(name = "address_id", nullable = false)
    private Integer addressId;

    @Column(name = "email", length = 50)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "username", nullable = false, length = 16)
    private String username;

    // Nunca se expone por GraphQL; solo verifyCredentials() la compara.
    @Column(name = "password")
    private String password;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

    // picture (bytea) queda fuera del mapeo — solo escalares simples.
}

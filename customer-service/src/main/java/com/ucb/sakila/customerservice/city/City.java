package com.ucb.sakila.customerservice.city;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

// FK como columna simple, no relación JPA.
@Entity
@Table(name = "city")
@Getter
@Setter
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")
    private Integer id;

    @Column(name = "city", length = 50, nullable = false)
    private String city;

    @Column(name = "country_id", nullable = false)
    private Integer countryId;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;
}

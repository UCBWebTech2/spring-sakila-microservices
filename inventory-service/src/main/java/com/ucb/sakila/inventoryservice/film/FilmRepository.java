package com.ucb.sakila.inventoryservice.film;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmRepository extends JpaRepository<Film, Integer> {

    // Evita LazyInitializationException al armar el XML fuera de la transacción.
    @Override
    @EntityGraph(attributePaths = {"actors", "categories"})
    Optional<Film> findById(Integer id);

    @Override
    @EntityGraph(attributePaths = {"actors", "categories"})
    Page<Film> findAll(Pageable pageable);
}

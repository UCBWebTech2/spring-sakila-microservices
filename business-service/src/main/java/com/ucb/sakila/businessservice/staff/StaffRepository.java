package com.ucb.sakila.businessservice.staff;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByUsername(String username);

    boolean existsByUsername(String username);
}

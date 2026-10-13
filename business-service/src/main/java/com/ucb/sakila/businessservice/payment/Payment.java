package com.ucb.sakila.businessservice.payment;

import com.ucb.sakila.businessservice.rental.Rental;
import com.ucb.sakila.businessservice.staff.Staff;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    // SEQUENCE, no IDENTITY: payment tiene RULES de partición que rechazan el RETURNING de IDENTITY.
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_payment_id_seq")
    @SequenceGenerator(name = "payment_payment_id_seq", sequenceName = "payment_payment_id_seq", allocationSize = 1)
    @Column(name = "payment_id")
    private Integer id;

    // customer vive en customer-service — columna plana, nunca @ManyToOne.
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    // NOT NULL aunque la FK real sea ON DELETE SET NULL (inconsistencia del dump de Sakila).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @Column(name = "amount", nullable = false, precision = 5, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
}

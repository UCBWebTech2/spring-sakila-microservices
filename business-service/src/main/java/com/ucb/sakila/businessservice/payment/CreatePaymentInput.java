package com.ucb.sakila.businessservice.payment;

// amount es Double (scalar Float de GraphQL); PaymentService lo convierte a BigDecimal.
public record CreatePaymentInput(
        Integer customerId,
        Integer staffId,
        Integer rentalId, // requerido: payment.rental_id es NOT NULL en la base real
        Double amount,
        String paymentDate) {
}

package com.ucb.sakila.apigateway.business;

import java.util.Map;

public record PaymentDto(int id, int customerId, int staffId, int rentalId, double amount, String paymentDate) {

    @SuppressWarnings("unchecked")
    static PaymentDto from(Map<String, Object> payment) {
        Map<String, Object> staff = (Map<String, Object>) payment.get("staff");
        Map<String, Object> rental = (Map<String, Object>) payment.get("rental");
        return new PaymentDto(
                Integer.parseInt(payment.get("id").toString()),
                (Integer) payment.get("customerId"),
                Integer.parseInt(staff.get("id").toString()),
                Integer.parseInt(rental.get("id").toString()),
                ((Number) payment.get("amount")).doubleValue(),
                (String) payment.get("paymentDate"));
    }
}

package com.ucb.sakila.businessservice.payment;

import com.ucb.sakila.businessservice.shared.BusinessServiceException;

public class PaymentNotFoundException extends BusinessServiceException {
    public PaymentNotFoundException() {
        super("PAYMENT_NOT_FOUND", 404, "Payment not found.");
    }
}

package com.ucb.sakila.customerservice.shared;

import java.util.function.Supplier;

public final class AmqpErrors {

    private AmqpErrors() {
    }

    public static Object handle(Supplier<?> action) {
        try {
            return action.get();
        } catch (CustomerServiceException ex) {
            return new RpcErrorBody(ex.getStatusCode(), ex.getCode(), ex.getMessage());
        } catch (jakarta.validation.ConstraintViolationException ex) {
            return new RpcErrorBody(400, "VALIDATION_ERROR", ex.getMessage());
        }
    }
}

package com.ucb.sakila.customerservice.shared;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// gRPC no pasa por @Valid de Spring MVC — se valida a mano antes de llamar al service.
@Component
@RequiredArgsConstructor
public class GrpcValidation {

    private final Validator validator;

    public <T> T check(T dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        return dto;
    }
}

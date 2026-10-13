package com.ucb.sakila.customerservice.shared;

import io.grpc.Status;
import org.springframework.grpc.server.advice.GrpcAdvice;
import org.springframework.grpc.server.advice.GrpcExceptionHandler;

// El código de dominio viaja como "<CODE>: texto" dentro del mensaje de status.
@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(NotFoundException.class)
    public Status handleNotFound(NotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getCode() + ": " + ex.getMessage());
    }

    @GrpcExceptionHandler(InvalidReferenceException.class)
    public Status handleInvalidReference(InvalidReferenceException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getCode() + ": " + ex.getMessage());
    }

    @GrpcExceptionHandler(StillReferencedException.class)
    public Status handleStillReferenced(StillReferencedException ex) {
        return Status.FAILED_PRECONDITION.withDescription(ex.getCode() + ": " + ex.getMessage());
    }

    @GrpcExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public Status handleValidation(jakarta.validation.ConstraintViolationException ex) {
        return Status.INVALID_ARGUMENT.withDescription("VALIDATION_ERROR: " + ex.getMessage());
    }
}

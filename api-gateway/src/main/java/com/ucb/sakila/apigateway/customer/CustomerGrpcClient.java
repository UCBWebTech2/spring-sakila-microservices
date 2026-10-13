package com.ucb.sakila.apigateway.customer;

import com.ucb.sakila.apigateway.customer.grpc.generated.AddressesServiceGrpc;
import com.ucb.sakila.apigateway.customer.grpc.generated.CustomersServiceGrpc;
import com.ucb.sakila.apigateway.shared.ApiException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerGrpcClient {

    private final CustomerGrpcChannel channel;

    public CustomersServiceGrpc.CustomersServiceBlockingStub customers() {
        return CustomersServiceGrpc.newBlockingStub(channel.get());
    }

    public AddressesServiceGrpc.AddressesServiceBlockingStub addresses() {
        return AddressesServiceGrpc.newBlockingStub(channel.get());
    }

    public <T> T call(Supplier<T> action) {
        try {
            return action.get();
        } catch (StatusRuntimeException ex) {
            throw translate(ex);
        }
    }

    private ApiException translate(StatusRuntimeException ex) {
        String description = ex.getStatus().getDescription();
        int sep = description == null ? -1 : description.indexOf(": ");
        String code = sep > 0 ? description.substring(0, sep) : "CUSTOMER_SERVICE_ERROR";
        String message = sep > 0 ? description.substring(sep + 2) : (description == null ? "" : description);
        return new ApiException(statusFor(ex.getStatus().getCode()), code, message);
    }

    private HttpStatus statusFor(Status.Code code) {
        return switch (code) {
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FAILED_PRECONDITION -> HttpStatus.CONFLICT;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

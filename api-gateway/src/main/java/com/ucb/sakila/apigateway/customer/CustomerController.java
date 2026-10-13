package com.ucb.sakila.apigateway.customer;

import com.ucb.sakila.apigateway.customer.grpc.generated.ById;
import com.ucb.sakila.apigateway.customer.grpc.generated.CreateCustomerRequest;
import com.ucb.sakila.apigateway.customer.grpc.generated.PaginationRequest;
import com.ucb.sakila.apigateway.customer.grpc.generated.UpdateCustomerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerGrpcClient client;

    public record CreateCustomerBody(int storeId, String firstName, String lastName, String email, int addressId) {
    }

    public record UpdateCustomerBody(Integer storeId, String firstName, String lastName, String email,
            Integer addressId, Boolean active) {
    }

    @GetMapping
    public PageResponse<CustomerDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var request = PaginationRequest.newBuilder().setPage(page).setLimit(limit).build();
        var response = client.call(() -> client.customers().list(request));
        return new PageResponse<>(response.getDataList().stream().map(CustomerDto::from).toList(), PageResponse.toMeta(response.getMeta()));
    }

    @GetMapping("/{id}")
    public CustomerDto get(@PathVariable int id) {
        var request = ById.newBuilder().setId(id).build();
        return CustomerDto.from(client.call(() -> client.customers().findOne(request)));
    }

    @PostMapping
    public CustomerDto create(@RequestBody CreateCustomerBody body) {
        var builder = CreateCustomerRequest.newBuilder()
                .setStoreId(body.storeId()).setFirstName(body.firstName()).setLastName(body.lastName())
                .setAddressId(body.addressId());
        if (body.email() != null) builder.setEmail(body.email());
        var request = builder.build();
        return CustomerDto.from(client.call(() -> client.customers().create(request)));
    }

    @PatchMapping("/{id}")
    public CustomerDto update(@PathVariable int id, @RequestBody UpdateCustomerBody body) {
        var builder = UpdateCustomerRequest.newBuilder().setId(id);
        if (body.storeId() != null) builder.setStoreId(body.storeId());
        if (body.firstName() != null) builder.setFirstName(body.firstName());
        if (body.lastName() != null) builder.setLastName(body.lastName());
        if (body.email() != null) builder.setEmail(body.email());
        if (body.addressId() != null) builder.setAddressId(body.addressId());
        if (body.active() != null) builder.setActive(body.active());
        var request = builder.build();
        return CustomerDto.from(client.call(() -> client.customers().update(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int id) {
        var request = ById.newBuilder().setId(id).build();
        client.call(() -> client.customers().remove(request));
    }
}

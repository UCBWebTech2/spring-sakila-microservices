package com.ucb.sakila.apigateway.customer;

import com.ucb.sakila.apigateway.customer.grpc.generated.ById;
import com.ucb.sakila.apigateway.customer.grpc.generated.CreateAddressRequest;
import com.ucb.sakila.apigateway.customer.grpc.generated.PaginationRequest;
import com.ucb.sakila.apigateway.customer.grpc.generated.UpdateAddressRequest;
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
@RequestMapping("/customer/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final CustomerGrpcClient client;

    public record CreateAddressBody(String address, String address2, String district, int cityId,
            String postalCode, String phone) {
    }

    public record UpdateAddressBody(String address, String address2, String district, Integer cityId,
            String postalCode, String phone) {
    }

    @GetMapping
    public PageResponse<AddressDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var request = PaginationRequest.newBuilder().setPage(page).setLimit(limit).build();
        var response = client.call(() -> client.addresses().list(request));
        return new PageResponse<>(response.getDataList().stream().map(AddressDto::from).toList(), PageResponse.toMeta(response.getMeta()));
    }

    @GetMapping("/{id}")
    public AddressDto get(@PathVariable int id) {
        var request = ById.newBuilder().setId(id).build();
        return AddressDto.from(client.call(() -> client.addresses().findOne(request)));
    }

    @PostMapping
    public AddressDto create(@RequestBody CreateAddressBody body) {
        var builder = CreateAddressRequest.newBuilder()
                .setAddress(body.address()).setDistrict(body.district()).setCityId(body.cityId()).setPhone(body.phone());
        if (body.address2() != null) builder.setAddress2(body.address2());
        if (body.postalCode() != null) builder.setPostalCode(body.postalCode());
        var request = builder.build();
        return AddressDto.from(client.call(() -> client.addresses().create(request)));
    }

    @PatchMapping("/{id}")
    public AddressDto update(@PathVariable int id, @RequestBody UpdateAddressBody body) {
        var builder = UpdateAddressRequest.newBuilder().setId(id);
        if (body.address() != null) builder.setAddress(body.address());
        if (body.address2() != null) builder.setAddress2(body.address2());
        if (body.district() != null) builder.setDistrict(body.district());
        if (body.cityId() != null) builder.setCityId(body.cityId());
        if (body.postalCode() != null) builder.setPostalCode(body.postalCode());
        if (body.phone() != null) builder.setPhone(body.phone());
        var request = builder.build();
        return AddressDto.from(client.call(() -> client.addresses().update(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int id) {
        var request = ById.newBuilder().setId(id).build();
        client.call(() -> client.addresses().remove(request));
    }
}

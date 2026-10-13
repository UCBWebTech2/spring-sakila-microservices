package com.ucb.sakila.apigateway.customer;

public record CustomerDto(int id, int storeId, String firstName, String lastName, String email,
        int addressId, boolean active, String createDate, String lastUpdate) {

    static CustomerDto from(com.ucb.sakila.apigateway.customer.grpc.generated.Customer proto) {
        return new CustomerDto(proto.getId(), proto.getStoreId(), proto.getFirstName(), proto.getLastName(),
                proto.hasEmail() ? proto.getEmail() : null, proto.getAddressId(), proto.getActive(),
                proto.getCreateDate(), proto.getLastUpdate());
    }
}

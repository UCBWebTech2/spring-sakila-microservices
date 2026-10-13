package com.ucb.sakila.apigateway.customer;

public record AddressDto(int id, String address, String address2, String district, int cityId,
        String postalCode, String phone, String lastUpdate) {

    static AddressDto from(com.ucb.sakila.apigateway.customer.grpc.generated.Address proto) {
        return new AddressDto(proto.getId(), proto.getAddress(),
                proto.hasAddress2() ? proto.getAddress2() : null, proto.getDistrict(), proto.getCityId(),
                proto.hasPostalCode() ? proto.getPostalCode() : null, proto.getPhone(), proto.getLastUpdate());
    }
}

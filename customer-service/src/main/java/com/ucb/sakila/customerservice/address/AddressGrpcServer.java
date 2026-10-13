package com.ucb.sakila.customerservice.address;

import com.ucb.sakila.customerservice.grpc.generated.AddressesServiceGrpc;
import com.ucb.sakila.customerservice.grpc.generated.ById;
import com.ucb.sakila.customerservice.grpc.generated.CreateAddressRequest;
import com.ucb.sakila.customerservice.grpc.generated.Empty;
import com.ucb.sakila.customerservice.grpc.generated.ListAddressesResponse;
import com.ucb.sakila.customerservice.grpc.generated.PaginationMeta;
import com.ucb.sakila.customerservice.grpc.generated.PaginationRequest;
import com.ucb.sakila.customerservice.grpc.generated.UpdateAddressRequest;
import com.ucb.sakila.customerservice.shared.GrpcValidation;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AddressGrpcServer extends AddressesServiceGrpc.AddressesServiceImplBase {

    private final AddressService service;
    private final GrpcValidation validation;

    @Override
    public void list(PaginationRequest request, StreamObserver<ListAddressesResponse> observer) {
        var result = service.findAll(request.getPage(), request.getLimit());
        observer.onNext(ListAddressesResponse.newBuilder()
                .addAllData(result.data().stream().map(AddressGrpcServer::toProto).toList())
                .setMeta(toProtoMeta(result.meta()))
                .build());
        observer.onCompleted();
    }

    @Override
    public void findOne(ById request, StreamObserver<com.ucb.sakila.customerservice.grpc.generated.Address> observer) {
        observer.onNext(toProto(service.findOneById(request.getId())));
        observer.onCompleted();
    }

    @Override
    public void create(CreateAddressRequest request, StreamObserver<com.ucb.sakila.customerservice.grpc.generated.Address> observer) {
        var dto = validation.check(new CreateAddressDto(
                request.getAddress(),
                request.hasAddress2() ? request.getAddress2() : null,
                request.getDistrict(),
                request.getCityId(),
                request.hasPostalCode() ? request.getPostalCode() : null,
                request.getPhone()));
        observer.onNext(toProto(service.create(dto)));
        observer.onCompleted();
    }

    @Override
    public void update(UpdateAddressRequest request, StreamObserver<com.ucb.sakila.customerservice.grpc.generated.Address> observer) {
        var dto = validation.check(new UpdateAddressDto(
                request.getId(),
                request.hasAddress() ? request.getAddress() : null,
                request.hasAddress2() ? request.getAddress2() : null,
                request.hasDistrict() ? request.getDistrict() : null,
                request.hasCityId() ? request.getCityId() : null,
                request.hasPostalCode() ? request.getPostalCode() : null,
                request.hasPhone() ? request.getPhone() : null));
        observer.onNext(toProto(service.update(dto)));
        observer.onCompleted();
    }

    @Override
    public void remove(ById request, StreamObserver<Empty> observer) {
        service.remove(request.getId());
        observer.onNext(Empty.newBuilder().build());
        observer.onCompleted();
    }

    private static com.ucb.sakila.customerservice.grpc.generated.Address toProto(AddressDto dto) {
        var builder = com.ucb.sakila.customerservice.grpc.generated.Address.newBuilder()
                .setId(dto.id())
                .setAddress(dto.address())
                .setDistrict(dto.district())
                .setCityId(dto.cityId())
                .setPhone(dto.phone())
                .setLastUpdate(dto.lastUpdate());
        if (dto.address2() != null) builder.setAddress2(dto.address2());
        if (dto.postalCode() != null) builder.setPostalCode(dto.postalCode());
        return builder.build();
    }

    private static PaginationMeta toProtoMeta(com.ucb.sakila.customerservice.shared.PageInfo meta) {
        return PaginationMeta.newBuilder()
                .setPage(meta.page()).setLimit(meta.limit()).setTotal((int) meta.total()).setPages(meta.pages())
                .build();
    }
}

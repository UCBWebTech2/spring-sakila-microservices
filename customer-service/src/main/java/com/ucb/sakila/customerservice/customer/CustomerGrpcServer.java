package com.ucb.sakila.customerservice.customer;

import com.ucb.sakila.customerservice.grpc.generated.ById;
import com.ucb.sakila.customerservice.grpc.generated.CreateCustomerRequest;
import com.ucb.sakila.customerservice.grpc.generated.CustomersServiceGrpc;
import com.ucb.sakila.customerservice.grpc.generated.Empty;
import com.ucb.sakila.customerservice.grpc.generated.ListCustomersResponse;
import com.ucb.sakila.customerservice.grpc.generated.PaginationMeta;
import com.ucb.sakila.customerservice.grpc.generated.PaginationRequest;
import com.ucb.sakila.customerservice.grpc.generated.UpdateCustomerRequest;
import com.ucb.sakila.customerservice.shared.GrpcValidation;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class CustomerGrpcServer extends CustomersServiceGrpc.CustomersServiceImplBase {

    private final CustomerService service;
    private final GrpcValidation validation;

    @Override
    public void list(PaginationRequest request, StreamObserver<ListCustomersResponse> observer) {
        var result = service.findAll(request.getPage(), request.getLimit());
        observer.onNext(ListCustomersResponse.newBuilder()
                .addAllData(result.data().stream().map(CustomerGrpcServer::toProto).toList())
                .setMeta(toProtoMeta(result.meta()))
                .build());
        observer.onCompleted();
    }

    @Override
    public void findOne(ById request, StreamObserver<com.ucb.sakila.customerservice.grpc.generated.Customer> observer) {
        observer.onNext(toProto(service.findOneById(request.getId())));
        observer.onCompleted();
    }

    @Override
    public void create(CreateCustomerRequest request, StreamObserver<com.ucb.sakila.customerservice.grpc.generated.Customer> observer) {
        var dto = validation.check(new CreateCustomerDto(
                request.getStoreId(),
                request.getFirstName(),
                request.getLastName(),
                request.hasEmail() ? request.getEmail() : null,
                request.getAddressId()));
        observer.onNext(toProto(service.create(dto)));
        observer.onCompleted();
    }

    @Override
    public void update(UpdateCustomerRequest request, StreamObserver<com.ucb.sakila.customerservice.grpc.generated.Customer> observer) {
        var dto = validation.check(new UpdateCustomerDto(
                request.getId(),
                request.hasStoreId() ? request.getStoreId() : null,
                request.hasFirstName() ? request.getFirstName() : null,
                request.hasLastName() ? request.getLastName() : null,
                request.hasEmail() ? request.getEmail() : null,
                request.hasAddressId() ? request.getAddressId() : null,
                request.hasActive() ? request.getActive() : null));
        observer.onNext(toProto(service.update(dto)));
        observer.onCompleted();
    }

    @Override
    public void remove(ById request, StreamObserver<Empty> observer) {
        service.remove(request.getId());
        observer.onNext(Empty.newBuilder().build());
        observer.onCompleted();
    }

    private static com.ucb.sakila.customerservice.grpc.generated.Customer toProto(CustomerDto dto) {
        var builder = com.ucb.sakila.customerservice.grpc.generated.Customer.newBuilder()
                .setId(dto.id())
                .setStoreId(dto.storeId())
                .setFirstName(dto.firstName())
                .setLastName(dto.lastName())
                .setAddressId(dto.addressId())
                .setActive(dto.active())
                .setCreateDate(dto.createDate())
                .setLastUpdate(dto.lastUpdate() == null ? "" : dto.lastUpdate());
        if (dto.email() != null) builder.setEmail(dto.email());
        return builder.build();
    }

    private static PaginationMeta toProtoMeta(com.ucb.sakila.customerservice.shared.PageInfo meta) {
        return PaginationMeta.newBuilder()
                .setPage(meta.page()).setLimit(meta.limit()).setTotal((int) meta.total()).setPages(meta.pages())
                .build();
    }
}

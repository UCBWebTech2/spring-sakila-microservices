package com.ucb.sakila.apigateway.customer;

import java.util.List;

public record PageResponse<T>(List<T> data, PageMeta meta) {

    static PageMeta toMeta(com.ucb.sakila.apigateway.customer.grpc.generated.PaginationMeta proto) {
        return new PageMeta(proto.getPage(), proto.getLimit(), proto.getTotal(), proto.getPages());
    }
}

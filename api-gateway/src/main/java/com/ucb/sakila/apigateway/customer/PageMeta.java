package com.ucb.sakila.apigateway.customer;

public record PageMeta(int page, int limit, long total, int pages) {
}

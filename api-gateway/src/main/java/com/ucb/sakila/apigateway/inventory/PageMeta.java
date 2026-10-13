package com.ucb.sakila.apigateway.inventory;

public record PageMeta(int page, int limit, long total, int pages) {
}

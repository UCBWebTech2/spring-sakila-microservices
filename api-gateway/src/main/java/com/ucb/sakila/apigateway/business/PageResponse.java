package com.ucb.sakila.apigateway.business;

import java.util.List;

public record PageResponse<T>(List<T> data, PageMeta meta) {
}

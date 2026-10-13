package com.ucb.sakila.businessservice.shared;

import java.util.List;

/** Forma común de cada {@code *Connection} del schema GraphQL: {@code { data, meta } }. */
public record PageResult<T>(List<T> data, PageInfo meta) {
}

package com.ucb.sakila.customerservice.shared;

import java.util.List;

public record PageResult<T>(List<T> data, PageInfo meta) {
}

package com.ucb.sakila.inventoryservice.shared;

import java.util.List;

public record PageResult<T>(List<T> data, int page, int limit, long total) {

    public int pages() {
        return limit <= 0 ? 0 : (int) Math.ceil((double) total / limit);
    }
}

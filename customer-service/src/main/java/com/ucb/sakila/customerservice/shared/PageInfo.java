package com.ucb.sakila.customerservice.shared;

public record PageInfo(int page, int limit, long total, int pages) {

    public static PageInfo of(int page, int limit, long total) {
        int pages = limit <= 0 ? 0 : (int) Math.ceil((double) total / limit);
        return new PageInfo(page, limit, total, pages);
    }
}

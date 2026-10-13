package com.ucb.sakila.reportingservice.reports;

public record PageMeta(int page, int limit, long total, int pages) {

    static PageMeta from(PageResult result) {
        return new PageMeta(result.page(), result.limit(), result.total(), result.pages());
    }
}

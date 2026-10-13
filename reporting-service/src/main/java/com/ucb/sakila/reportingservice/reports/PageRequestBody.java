package com.ucb.sakila.reportingservice.reports;

public record PageRequestBody(Integer page, Integer limit) {

    int pageOrDefault() {
        return page == null ? 1 : page;
    }

    int limitOrDefault() {
        return limit == null ? 10 : limit;
    }
}

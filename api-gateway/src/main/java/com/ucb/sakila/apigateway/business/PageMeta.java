package com.ucb.sakila.apigateway.business;

public record PageMeta(int page, int limit, long total, int pages) {

    @SuppressWarnings("unchecked")
    static PageMeta from(Object meta) {
        var m = (java.util.Map<String, Object>) meta;
        return new PageMeta((Integer) m.get("page"), (Integer) m.get("limit"),
                ((Number) m.get("total")).longValue(), (Integer) m.get("pages"));
    }
}

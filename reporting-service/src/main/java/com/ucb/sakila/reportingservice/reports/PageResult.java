package com.ucb.sakila.reportingservice.reports;

import java.util.List;
import java.util.Map;

public record PageResult(List<Map<String, Object>> data, int page, int limit, long total) {

    public int pages() {
        return limit <= 0 ? 0 : (int) Math.ceil((double) total / limit);
    }
}

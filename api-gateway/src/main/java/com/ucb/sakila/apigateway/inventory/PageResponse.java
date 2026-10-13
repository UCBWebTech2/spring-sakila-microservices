package com.ucb.sakila.apigateway.inventory;

import com.ucb.sakila.apigateway.inventory.ws.generated.PageInfo;
import java.util.List;

public record PageResponse<T>(List<T> data, PageMeta meta) {

    static PageMeta toMeta(PageInfo xml) {
        return new PageMeta(xml.getPage(), xml.getLimit(), xml.getTotal(), xml.getPages());
    }
}

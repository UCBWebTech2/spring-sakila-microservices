package com.ucb.sakila.apigateway.inventory;

import com.ucb.sakila.apigateway.inventory.ws.generated.GetCategoryRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.GetCategoryResponse;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListCategoriesRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListCategoriesResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final InventorySoapClient client;

    @GetMapping
    public PageResponse<CategoryDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var request = new ListCategoriesRequest();
        request.setPage(page);
        request.setLimit(limit);
        ListCategoriesResponse response = client.call(request);
        List<CategoryDto> data = response.getData().stream().map(CategoryDto::from).toList();
        return new PageResponse<>(data, PageResponse.toMeta(response.getMeta()));
    }

    @GetMapping("/{id}")
    public CategoryDto get(@PathVariable int id) {
        var request = new GetCategoryRequest();
        request.setId(id);
        GetCategoryResponse response = client.call(request);
        return CategoryDto.from(response.getCategory());
    }
}

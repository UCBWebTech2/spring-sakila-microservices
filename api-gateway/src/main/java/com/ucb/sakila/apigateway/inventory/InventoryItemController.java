package com.ucb.sakila.apigateway.inventory;

import com.ucb.sakila.apigateway.inventory.ws.generated.CreateInventoryRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.CreateInventoryResponse;
import com.ucb.sakila.apigateway.inventory.ws.generated.DeleteInventoryRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.GetInventoryRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.GetInventoryResponse;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListInventoriesRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListInventoriesResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory/items")
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventorySoapClient client;

    @GetMapping
    public PageResponse<InventoryItemDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var request = new ListInventoriesRequest();
        request.setPage(page);
        request.setLimit(limit);
        ListInventoriesResponse response = client.call(request);
        List<InventoryItemDto> data = response.getData().stream().map(InventoryItemDto::from).toList();
        return new PageResponse<>(data, PageResponse.toMeta(response.getMeta()));
    }

    @GetMapping("/{id}")
    public InventoryItemDto get(@PathVariable int id) {
        var request = new GetInventoryRequest();
        request.setId(id);
        GetInventoryResponse response = client.call(request);
        return InventoryItemDto.from(response.getInventoryItem());
    }

    public record CreateInventoryBody(int filmId, int storeId) {
    }

    @PostMapping
    public InventoryItemDto create(@RequestBody CreateInventoryBody body) {
        var request = new CreateInventoryRequest();
        request.setFilmId(body.filmId());
        request.setStoreId(body.storeId());
        CreateInventoryResponse response = client.call(request);
        return InventoryItemDto.from(response.getInventoryItem());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        var request = new DeleteInventoryRequest();
        request.setId(id);
        client.call(request);
    }
}

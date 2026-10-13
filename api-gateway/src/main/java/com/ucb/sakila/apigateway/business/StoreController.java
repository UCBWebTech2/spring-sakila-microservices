package com.ucb.sakila.apigateway.business;

import com.ucb.sakila.apigateway.shared.BusinessServiceGraphQlClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/stores")
@RequiredArgsConstructor
public class StoreController {

    private static final String FIELDS = "id manager { id } addressId lastUpdate";

    private final BusinessServiceGraphQlClient client;

    public record CreateStoreBody(Integer managerStaffId, Integer addressId) {
    }

    public record UpdateStoreBody(Integer managerStaffId, Integer addressId) {
    }

    @SuppressWarnings("unchecked")
    @GetMapping
    public PageResponse<StoreDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var data = client.query("query($page:Int,$limit:Int){ stores(page:$page,limit:$limit){ data { " + FIELDS + " } meta { page limit total pages } } }",
                Map.of("page", page, "limit", limit));
        Map<String, Object> stores = (Map<String, Object>) data.get("stores");
        List<StoreDto> items = ((List<Map<String, Object>>) stores.get("data")).stream().map(StoreDto::from).toList();
        return new PageResponse<>(items, PageMeta.from(stores.get("meta")));
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/{id}")
    public StoreDto get(@PathVariable int id) {
        var data = client.query("query($id:ID!){ store(id:$id){ " + FIELDS + " } }", Map.of("id", id));
        return StoreDto.from((Map<String, Object>) data.get("store"));
    }

    @SuppressWarnings("unchecked")
    @PostMapping
    public StoreDto create(@RequestBody CreateStoreBody body) {
        Map<String, Object> input = new HashMap<>();
        input.put("managerStaffId", body.managerStaffId());
        input.put("addressId", body.addressId());
        var data = client.query("mutation($input:CreateStoreInput!){ createStore(input:$input){ " + FIELDS + " } }", Map.of("input", input));
        return StoreDto.from((Map<String, Object>) data.get("createStore"));
    }

    @SuppressWarnings("unchecked")
    @PatchMapping("/{id}")
    public StoreDto update(@PathVariable int id, @RequestBody UpdateStoreBody body) {
        Map<String, Object> input = new HashMap<>();
        input.put("managerStaffId", body.managerStaffId());
        input.put("addressId", body.addressId());
        var data = client.query("mutation($id:ID!,$input:UpdateStoreInput!){ updateStore(id:$id,input:$input){ " + FIELDS + " } }",
                Map.of("id", id, "input", input));
        return StoreDto.from((Map<String, Object>) data.get("updateStore"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int id) {
        client.query("mutation($id:ID!){ removeStore(id:$id) }", Map.of("id", id));
    }
}

package com.ucb.sakila.apigateway.reporting;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reporting")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingStompClient client;

    @GetMapping("/actors/info")
    public Map<String, Object> getActorInfo(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return client.call("getActorInfo", Map.of("page", page, "limit", limit), Map.class);
    }

    @GetMapping("/customers")
    public Map<String, Object> getCustomerList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return client.call("getCustomerList", Map.of("page", page, "limit", limit), Map.class);
    }

    @GetMapping("/films")
    public Map<String, Object> getFilmList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return client.call("getFilmList", Map.of("page", page, "limit", limit), Map.class);
    }

    @GetMapping("/films/nicer-but-slower")
    public Map<String, Object> getNicerButSlowerFilmList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return client.call("getNicerButSlowerFilmList", Map.of("page", page, "limit", limit), Map.class);
    }

    @GetMapping("/sales/by-category")
    public Map<String, Object> getSalesByFilmCategory(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return client.call("getSalesByFilmCategory", Map.of("page", page, "limit", limit), Map.class);
    }

    @GetMapping("/sales/by-store")
    public Map<String, Object> getSalesByStore(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return client.call("getSalesByStore", Map.of("page", page, "limit", limit), Map.class);
    }

    @GetMapping("/staff")
    public Map<String, Object> getStaffList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return client.call("getStaffList", Map.of("page", page, "limit", limit), Map.class);
    }

    @GetMapping("/films/{filmId}/in-stock")
    public List<Integer> getFilmInStock(@PathVariable int filmId, @RequestParam int storeId) {
        return client.call("getFilmInStock", Map.of("filmId", filmId, "storeId", storeId), List.class);
    }

    @GetMapping("/films/{filmId}/not-in-stock")
    public List<Integer> getFilmNotInStock(@PathVariable int filmId, @RequestParam int storeId) {
        return client.call("getFilmNotInStock", Map.of("filmId", filmId, "storeId", storeId), List.class);
    }

    @GetMapping("/customers/{customerId}/balance")
    public Map<String, Object> getCustomerBalance(@PathVariable int customerId, @RequestParam String effectiveDate) {
        return client.call("getCustomerBalance", Map.of("customerId", customerId, "effectiveDate", effectiveDate), Map.class);
    }

    @GetMapping("/inventory/{inventoryId}/held-by")
    public Map<String, Object> getInventoryHeldByCustomer(@PathVariable int inventoryId) {
        return client.call("getInventoryHeldByCustomer", Map.of("inventoryId", inventoryId), Map.class);
    }

    @GetMapping("/inventory/{inventoryId}/in-stock")
    public Map<String, Object> getInventoryInStock(@PathVariable int inventoryId) {
        return client.call("getInventoryInStock", Map.of("inventoryId", inventoryId), Map.class);
    }

    @GetMapping("/customers/rewards")
    public List<Map> getRewardsReport(@RequestParam int minMonthlyPurchases, @RequestParam String minDollarAmountPurchased) {
        return client.call("getRewardsReport",
                Map.of("minMonthlyPurchases", minMonthlyPurchases, "minDollarAmountPurchased", minDollarAmountPurchased),
                List.class);
    }
}

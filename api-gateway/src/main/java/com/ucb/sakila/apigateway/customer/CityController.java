package com.ucb.sakila.apigateway.customer;

import java.util.HashMap;
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
@RequestMapping("/customer/cities")
@RequiredArgsConstructor
public class CityController {

    private final CustomerAmqpClient client;

    public record CreateCityBody(String city, Integer countryId) {
    }

    public record UpdateCityBody(String city, Integer countryId) {
    }

    @GetMapping
    public PageResponse<CityDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return client.callPage("cities.find-all", Map.of("page", page, "limit", limit), CityDto.class);
    }

    @GetMapping("/{id}")
    public CityDto get(@PathVariable int id) {
        return client.call("cities.find-one", Map.of("id", id), CityDto.class);
    }

    @PostMapping
    public CityDto create(@RequestBody CreateCityBody body) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("city", body.city());
        payload.put("countryId", body.countryId());
        return client.call("cities.create", payload, CityDto.class);
    }

    @PatchMapping("/{id}")
    public CityDto update(@PathVariable int id, @RequestBody UpdateCityBody body) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("city", body.city());
        payload.put("countryId", body.countryId());
        return client.call("cities.update", payload, CityDto.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int id) {
        client.call("cities.remove", Map.of("id", id), Map.class);
    }
}

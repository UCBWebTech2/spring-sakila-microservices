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
@RequestMapping("/customer/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CustomerAmqpClient client;

    public record CreateCountryBody(String country) {
    }

    public record UpdateCountryBody(String country) {
    }

    @GetMapping
    public PageResponse<CountryDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return client.callPage("countries.find-all", Map.of("page", page, "limit", limit), CountryDto.class);
    }

    @GetMapping("/{id}")
    public CountryDto get(@PathVariable int id) {
        return client.call("countries.find-one", Map.of("id", id), CountryDto.class);
    }

    @PostMapping
    public CountryDto create(@RequestBody CreateCountryBody body) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("country", body.country());
        return client.call("countries.create", payload, CountryDto.class);
    }

    @PatchMapping("/{id}")
    public CountryDto update(@PathVariable int id, @RequestBody UpdateCountryBody body) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("country", body.country());
        return client.call("countries.update", payload, CountryDto.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int id) {
        client.call("countries.remove", Map.of("id", id), Map.class);
    }
}

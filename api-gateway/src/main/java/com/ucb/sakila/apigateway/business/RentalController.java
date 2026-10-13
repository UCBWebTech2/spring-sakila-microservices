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
@RequestMapping("/business/rentals")
@RequiredArgsConstructor
public class RentalController {

    private static final String FIELDS = "id rentalDate inventoryId customerId returnDate staff { id } lastUpdate";

    private final BusinessServiceGraphQlClient client;

    public record CreateRentalBody(String rentalDate, Integer inventoryId, Integer customerId, Integer staffId) {
    }

    public record UpdateRentalBody(String returnDate, Integer staffId) {
    }

    @SuppressWarnings("unchecked")
    @GetMapping
    public PageResponse<RentalDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var data = client.query("query($page:Int,$limit:Int){ rentals(page:$page,limit:$limit){ data { " + FIELDS + " } meta { page limit total pages } } }",
                Map.of("page", page, "limit", limit));
        Map<String, Object> rentals = (Map<String, Object>) data.get("rentals");
        List<RentalDto> items = ((List<Map<String, Object>>) rentals.get("data")).stream().map(RentalDto::from).toList();
        return new PageResponse<>(items, PageMeta.from(rentals.get("meta")));
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/{id}")
    public RentalDto get(@PathVariable int id) {
        var data = client.query("query($id:ID!){ rental(id:$id){ " + FIELDS + " } }", Map.of("id", id));
        return RentalDto.from((Map<String, Object>) data.get("rental"));
    }

    @SuppressWarnings("unchecked")
    @PostMapping
    public RentalDto create(@RequestBody CreateRentalBody body) {
        Map<String, Object> input = new HashMap<>();
        input.put("rentalDate", body.rentalDate());
        input.put("inventoryId", body.inventoryId());
        input.put("customerId", body.customerId());
        input.put("staffId", body.staffId());
        var data = client.query("mutation($input:CreateRentalInput!){ createRental(input:$input){ " + FIELDS + " } }", Map.of("input", input));
        return RentalDto.from((Map<String, Object>) data.get("createRental"));
    }

    @SuppressWarnings("unchecked")
    @PatchMapping("/{id}")
    public RentalDto update(@PathVariable int id, @RequestBody UpdateRentalBody body) {
        Map<String, Object> input = new HashMap<>();
        input.put("returnDate", body.returnDate());
        input.put("staffId", body.staffId());
        var data = client.query("mutation($id:ID!,$input:UpdateRentalInput!){ updateRental(id:$id,input:$input){ " + FIELDS + " } }",
                Map.of("id", id, "input", input));
        return RentalDto.from((Map<String, Object>) data.get("updateRental"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int id) {
        client.query("mutation($id:ID!){ removeRental(id:$id) }", Map.of("id", id));
    }
}

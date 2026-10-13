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
@RequestMapping("/business/staffs")
@RequiredArgsConstructor
public class StaffController {

    private static final String FIELDS = "id firstName lastName addressId email store { id } active username lastUpdate";

    private final BusinessServiceGraphQlClient client;

    public record CreateStaffBody(String firstName, String lastName, Integer addressId, Integer storeId,
            String email, String username, String password) {
    }

    public record UpdateStaffBody(String firstName, String lastName, Integer addressId, String email,
            Integer storeId, Boolean active, String username) {
    }

    @SuppressWarnings("unchecked")
    @GetMapping
    public PageResponse<StaffDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var data = client.query("query($page:Int,$limit:Int){ staffs(page:$page,limit:$limit){ data { " + FIELDS + " } meta { page limit total pages } } }",
                Map.of("page", page, "limit", limit));
        Map<String, Object> staffs = (Map<String, Object>) data.get("staffs");
        List<StaffDto> items = ((List<Map<String, Object>>) staffs.get("data")).stream().map(StaffDto::from).toList();
        return new PageResponse<>(items, PageMeta.from(staffs.get("meta")));
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/{id}")
    public StaffDto get(@PathVariable int id) {
        var data = client.query("query($id:ID!){ staff(id:$id){ " + FIELDS + " } }", Map.of("id", id));
        return StaffDto.from((Map<String, Object>) data.get("staff"));
    }

    @SuppressWarnings("unchecked")
    @PostMapping
    public StaffDto create(@RequestBody CreateStaffBody body) {
        Map<String, Object> input = new HashMap<>();
        input.put("firstName", body.firstName());
        input.put("lastName", body.lastName());
        input.put("addressId", body.addressId());
        input.put("storeId", body.storeId());
        input.put("email", body.email());
        input.put("username", body.username());
        input.put("password", body.password());
        var data = client.query("mutation($input:CreateStaffInput!){ createStaff(input:$input){ " + FIELDS + " } }", Map.of("input", input));
        return StaffDto.from((Map<String, Object>) data.get("createStaff"));
    }

    @SuppressWarnings("unchecked")
    @PatchMapping("/{id}")
    public StaffDto update(@PathVariable int id, @RequestBody UpdateStaffBody body) {
        Map<String, Object> input = new HashMap<>();
        input.put("firstName", body.firstName());
        input.put("lastName", body.lastName());
        input.put("addressId", body.addressId());
        input.put("email", body.email());
        input.put("storeId", body.storeId());
        input.put("active", body.active());
        input.put("username", body.username());
        var data = client.query("mutation($id:ID!,$input:UpdateStaffInput!){ updateStaff(id:$id,input:$input){ " + FIELDS + " } }",
                Map.of("id", id, "input", input));
        return StaffDto.from((Map<String, Object>) data.get("updateStaff"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int id) {
        client.query("mutation($id:ID!){ removeStaff(id:$id) }", Map.of("id", id));
    }
}

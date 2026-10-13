package com.ucb.sakila.apigateway.inventory;

import com.ucb.sakila.apigateway.inventory.ws.generated.GetLanguageRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.GetLanguageResponse;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListLanguagesRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListLanguagesResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final InventorySoapClient client;

    @GetMapping
    public PageResponse<LanguageDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var request = new ListLanguagesRequest();
        request.setPage(page);
        request.setLimit(limit);
        ListLanguagesResponse response = client.call(request);
        List<LanguageDto> data = response.getData().stream().map(LanguageDto::from).toList();
        return new PageResponse<>(data, PageResponse.toMeta(response.getMeta()));
    }

    @GetMapping("/{id}")
    public LanguageDto get(@PathVariable int id) {
        var request = new GetLanguageRequest();
        request.setId(id);
        GetLanguageResponse response = client.call(request);
        return LanguageDto.from(response.getLanguage());
    }
}

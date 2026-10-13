package com.ucb.sakila.apigateway.inventory;

import com.ucb.sakila.apigateway.inventory.ws.generated.CreateFilmRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.CreateFilmResponse;
import com.ucb.sakila.apigateway.inventory.ws.generated.DeleteFilmRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.GetFilmRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.GetFilmResponse;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListFilmsRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListFilmsResponse;
import java.math.BigDecimal;
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
@RequestMapping("/inventory/films")
@RequiredArgsConstructor
public class FilmController {

    private final InventorySoapClient client;

    @GetMapping
    public PageResponse<FilmDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var request = new ListFilmsRequest();
        request.setPage(page);
        request.setLimit(limit);
        ListFilmsResponse response = client.call(request);
        List<FilmDto> data = response.getData().stream().map(FilmDto::from).toList();
        return new PageResponse<>(data, PageResponse.toMeta(response.getMeta()));
    }

    @GetMapping("/{id}")
    public FilmDto get(@PathVariable int id) {
        var request = new GetFilmRequest();
        request.setId(id);
        GetFilmResponse response = client.call(request);
        return FilmDto.from(response.getFilm());
    }

    public record CreateFilmBody(String title, String description, int languageId, Integer originalLanguageId,
            int rentalDuration, BigDecimal rentalRate, Integer length, BigDecimal replacementCost,
            List<Integer> actorIds, List<Integer> categoryIds) {
    }

    @PostMapping
    public FilmDto create(@RequestBody CreateFilmBody body) {
        var request = new CreateFilmRequest();
        request.setTitle(body.title());
        request.setDescription(body.description());
        request.setLanguageId(body.languageId());
        request.setOriginalLanguageId(body.originalLanguageId());
        request.setRentalDuration(body.rentalDuration());
        request.setRentalRate(body.rentalRate());
        request.setLength(body.length());
        request.setReplacementCost(body.replacementCost());
        if (body.actorIds() != null) request.getActorId().addAll(body.actorIds());
        if (body.categoryIds() != null) request.getCategoryId().addAll(body.categoryIds());
        CreateFilmResponse response = client.call(request);
        return FilmDto.from(response.getFilm());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        var request = new DeleteFilmRequest();
        request.setId(id);
        client.call(request);
    }
}

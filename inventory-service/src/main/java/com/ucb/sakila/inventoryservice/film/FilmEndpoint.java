package com.ucb.sakila.inventoryservice.film;

import com.ucb.sakila.inventoryservice.shared.PageResult;
import com.ucb.sakila.inventoryservice.ws.XmlDates;
import com.ucb.sakila.inventoryservice.ws.generated.CreateFilmRequest;
import com.ucb.sakila.inventoryservice.ws.generated.CreateFilmResponse;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteFilmRequest;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteFilmResponse;
import com.ucb.sakila.inventoryservice.ws.generated.GetFilmRequest;
import com.ucb.sakila.inventoryservice.ws.generated.GetFilmResponse;
import com.ucb.sakila.inventoryservice.ws.generated.ListFilmsRequest;
import com.ucb.sakila.inventoryservice.ws.generated.ListFilmsResponse;
import com.ucb.sakila.inventoryservice.ws.generated.PageInfo;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateFilmRequest;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateFilmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@RequiredArgsConstructor
public class FilmEndpoint {

    private static final String NAMESPACE = "http://inventory.sakila.ucb.com/schema";

    private final FilmService service;

    @PayloadRoot(namespace = NAMESPACE, localPart = "ListFilmsRequest")
    @ResponsePayload
    public ListFilmsResponse list(@RequestPayload ListFilmsRequest request) {
        PageResult<Film> result = service.findAll(request.getPage(), request.getLimit());
        ListFilmsResponse response = new ListFilmsResponse();
        for (Film film : result.data()) response.getData().add(toXml(film));
        response.setMeta(toPageInfo(result));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetFilmRequest")
    @ResponsePayload
    public GetFilmResponse get(@RequestPayload GetFilmRequest request) {
        GetFilmResponse response = new GetFilmResponse();
        response.setFilm(toXml(service.findOneById(request.getId())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "CreateFilmRequest")
    @ResponsePayload
    public CreateFilmResponse create(@RequestPayload CreateFilmRequest request) {
        Film film = service.create(request.getTitle(), request.getDescription(),
                request.getLanguageId(), request.getOriginalLanguageId(), request.getRentalDuration(),
                request.getRentalRate(), request.getLength(), request.getReplacementCost(),
                request.getActorId(), request.getCategoryId());
        CreateFilmResponse response = new CreateFilmResponse();
        response.setFilm(toXml(film));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "UpdateFilmRequest")
    @ResponsePayload
    public UpdateFilmResponse update(@RequestPayload UpdateFilmRequest request) {
        Film film = service.update(request.getId(), request.getTitle(), request.getDescription(),
                request.getRentalRate(), request.getReplacementCost());
        UpdateFilmResponse response = new UpdateFilmResponse();
        response.setFilm(toXml(film));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "DeleteFilmRequest")
    @ResponsePayload
    public DeleteFilmResponse delete(@RequestPayload DeleteFilmRequest request) {
        service.remove(request.getId());
        DeleteFilmResponse response = new DeleteFilmResponse();
        response.setSuccess(true);
        return response;
    }

    private com.ucb.sakila.inventoryservice.ws.generated.Film toXml(Film entity) {
        var xml = new com.ucb.sakila.inventoryservice.ws.generated.Film();
        xml.setId(entity.getId());
        xml.setTitle(entity.getTitle());
        xml.setDescription(entity.getDescription());
        xml.setLanguageId(entity.getLanguage().getId());
        if (entity.getOriginalLanguage() != null) xml.setOriginalLanguageId(entity.getOriginalLanguage().getId());
        xml.setRentalDuration(entity.getRentalDuration());
        xml.setRentalRate(entity.getRentalRate());
        if (entity.getLength() != null) xml.setLength((int) entity.getLength());
        xml.setReplacementCost(entity.getReplacementCost());
        xml.setLastUpdate(XmlDates.from(entity.getLastUpdate()));
        entity.getActors().forEach(actor -> xml.getActorId().add(actor.getId()));
        entity.getCategories().forEach(category -> xml.getCategoryId().add(category.getId()));
        return xml;
    }

    private PageInfo toPageInfo(PageResult<?> result) {
        PageInfo meta = new PageInfo();
        meta.setPage(result.page());
        meta.setLimit(result.limit());
        meta.setTotal(result.total());
        meta.setPages(result.pages());
        return meta;
    }
}

package com.ucb.sakila.inventoryservice.language;

import com.ucb.sakila.inventoryservice.shared.PageResult;
import com.ucb.sakila.inventoryservice.ws.XmlDates;
import com.ucb.sakila.inventoryservice.ws.generated.CreateLanguageRequest;
import com.ucb.sakila.inventoryservice.ws.generated.CreateLanguageResponse;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteLanguageRequest;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteLanguageResponse;
import com.ucb.sakila.inventoryservice.ws.generated.GetLanguageRequest;
import com.ucb.sakila.inventoryservice.ws.generated.GetLanguageResponse;
import com.ucb.sakila.inventoryservice.ws.generated.ListLanguagesRequest;
import com.ucb.sakila.inventoryservice.ws.generated.ListLanguagesResponse;
import com.ucb.sakila.inventoryservice.ws.generated.PageInfo;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateLanguageRequest;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateLanguageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@RequiredArgsConstructor
public class LanguageEndpoint {

    private static final String NAMESPACE = "http://inventory.sakila.ucb.com/schema";

    private final LanguageService service;

    @PayloadRoot(namespace = NAMESPACE, localPart = "ListLanguagesRequest")
    @ResponsePayload
    public ListLanguagesResponse list(@RequestPayload ListLanguagesRequest request) {
        PageResult<Language> result = service.findAll(request.getPage(), request.getLimit());
        ListLanguagesResponse response = new ListLanguagesResponse();
        for (Language language : result.data()) response.getData().add(toXml(language));
        response.setMeta(toPageInfo(result));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetLanguageRequest")
    @ResponsePayload
    public GetLanguageResponse get(@RequestPayload GetLanguageRequest request) {
        GetLanguageResponse response = new GetLanguageResponse();
        response.setLanguage(toXml(service.findOneById(request.getId())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "CreateLanguageRequest")
    @ResponsePayload
    public CreateLanguageResponse create(@RequestPayload CreateLanguageRequest request) {
        CreateLanguageResponse response = new CreateLanguageResponse();
        response.setLanguage(toXml(service.create(request.getName())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "UpdateLanguageRequest")
    @ResponsePayload
    public UpdateLanguageResponse update(@RequestPayload UpdateLanguageRequest request) {
        UpdateLanguageResponse response = new UpdateLanguageResponse();
        response.setLanguage(toXml(service.update(request.getId(), request.getName())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "DeleteLanguageRequest")
    @ResponsePayload
    public DeleteLanguageResponse delete(@RequestPayload DeleteLanguageRequest request) {
        service.remove(request.getId());
        DeleteLanguageResponse response = new DeleteLanguageResponse();
        response.setSuccess(true);
        return response;
    }

    private com.ucb.sakila.inventoryservice.ws.generated.Language toXml(Language entity) {
        var xml = new com.ucb.sakila.inventoryservice.ws.generated.Language();
        xml.setId(entity.getId());
        xml.setName(entity.getName().trim());
        xml.setLastUpdate(XmlDates.from(entity.getLastUpdate()));
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

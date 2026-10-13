package com.ucb.sakila.inventoryservice.category;

import com.ucb.sakila.inventoryservice.shared.PageResult;
import com.ucb.sakila.inventoryservice.ws.XmlDates;
import com.ucb.sakila.inventoryservice.ws.generated.CreateCategoryRequest;
import com.ucb.sakila.inventoryservice.ws.generated.CreateCategoryResponse;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteCategoryRequest;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteCategoryResponse;
import com.ucb.sakila.inventoryservice.ws.generated.GetCategoryRequest;
import com.ucb.sakila.inventoryservice.ws.generated.GetCategoryResponse;
import com.ucb.sakila.inventoryservice.ws.generated.ListCategoriesRequest;
import com.ucb.sakila.inventoryservice.ws.generated.ListCategoriesResponse;
import com.ucb.sakila.inventoryservice.ws.generated.PageInfo;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateCategoryRequest;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@RequiredArgsConstructor
public class CategoryEndpoint {

    private static final String NAMESPACE = "http://inventory.sakila.ucb.com/schema";

    private final CategoryService service;

    @PayloadRoot(namespace = NAMESPACE, localPart = "ListCategoriesRequest")
    @ResponsePayload
    public ListCategoriesResponse list(@RequestPayload ListCategoriesRequest request) {
        PageResult<Category> result = service.findAll(request.getPage(), request.getLimit());
        ListCategoriesResponse response = new ListCategoriesResponse();
        for (Category category : result.data()) response.getData().add(toXml(category));
        response.setMeta(toPageInfo(result));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetCategoryRequest")
    @ResponsePayload
    public GetCategoryResponse get(@RequestPayload GetCategoryRequest request) {
        GetCategoryResponse response = new GetCategoryResponse();
        response.setCategory(toXml(service.findOneById(request.getId())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "CreateCategoryRequest")
    @ResponsePayload
    public CreateCategoryResponse create(@RequestPayload CreateCategoryRequest request) {
        CreateCategoryResponse response = new CreateCategoryResponse();
        response.setCategory(toXml(service.create(request.getName())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "UpdateCategoryRequest")
    @ResponsePayload
    public UpdateCategoryResponse update(@RequestPayload UpdateCategoryRequest request) {
        UpdateCategoryResponse response = new UpdateCategoryResponse();
        response.setCategory(toXml(service.update(request.getId(), request.getName())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "DeleteCategoryRequest")
    @ResponsePayload
    public DeleteCategoryResponse delete(@RequestPayload DeleteCategoryRequest request) {
        service.remove(request.getId());
        DeleteCategoryResponse response = new DeleteCategoryResponse();
        response.setSuccess(true);
        return response;
    }

    private com.ucb.sakila.inventoryservice.ws.generated.Category toXml(Category entity) {
        var xml = new com.ucb.sakila.inventoryservice.ws.generated.Category();
        xml.setId(entity.getId());
        xml.setName(entity.getName());
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

package com.ucb.sakila.inventoryservice.inventory;

import com.ucb.sakila.inventoryservice.shared.PageResult;
import com.ucb.sakila.inventoryservice.ws.XmlDates;
import com.ucb.sakila.inventoryservice.ws.generated.CreateInventoryRequest;
import com.ucb.sakila.inventoryservice.ws.generated.CreateInventoryResponse;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteInventoryRequest;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteInventoryResponse;
import com.ucb.sakila.inventoryservice.ws.generated.GetInventoryRequest;
import com.ucb.sakila.inventoryservice.ws.generated.GetInventoryResponse;
import com.ucb.sakila.inventoryservice.ws.generated.ListInventoriesRequest;
import com.ucb.sakila.inventoryservice.ws.generated.ListInventoriesResponse;
import com.ucb.sakila.inventoryservice.ws.generated.PageInfo;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateInventoryRequest;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateInventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@RequiredArgsConstructor
public class InventoryEndpoint {

    private static final String NAMESPACE = "http://inventory.sakila.ucb.com/schema";

    private final InventoryService service;

    @PayloadRoot(namespace = NAMESPACE, localPart = "ListInventoriesRequest")
    @ResponsePayload
    public ListInventoriesResponse list(@RequestPayload ListInventoriesRequest request) {
        PageResult<InventoryItem> result = service.findAll(request.getPage(), request.getLimit());
        ListInventoriesResponse response = new ListInventoriesResponse();
        for (InventoryItem item : result.data()) response.getData().add(toXml(item));
        response.setMeta(toPageInfo(result));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetInventoryRequest")
    @ResponsePayload
    public GetInventoryResponse get(@RequestPayload GetInventoryRequest request) {
        GetInventoryResponse response = new GetInventoryResponse();
        response.setInventoryItem(toXml(service.findOneById(request.getId())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "CreateInventoryRequest")
    @ResponsePayload
    public CreateInventoryResponse create(@RequestPayload CreateInventoryRequest request) {
        CreateInventoryResponse response = new CreateInventoryResponse();
        response.setInventoryItem(toXml(service.create(request.getFilmId(), request.getStoreId())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "UpdateInventoryRequest")
    @ResponsePayload
    public UpdateInventoryResponse update(@RequestPayload UpdateInventoryRequest request) {
        UpdateInventoryResponse response = new UpdateInventoryResponse();
        response.setInventoryItem(toXml(service.update(request.getId(), request.getFilmId(), request.getStoreId())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "DeleteInventoryRequest")
    @ResponsePayload
    public DeleteInventoryResponse delete(@RequestPayload DeleteInventoryRequest request) {
        service.remove(request.getId());
        DeleteInventoryResponse response = new DeleteInventoryResponse();
        response.setSuccess(true);
        return response;
    }

    private com.ucb.sakila.inventoryservice.ws.generated.InventoryItem toXml(InventoryItem entity) {
        var xml = new com.ucb.sakila.inventoryservice.ws.generated.InventoryItem();
        xml.setId(entity.getId());
        xml.setFilmId(entity.getFilm().getId());
        xml.setStoreId(entity.getStoreId());
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

package com.ucb.sakila.inventoryservice.actor;

import com.ucb.sakila.inventoryservice.shared.PageResult;
import com.ucb.sakila.inventoryservice.ws.XmlDates;
import com.ucb.sakila.inventoryservice.ws.generated.CreateActorRequest;
import com.ucb.sakila.inventoryservice.ws.generated.CreateActorResponse;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteActorRequest;
import com.ucb.sakila.inventoryservice.ws.generated.DeleteActorResponse;
import com.ucb.sakila.inventoryservice.ws.generated.GetActorRequest;
import com.ucb.sakila.inventoryservice.ws.generated.GetActorResponse;
import com.ucb.sakila.inventoryservice.ws.generated.ListActorsRequest;
import com.ucb.sakila.inventoryservice.ws.generated.ListActorsResponse;
import com.ucb.sakila.inventoryservice.ws.generated.PageInfo;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateActorRequest;
import com.ucb.sakila.inventoryservice.ws.generated.UpdateActorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@RequiredArgsConstructor
public class ActorEndpoint {

    private static final String NAMESPACE = "http://inventory.sakila.ucb.com/schema";

    private final ActorService service;

    @PayloadRoot(namespace = NAMESPACE, localPart = "ListActorsRequest")
    @ResponsePayload
    public ListActorsResponse list(@RequestPayload ListActorsRequest request) {
        PageResult<Actor> result = service.findAll(request.getPage(), request.getLimit());
        ListActorsResponse response = new ListActorsResponse();
        for (Actor actor : result.data()) response.getData().add(toXml(actor));
        response.setMeta(toPageInfo(result));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "GetActorRequest")
    @ResponsePayload
    public GetActorResponse get(@RequestPayload GetActorRequest request) {
        GetActorResponse response = new GetActorResponse();
        response.setActor(toXml(service.findOneById(request.getId())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "CreateActorRequest")
    @ResponsePayload
    public CreateActorResponse create(@RequestPayload CreateActorRequest request) {
        CreateActorResponse response = new CreateActorResponse();
        response.setActor(toXml(service.create(request.getFirstName(), request.getLastName())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "UpdateActorRequest")
    @ResponsePayload
    public UpdateActorResponse update(@RequestPayload UpdateActorRequest request) {
        UpdateActorResponse response = new UpdateActorResponse();
        response.setActor(toXml(service.update(request.getId(), request.getFirstName(), request.getLastName())));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "DeleteActorRequest")
    @ResponsePayload
    public DeleteActorResponse delete(@RequestPayload DeleteActorRequest request) {
        service.remove(request.getId());
        DeleteActorResponse response = new DeleteActorResponse();
        response.setSuccess(true);
        return response;
    }

    private com.ucb.sakila.inventoryservice.ws.generated.Actor toXml(Actor entity) {
        var xml = new com.ucb.sakila.inventoryservice.ws.generated.Actor();
        xml.setId(entity.getId());
        xml.setFirstName(entity.getFirstName());
        xml.setLastName(entity.getLastName());
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

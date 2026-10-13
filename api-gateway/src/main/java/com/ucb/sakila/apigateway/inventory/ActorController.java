package com.ucb.sakila.apigateway.inventory;

import com.ucb.sakila.apigateway.inventory.ws.generated.CreateActorRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.CreateActorResponse;
import com.ucb.sakila.apigateway.inventory.ws.generated.DeleteActorRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.GetActorRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.GetActorResponse;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListActorsRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.ListActorsResponse;
import com.ucb.sakila.apigateway.inventory.ws.generated.UpdateActorRequest;
import com.ucb.sakila.apigateway.inventory.ws.generated.UpdateActorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory/actors")
@RequiredArgsConstructor
public class ActorController {

    private final InventorySoapClient client;

    @GetMapping
    public PageResponse<ActorDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var request = new ListActorsRequest();
        request.setPage(page);
        request.setLimit(limit);
        ListActorsResponse response = client.call(request);
        List<ActorDto> data = response.getData().stream().map(ActorDto::from).toList();
        return new PageResponse<>(data, PageResponse.toMeta(response.getMeta()));
    }

    @GetMapping("/{id}")
    public ActorDto get(@PathVariable int id) {
        var request = new GetActorRequest();
        request.setId(id);
        GetActorResponse response = client.call(request);
        return ActorDto.from(response.getActor());
    }

    public record ActorBody(String firstName, String lastName) {
    }

    @PostMapping
    public ActorDto create(@RequestBody ActorBody body) {
        var request = new CreateActorRequest();
        request.setFirstName(body.firstName());
        request.setLastName(body.lastName());
        CreateActorResponse response = client.call(request);
        return ActorDto.from(response.getActor());
    }

    @PatchMapping("/{id}")
    public ActorDto update(@PathVariable int id, @RequestBody ActorBody body) {
        var request = new UpdateActorRequest();
        request.setId(id);
        request.setFirstName(body.firstName());
        request.setLastName(body.lastName());
        UpdateActorResponse response = client.call(request);
        return ActorDto.from(response.getActor());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        var request = new DeleteActorRequest();
        request.setId(id);
        client.call(request);
    }
}

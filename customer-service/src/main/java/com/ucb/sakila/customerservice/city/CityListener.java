package com.ucb.sakila.customerservice.city;

import com.ucb.sakila.customerservice.shared.AmqpErrors;
import com.ucb.sakila.customerservice.shared.IdRequest;
import com.ucb.sakila.customerservice.shared.PaginationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.validation.annotation.Validated;

@org.springframework.stereotype.Component
@RequiredArgsConstructor
@Validated
public class CityListener {

    private final CityService service;

    @RabbitListener(queues = "cities.find-all")
    public Object findAll(PaginationRequest request) {
        return AmqpErrors.handle(() -> service.findAll(request.page(), request.limit()));
    }

    @RabbitListener(queues = "cities.find-one")
    public Object findOne(IdRequest request) {
        return AmqpErrors.handle(() -> service.findOneById(request.id()));
    }

    @RabbitListener(queues = "cities.create")
    public Object create(@Valid CreateCityDto dto) {
        return AmqpErrors.handle(() -> service.create(dto));
    }

    @RabbitListener(queues = "cities.update")
    public Object update(@Valid UpdateCityDto dto) {
        return AmqpErrors.handle(() -> service.update(dto));
    }

    @RabbitListener(queues = "cities.remove")
    public Object remove(IdRequest request) {
        return AmqpErrors.handle(() -> {
            service.remove(request.id());
            return request;
        });
    }
}

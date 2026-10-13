package com.ucb.sakila.customerservice.country;

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
public class CountryListener {

    private final CountryService service;

    @RabbitListener(queues = "countries.find-all")
    public Object findAll(PaginationRequest request) {
        return AmqpErrors.handle(() -> service.findAll(request.page(), request.limit()));
    }

    @RabbitListener(queues = "countries.find-one")
    public Object findOne(IdRequest request) {
        return AmqpErrors.handle(() -> service.findOneById(request.id()));
    }

    @RabbitListener(queues = "countries.create")
    public Object create(@Valid CreateCountryDto dto) {
        return AmqpErrors.handle(() -> service.create(dto));
    }

    @RabbitListener(queues = "countries.update")
    public Object update(@Valid UpdateCountryDto dto) {
        return AmqpErrors.handle(() -> service.update(dto));
    }

    @RabbitListener(queues = "countries.remove")
    public Object remove(IdRequest request) {
        return AmqpErrors.handle(() -> {
            service.remove(request.id());
            return request;
        });
    }
}

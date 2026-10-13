package com.ucb.sakila.inventoryservice.actor;

import com.ucb.sakila.inventoryservice.shared.PageResult;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository repository;

    public PageResult<Actor> findAll(int page, int limit) {
        var result = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent(), page, limit, result.getTotalElements());
    }

    public Actor findOneById(Integer id) {
        return repository.findById(id).orElseThrow(ActorNotFoundException::new);
    }

    @Transactional
    public Actor create(String firstName, String lastName) {
        Actor actor = new Actor();
        actor.setFirstName(firstName);
        actor.setLastName(lastName);
        actor.setLastUpdate(LocalDateTime.now());
        return repository.save(actor);
    }

    @Transactional
    public Actor update(Integer id, String firstName, String lastName) {
        Actor actor = findOneById(id);
        if (firstName != null) actor.setFirstName(firstName);
        if (lastName != null) actor.setLastName(lastName);
        actor.setLastUpdate(LocalDateTime.now());
        return actor;
    }

    @Transactional
    public void remove(Integer id) {
        if (!repository.existsById(id)) throw new ActorNotFoundException();
        repository.deleteById(id);
    }
}

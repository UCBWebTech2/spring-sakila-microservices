package com.ucb.sakila.inventoryservice.inventory;

import com.ucb.sakila.inventoryservice.film.Film;
import com.ucb.sakila.inventoryservice.film.FilmNotFoundException;
import com.ucb.sakila.inventoryservice.film.FilmRepository;
import com.ucb.sakila.inventoryservice.shared.PageResult;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository repository;
    private final FilmRepository filmRepository;

    public PageResult<InventoryItem> findAll(int page, int limit) {
        var result = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent(), page, limit, result.getTotalElements());
    }

    public InventoryItem findOneById(Integer id) {
        return repository.findById(id).orElseThrow(InventoryNotFoundException::new);
    }

    @Transactional
    public InventoryItem create(Integer filmId, Integer storeId) {
        Film film = filmRepository.findById(filmId).orElseThrow(FilmNotFoundException::new);
        InventoryItem item = new InventoryItem();
        item.setFilm(film);
        item.setStoreId(storeId);
        item.setLastUpdate(LocalDateTime.now());
        return repository.save(item);
    }

    @Transactional
    public InventoryItem update(Integer id, Integer filmId, Integer storeId) {
        InventoryItem item = findOneById(id);
        if (filmId != null) item.setFilm(filmRepository.findById(filmId).orElseThrow(FilmNotFoundException::new));
        if (storeId != null) item.setStoreId(storeId);
        item.setLastUpdate(LocalDateTime.now());
        return item;
    }

    @Transactional
    public void remove(Integer id) {
        if (!repository.existsById(id)) throw new InventoryNotFoundException();
        repository.deleteById(id);
    }
}

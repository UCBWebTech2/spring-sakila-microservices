package com.ucb.sakila.inventoryservice.film;

import com.ucb.sakila.inventoryservice.actor.Actor;
import com.ucb.sakila.inventoryservice.actor.ActorNotFoundException;
import com.ucb.sakila.inventoryservice.actor.ActorRepository;
import com.ucb.sakila.inventoryservice.category.Category;
import com.ucb.sakila.inventoryservice.category.CategoryNotFoundException;
import com.ucb.sakila.inventoryservice.category.CategoryRepository;
import com.ucb.sakila.inventoryservice.language.LanguageNotFoundException;
import com.ucb.sakila.inventoryservice.language.LanguageRepository;
import com.ucb.sakila.inventoryservice.shared.PageResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final LanguageRepository languageRepository;
    private final ActorRepository actorRepository;
    private final CategoryRepository categoryRepository;

    public PageResult<Film> findAll(int page, int limit) {
        var result = filmRepository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent(), page, limit, result.getTotalElements());
    }

    public Film findOneById(Integer id) {
        return filmRepository.findById(id).orElseThrow(FilmNotFoundException::new);
    }

    @Transactional
    public Film create(String title, String description, Integer languageId,
            Integer originalLanguageId, int rentalDuration, BigDecimal rentalRate, Integer length,
            BigDecimal replacementCost, List<Integer> actorIds, List<Integer> categoryIds) {
        Film film = new Film();
        film.setTitle(title);
        film.setDescription(description);
        film.setLanguage(languageRepository.findById(languageId).orElseThrow(LanguageNotFoundException::new));
        if (originalLanguageId != null) {
            film.setOriginalLanguage(languageRepository.findById(originalLanguageId)
                    .orElseThrow(LanguageNotFoundException::new));
        }
        film.setRentalDuration((short) rentalDuration);
        film.setRentalRate(rentalRate);
        film.setLength(length == null ? null : length.shortValue());
        film.setReplacementCost(replacementCost);
        film.setActors(resolveActors(actorIds));
        film.setCategories(resolveCategories(categoryIds));
        film.setLastUpdate(LocalDateTime.now());
        return filmRepository.save(film);
    }

    @Transactional
    public Film update(Integer id, String title, String description, BigDecimal rentalRate, BigDecimal replacementCost) {
        Film film = findOneById(id);
        if (title != null) film.setTitle(title);
        if (description != null) film.setDescription(description);
        if (rentalRate != null) film.setRentalRate(rentalRate);
        if (replacementCost != null) film.setReplacementCost(replacementCost);
        film.setLastUpdate(LocalDateTime.now());
        return film;
    }

    @Transactional
    public void remove(Integer id) {
        if (!filmRepository.existsById(id)) throw new FilmNotFoundException();
        filmRepository.deleteById(id);
    }

    private Set<Actor> resolveActors(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        Set<Actor> actors = new HashSet<>();
        for (Integer actorId : ids) {
            actors.add(actorRepository.findById(actorId).orElseThrow(ActorNotFoundException::new));
        }
        return actors;
    }

    private Set<Category> resolveCategories(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        Set<Category> categories = new HashSet<>();
        for (Integer categoryId : ids) {
            categories.add(categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new));
        }
        return categories;
    }
}

package com.ucb.sakila.inventoryservice.language;

import com.ucb.sakila.inventoryservice.shared.PageResult;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository repository;

    public PageResult<Language> findAll(int page, int limit) {
        var result = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent(), page, limit, result.getTotalElements());
    }

    public Language findOneById(Integer id) {
        return repository.findById(id).orElseThrow(LanguageNotFoundException::new);
    }

    @Transactional
    public Language create(String name) {
        Language language = new Language();
        language.setName(name);
        language.setLastUpdate(LocalDateTime.now());
        return repository.save(language);
    }

    @Transactional
    public Language update(Integer id, String name) {
        Language language = findOneById(id);
        if (name != null) language.setName(name);
        language.setLastUpdate(LocalDateTime.now());
        return language;
    }

    @Transactional
    public void remove(Integer id) {
        if (!repository.existsById(id)) throw new LanguageNotFoundException();
        repository.deleteById(id);
    }
}

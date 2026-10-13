package com.ucb.sakila.inventoryservice.category;

import com.ucb.sakila.inventoryservice.shared.PageResult;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public PageResult<Category> findAll(int page, int limit) {
        var result = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent(), page, limit, result.getTotalElements());
    }

    public Category findOneById(Integer id) {
        return repository.findById(id).orElseThrow(CategoryNotFoundException::new);
    }

    @Transactional
    public Category create(String name) {
        Category category = new Category();
        category.setName(name);
        category.setLastUpdate(LocalDateTime.now());
        return repository.save(category);
    }

    @Transactional
    public Category update(Integer id, String name) {
        Category category = findOneById(id);
        if (name != null) category.setName(name);
        category.setLastUpdate(LocalDateTime.now());
        return category;
    }

    @Transactional
    public void remove(Integer id) {
        if (!repository.existsById(id)) throw new CategoryNotFoundException();
        repository.deleteById(id);
    }
}

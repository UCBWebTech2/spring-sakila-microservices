package com.ucb.sakila.customerservice.country;

import com.ucb.sakila.customerservice.shared.PageInfo;
import com.ucb.sakila.customerservice.shared.PageResult;
import com.ucb.sakila.customerservice.shared.PgErrors;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository repository;

    public PageResult<CountryDto> findAll(int page, int limit) {
        var result = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent().stream().map(CountryDto::fromEntity).toList(),
                PageInfo.of(page, limit, result.getTotalElements()));
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public CountryDto findOneById(Integer id) {
        return CountryDto.fromEntity(findEntity(id));
    }

    public CountryDto create(CreateCountryDto dto) {
        Country entity = new Country();
        entity.setCountry(dto.country());
        entity.setLastUpdate(LocalDateTime.now());
        return CountryDto.fromEntity(save(entity));
    }

    public CountryDto update(UpdateCountryDto dto) {
        Country entity = findEntity(dto.id());
        if (dto.country() != null) entity.setCountry(dto.country());
        entity.setLastUpdate(LocalDateTime.now());
        return CountryDto.fromEntity(save(entity));
    }

    public void remove(Integer id) {
        if (!repository.existsById(id)) throw new CountryNotFoundException();
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw PgErrors.translate(ex);
        }
    }

    private Country findEntity(Integer id) {
        return repository.findById(id).orElseThrow(CountryNotFoundException::new);
    }

    private Country save(Country entity) {
        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw PgErrors.translate(ex);
        }
    }
}

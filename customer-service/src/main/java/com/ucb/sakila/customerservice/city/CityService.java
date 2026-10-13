package com.ucb.sakila.customerservice.city;

import com.ucb.sakila.customerservice.country.CountryService;
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
public class CityService {

    private final CityRepository repository;
    private final CountryService countries;

    public PageResult<CityDto> findAll(int page, int limit) {
        var result = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent().stream().map(CityDto::fromEntity).toList(),
                PageInfo.of(page, limit, result.getTotalElements()));
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public CityDto findOneById(Integer id) {
        return CityDto.fromEntity(findEntity(id));
    }

    public CityDto create(CreateCityDto dto) {
        assertCountryExists(dto.countryId());
        City entity = new City();
        entity.setCity(dto.city());
        entity.setCountryId(dto.countryId());
        entity.setLastUpdate(LocalDateTime.now());
        return CityDto.fromEntity(save(entity));
    }

    public CityDto update(UpdateCityDto dto) {
        City entity = findEntity(dto.id());
        if (dto.countryId() != null) assertCountryExists(dto.countryId());
        if (dto.city() != null) entity.setCity(dto.city());
        if (dto.countryId() != null) entity.setCountryId(dto.countryId());
        entity.setLastUpdate(LocalDateTime.now());
        return CityDto.fromEntity(save(entity));
    }

    public void remove(Integer id) {
        if (!repository.existsById(id)) throw new CityNotFoundException();
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw PgErrors.translate(ex);
        }
    }

    private void assertCountryExists(Integer countryId) {
        if (!countries.existsById(countryId)) throw new InvalidCountryIdException(countryId);
    }

    private City findEntity(Integer id) {
        return repository.findById(id).orElseThrow(CityNotFoundException::new);
    }

    private City save(City entity) {
        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw PgErrors.translate(ex);
        }
    }
}

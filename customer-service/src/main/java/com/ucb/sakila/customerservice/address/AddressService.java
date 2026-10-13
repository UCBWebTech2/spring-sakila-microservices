package com.ucb.sakila.customerservice.address;

import com.ucb.sakila.customerservice.city.CityService;
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
public class AddressService {

    private final AddressRepository repository;
    private final CityService cities;

    public PageResult<AddressDto> findAll(int page, int limit) {
        var result = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent().stream().map(AddressDto::fromEntity).toList(),
                PageInfo.of(page, limit, result.getTotalElements()));
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public AddressDto findOneById(Integer id) {
        return AddressDto.fromEntity(findEntity(id));
    }

    public AddressDto create(CreateAddressDto dto) {
        assertCityExists(dto.cityId());
        Address entity = new Address();
        entity.setAddress(dto.address());
        entity.setAddress2(dto.address2());
        entity.setDistrict(dto.district());
        entity.setCityId(dto.cityId());
        entity.setPostalCode(dto.postalCode());
        entity.setPhone(dto.phone());
        entity.setLastUpdate(LocalDateTime.now());
        return AddressDto.fromEntity(save(entity));
    }

    public AddressDto update(UpdateAddressDto dto) {
        Address entity = findEntity(dto.id());
        if (dto.cityId() != null) assertCityExists(dto.cityId());
        if (dto.address() != null) entity.setAddress(dto.address());
        if (dto.address2() != null) entity.setAddress2(dto.address2());
        if (dto.district() != null) entity.setDistrict(dto.district());
        if (dto.cityId() != null) entity.setCityId(dto.cityId());
        if (dto.postalCode() != null) entity.setPostalCode(dto.postalCode());
        if (dto.phone() != null) entity.setPhone(dto.phone());
        entity.setLastUpdate(LocalDateTime.now());
        return AddressDto.fromEntity(save(entity));
    }

    public void remove(Integer id) {
        if (!repository.existsById(id)) throw new AddressNotFoundException();
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw PgErrors.translate(ex);
        }
    }

    private void assertCityExists(Integer cityId) {
        if (!cities.existsById(cityId)) throw new InvalidCityIdException(cityId);
    }

    private Address findEntity(Integer id) {
        return repository.findById(id).orElseThrow(AddressNotFoundException::new);
    }

    private Address save(Address entity) {
        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw PgErrors.translate(ex);
        }
    }
}

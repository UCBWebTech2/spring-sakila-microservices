package com.ucb.sakila.customerservice.customer;

import com.ucb.sakila.customerservice.address.AddressService;
import com.ucb.sakila.customerservice.shared.PageInfo;
import com.ucb.sakila.customerservice.shared.PageResult;
import com.ucb.sakila.customerservice.shared.PgErrors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final AddressService addresses;

    public PageResult<CustomerDto> findAll(int page, int limit) {
        var result = repository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent().stream().map(CustomerDto::fromEntity).toList(),
                PageInfo.of(page, limit, result.getTotalElements()));
    }

    public CustomerDto findOneById(Integer id) {
        return CustomerDto.fromEntity(findEntity(id));
    }

    public CustomerDto create(CreateCustomerDto dto) {
        assertAddressExists(dto.addressId());
        Customer entity = new Customer();
        entity.setStoreId(dto.storeId());
        entity.setFirstName(dto.firstName());
        entity.setLastName(dto.lastName());
        entity.setEmail(dto.email());
        entity.setAddressId(dto.addressId());
        entity.setActivebool(true);
        entity.setActive(1);
        entity.setCreateDate(LocalDate.now());
        entity.setLastUpdate(LocalDateTime.now());
        return CustomerDto.fromEntity(save(entity));
    }

    public CustomerDto update(UpdateCustomerDto dto) {
        Customer entity = findEntity(dto.id());
        if (dto.addressId() != null) assertAddressExists(dto.addressId());
        if (dto.storeId() != null) entity.setStoreId(dto.storeId());
        if (dto.firstName() != null) entity.setFirstName(dto.firstName());
        if (dto.lastName() != null) entity.setLastName(dto.lastName());
        if (dto.email() != null) entity.setEmail(dto.email());
        if (dto.addressId() != null) entity.setAddressId(dto.addressId());
        if (dto.active() != null) {
            entity.setActivebool(dto.active());
            entity.setActive(dto.active() ? 1 : 0);
        }
        entity.setLastUpdate(LocalDateTime.now());
        return CustomerDto.fromEntity(save(entity));
    }

    public void remove(Integer id) {
        if (!repository.existsById(id)) throw new CustomerNotFoundException();
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw PgErrors.translate(ex);
        }
    }

    private void assertAddressExists(Integer addressId) {
        if (!addresses.existsById(addressId)) throw new InvalidAddressIdException(addressId);
    }

    private Customer findEntity(Integer id) {
        return repository.findById(id).orElseThrow(CustomerNotFoundException::new);
    }

    private Customer save(Customer entity) {
        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw PgErrors.translate(ex);
        }
    }
}

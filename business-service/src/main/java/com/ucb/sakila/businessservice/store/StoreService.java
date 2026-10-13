package com.ucb.sakila.businessservice.store;

import com.ucb.sakila.businessservice.shared.PageInfo;
import com.ucb.sakila.businessservice.shared.PageResult;
import com.ucb.sakila.businessservice.staff.StaffNotFoundException;
import com.ucb.sakila.businessservice.staff.StaffRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StaffRepository staffRepository;

    public PageResult<Store> findAll(int page, int limit) {
        var result = storeRepository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent(), PageInfo.of(page, limit, result.getTotalElements()));
    }

    public Store findOneById(Integer id) {
        return storeRepository.findById(id).orElseThrow(StoreNotFoundException::new);
    }

    @Transactional
    public Store create(CreateStoreInput input) {
        if (!staffRepository.existsById(input.managerStaffId())) throw new StaffNotFoundException();

        Store store = new Store();
        store.setManagerStaffId(input.managerStaffId());
        store.setAddressId(input.addressId());
        store.setLastUpdate(LocalDateTime.now());
        return storeRepository.save(store);
    }

    @Transactional
    public Store update(Integer id, UpdateStoreInput input) {
        Store store = findOneById(id);
        if (input.managerStaffId() != null) {
            if (!staffRepository.existsById(input.managerStaffId())) throw new StaffNotFoundException();
            store.setManagerStaffId(input.managerStaffId());
        }
        if (input.addressId() != null) store.setAddressId(input.addressId());
        store.setLastUpdate(LocalDateTime.now());
        return store;
    }

    @Transactional
    public void remove(Integer id) {
        if (!storeRepository.existsById(id)) throw new StoreNotFoundException();
        storeRepository.deleteById(id);
    }
}

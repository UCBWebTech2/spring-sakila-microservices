package com.ucb.sakila.businessservice.staff;

import com.ucb.sakila.businessservice.shared.PageInfo;
import com.ucb.sakila.businessservice.shared.PageResult;
import com.ucb.sakila.businessservice.store.Store;
import com.ucb.sakila.businessservice.store.StoreNotFoundException;
import com.ucb.sakila.businessservice.store.StoreRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public PageResult<Staff> findAll(int page, int limit) {
        var result = staffRepository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent(), PageInfo.of(page, limit, result.getTotalElements()));
    }

    public Staff findOneById(Integer id) {
        return staffRepository.findById(id).orElseThrow(StaffNotFoundException::new);
    }

    @Transactional
    public Staff create(CreateStaffInput input) {
        if (staffRepository.existsByUsername(input.username())) {
            throw new StaffAlreadyExistsException();
        }
        Store store = storeRepository.findById(input.storeId()).orElseThrow(StoreNotFoundException::new);

        Staff staff = new Staff();
        staff.setFirstName(input.firstName());
        staff.setLastName(input.lastName());
        staff.setAddressId(input.addressId());
        staff.setEmail(input.email());
        staff.setStore(store);
        staff.setUsername(input.username());
        staff.setPassword(passwordEncoder.encode(input.password()));
        staff.setActive(true);
        staff.setLastUpdate(LocalDateTime.now());
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff update(Integer id, UpdateStaffInput input) {
        Staff staff = findOneById(id);
        if (input.firstName() != null) staff.setFirstName(input.firstName());
        if (input.lastName() != null) staff.setLastName(input.lastName());
        if (input.addressId() != null) staff.setAddressId(input.addressId());
        if (input.email() != null) staff.setEmail(input.email());
        if (input.storeId() != null) {
            staff.setStore(storeRepository.findById(input.storeId()).orElseThrow(StoreNotFoundException::new));
        }
        if (input.active() != null) staff.setActive(input.active());
        if (input.username() != null) staff.setUsername(input.username());
        staff.setLastUpdate(LocalDateTime.now());
        return staff;
    }

    @Transactional
    public void remove(Integer id) {
        if (!staffRepository.existsById(id)) throw new StaffNotFoundException();
        staffRepository.deleteById(id);
    }

    // "no existe", "contraseña incorrecta" e "inactivo" dan el mismo error, a propósito.
    public Staff verifyCredentials(String username, String rawPassword) {
        Staff staff = staffRepository.findByUsername(username).orElseThrow(InvalidCredentialsException::new);
        if (!staff.isActive() || !passwordEncoder.matches(rawPassword, staff.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return staff;
    }
}

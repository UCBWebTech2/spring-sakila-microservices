package com.ucb.sakila.businessservice.rental;

import com.ucb.sakila.businessservice.shared.PageInfo;
import com.ucb.sakila.businessservice.shared.PageResult;
import com.ucb.sakila.businessservice.shared.PgErrors;
import com.ucb.sakila.businessservice.staff.Staff;
import com.ucb.sakila.businessservice.staff.StaffNotFoundException;
import com.ucb.sakila.businessservice.staff.StaffRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final StaffRepository staffRepository;
    private final EntityManager entityManager;

    public PageResult<Rental> findAll(int page, int limit) {
        var result = rentalRepository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent(), PageInfo.of(page, limit, result.getTotalElements()));
    }

    public Rental findOneById(Integer id) {
        return rentalRepository.findById(id).orElseThrow(RentalNotFoundException::new);
    }

    @Transactional
    public Rental create(CreateRentalInput input) {
        Staff staff = staffRepository.findById(input.staffId()).orElseThrow(StaffNotFoundException::new);

        Rental rental = new Rental();
        rental.setRentalDate(LocalDateTime.parse(input.rentalDate()));
        rental.setInventoryId(input.inventoryId());
        rental.setCustomerId(input.customerId());
        rental.setStaff(staff);
        rental.setLastUpdate(LocalDateTime.now());
        try {
            return rentalRepository.save(rental);
        } catch (RuntimeException ex) {
            throw PgErrors.translate(ex);
        }
    }

    @Transactional
    public Rental update(Integer id, UpdateRentalInput input) {
        Rental rental = findOneById(id);
        if (input.returnDate() != null) rental.setReturnDate(LocalDateTime.parse(input.returnDate()));
        if (input.staffId() != null) {
            rental.setStaff(staffRepository.findById(input.staffId()).orElseThrow(StaffNotFoundException::new));
        }
        rental.setLastUpdate(LocalDateTime.now());
        try {
            entityManager.flush();
        } catch (RuntimeException ex) {
            throw PgErrors.translate(ex);
        }
        return rental;
    }

    @Transactional
    public void remove(Integer id) {
        if (!rentalRepository.existsById(id)) throw new RentalNotFoundException();
        try {
            rentalRepository.deleteById(id);
            entityManager.flush();
        } catch (RuntimeException ex) {
            throw PgErrors.translate(ex);
        }
    }
}

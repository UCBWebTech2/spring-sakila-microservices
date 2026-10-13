package com.ucb.sakila.businessservice.rental;

import com.ucb.sakila.businessservice.shared.PageResult;
import com.ucb.sakila.businessservice.staff.Staff;
import com.ucb.sakila.businessservice.staff.StaffRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final StaffRepository staffRepository;

    @QueryMapping
    public Rental rental(@Argument Integer id) {
        return rentalService.findOneById(id);
    }

    @QueryMapping
    public PageResult<Rental> rentals(@Argument Integer page, @Argument Integer limit) {
        return rentalService.findAll(page, limit);
    }

    @MutationMapping
    public Rental createRental(@Argument CreateRentalInput input) {
        return rentalService.create(input);
    }

    @MutationMapping
    public Rental updateRental(@Argument Integer id, @Argument UpdateRentalInput input) {
        return rentalService.update(id, input);
    }

    @MutationMapping
    public Boolean removeRental(@Argument Integer id) {
        rentalService.remove(id);
        return true;
    }

    @BatchMapping
    public Map<Rental, Staff> staff(List<Rental> rentals) {
        List<Integer> staffIds = rentals.stream().map(r -> r.getStaff().getId()).distinct().toList();
        Map<Integer, Staff> staffById = staffRepository.findAllById(staffIds).stream()
                .collect(Collectors.toMap(Staff::getId, s -> s));
        return rentals.stream().collect(Collectors.toMap(r -> r, r -> staffById.get(r.getStaff().getId())));
    }
}

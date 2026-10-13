package com.ucb.sakila.businessservice.payment;

import com.ucb.sakila.businessservice.rental.Rental;
import com.ucb.sakila.businessservice.rental.RentalRepository;
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
public class PaymentController {

    private final PaymentService paymentService;
    private final StaffRepository staffRepository;
    private final RentalRepository rentalRepository;

    @QueryMapping
    public Payment payment(@Argument Integer id) {
        return paymentService.findOneById(id);
    }

    @QueryMapping
    public PageResult<Payment> payments(@Argument Integer page, @Argument Integer limit) {
        return paymentService.findAll(page, limit);
    }

    @MutationMapping
    public Payment createPayment(@Argument CreatePaymentInput input) {
        return paymentService.create(input);
    }

    @MutationMapping
    public Boolean removePayment(@Argument Integer id) {
        paymentService.remove(id);
        return true;
    }

    @BatchMapping
    public Map<Payment, Staff> staff(List<Payment> payments) {
        List<Integer> staffIds = payments.stream().map(p -> p.getStaff().getId()).distinct().toList();
        Map<Integer, Staff> staffById = staffRepository.findAllById(staffIds).stream()
                .collect(Collectors.toMap(Staff::getId, s -> s));
        return payments.stream().collect(Collectors.toMap(p -> p, p -> staffById.get(p.getStaff().getId())));
    }

    @BatchMapping
    public Map<Payment, Rental> rental(List<Payment> payments) {
        List<Integer> rentalIds = payments.stream().map(p -> p.getRental().getId()).distinct().toList();
        Map<Integer, Rental> rentalById = rentalRepository.findAllById(rentalIds).stream()
                .collect(Collectors.toMap(Rental::getId, r -> r));
        return payments.stream().collect(Collectors.toMap(p -> p, p -> rentalById.get(p.getRental().getId())));
    }
}

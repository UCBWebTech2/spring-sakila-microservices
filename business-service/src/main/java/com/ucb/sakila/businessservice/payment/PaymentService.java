package com.ucb.sakila.businessservice.payment;

import com.ucb.sakila.businessservice.rental.Rental;
import com.ucb.sakila.businessservice.rental.RentalNotFoundException;
import com.ucb.sakila.businessservice.rental.RentalRepository;
import com.ucb.sakila.businessservice.shared.PageInfo;
import com.ucb.sakila.businessservice.shared.PageResult;
import com.ucb.sakila.businessservice.staff.Staff;
import com.ucb.sakila.businessservice.staff.StaffNotFoundException;
import com.ucb.sakila.businessservice.staff.StaffRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StaffRepository staffRepository;
    private final RentalRepository rentalRepository;

    public PageResult<Payment> findAll(int page, int limit) {
        var result = paymentRepository.findAll(PageRequest.of(page - 1, limit, Sort.by("id")));
        return new PageResult<>(result.getContent(), PageInfo.of(page, limit, result.getTotalElements()));
    }

    public Payment findOneById(Integer id) {
        return paymentRepository.findById(id).orElseThrow(PaymentNotFoundException::new);
    }

    @Transactional
    public Payment create(CreatePaymentInput input) {
        Staff staff = staffRepository.findById(input.staffId()).orElseThrow(StaffNotFoundException::new);
        Rental rental = rentalRepository.findById(input.rentalId()).orElseThrow(RentalNotFoundException::new);

        Payment payment = new Payment();
        payment.setCustomerId(input.customerId());
        payment.setStaff(staff);
        payment.setRental(rental);
        payment.setAmount(BigDecimal.valueOf(input.amount()));
        payment.setPaymentDate(LocalDateTime.parse(input.paymentDate()));
        return paymentRepository.save(payment);
    }

    @Transactional
    public void remove(Integer id) {
        if (!paymentRepository.existsById(id)) throw new PaymentNotFoundException();
        paymentRepository.deleteById(id);
    }
}

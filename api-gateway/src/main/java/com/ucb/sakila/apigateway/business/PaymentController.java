package com.ucb.sakila.apigateway.business;

import com.ucb.sakila.apigateway.shared.BusinessServiceGraphQlClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final String FIELDS = "id customerId staff { id } rental { id } amount paymentDate";

    private final BusinessServiceGraphQlClient client;

    public record CreatePaymentBody(Integer customerId, Integer staffId, Integer rentalId, Double amount, String paymentDate) {
    }

    @SuppressWarnings("unchecked")
    @GetMapping
    public PageResponse<PaymentDto> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        var data = client.query("query($page:Int,$limit:Int){ payments(page:$page,limit:$limit){ data { " + FIELDS + " } meta { page limit total pages } } }",
                Map.of("page", page, "limit", limit));
        Map<String, Object> payments = (Map<String, Object>) data.get("payments");
        List<PaymentDto> items = ((List<Map<String, Object>>) payments.get("data")).stream().map(PaymentDto::from).toList();
        return new PageResponse<>(items, PageMeta.from(payments.get("meta")));
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/{id}")
    public PaymentDto get(@PathVariable int id) {
        var data = client.query("query($id:ID!){ payment(id:$id){ " + FIELDS + " } }", Map.of("id", id));
        return PaymentDto.from((Map<String, Object>) data.get("payment"));
    }

    @SuppressWarnings("unchecked")
    @PostMapping
    public PaymentDto create(@RequestBody CreatePaymentBody body) {
        Map<String, Object> input = new HashMap<>();
        input.put("customerId", body.customerId());
        input.put("staffId", body.staffId());
        input.put("rentalId", body.rentalId());
        input.put("amount", body.amount());
        input.put("paymentDate", body.paymentDate());
        var data = client.query("mutation($input:CreatePaymentInput!){ createPayment(input:$input){ " + FIELDS + " } }", Map.of("input", input));
        return PaymentDto.from((Map<String, Object>) data.get("createPayment"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int id) {
        client.query("mutation($id:ID!){ removePayment(id:$id) }", Map.of("id", id));
    }
}

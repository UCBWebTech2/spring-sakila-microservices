package com.ucb.sakila.apigateway.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucb.sakila.apigateway.shared.ApiException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Se deserializa siempre a un Map primero (no __TypeId__: no comparte clases con customer-service).
@Component
@RequiredArgsConstructor
public class CustomerAmqpClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public <T> T call(String queue, Object payload, Class<T> responseType) {
        return objectMapper.convertValue(request(queue, payload), responseType);
    }

    public <T> PageResponse<T> callPage(String queue, Object payload, Class<T> itemType) {
        Map<String, Object> response = request(queue, payload);
        List<T> data = ((List<?>) response.get("data")).stream()
                .map(item -> objectMapper.convertValue(item, itemType)).toList();
        Map<?, ?> meta = (Map<?, ?>) response.get("meta");
        return new PageResponse<>(data, new PageMeta(
                (Integer) meta.get("page"), (Integer) meta.get("limit"),
                ((Number) meta.get("total")).longValue(), (Integer) meta.get("pages")));
    }

    private Map<String, Object> request(String queue, Object payload) {
        Map<String, Object> response;
        try {
            response = rabbitTemplate.convertSendAndReceiveAsType(queue, payload, MAP_TYPE);
        } catch (AmqpException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CUSTOMER_SERVICE_UNREACHABLE", "customer-service is unreachable.");
        }
        if (response == null) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "CUSTOMER_SERVICE_TIMEOUT", "customer-service timed out.");
        }
        if (response.containsKey("statusCode") && response.containsKey("error")) {
            HttpStatus status = HttpStatus.resolve((Integer) response.get("statusCode"));
            throw new ApiException(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR,
                    (String) response.get("error"), (String) response.get("message"));
        }
        return response;
    }
}

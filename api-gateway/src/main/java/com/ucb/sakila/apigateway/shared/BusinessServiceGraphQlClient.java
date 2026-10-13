package com.ucb.sakila.apigateway.shared;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Cliente GraphQL genérico hacia business-service — resuelve el host vía Eureka/LoadBalancer.
@Component
public class BusinessServiceGraphQlClient {

    private final RestClient restClient;

    public BusinessServiceGraphQlClient(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://business-service/graphql").build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> query(String query, Map<String, Object> variables) {
        Map<String, Object> response = restClient.post()
                .body(Map.of("query", query, "variables", variables))
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> errors = (List<Map<String, Object>>) response.get("errors");
        if (errors != null && !errors.isEmpty()) {
            Map<String, Object> firstError = errors.get(0);
            Map<String, Object> extensions = (Map<String, Object>) firstError.get("extensions");
            String code = extensions != null ? (String) extensions.get("code") : "BUSINESS_SERVICE_ERROR";
            int statusCode = extensions != null && extensions.get("statusCode") != null
                    ? ((Number) extensions.get("statusCode")).intValue()
                    : 500;
            throw new ApiException(HttpStatus.valueOf(statusCode), code, (String) firstError.get("message"));
        }
        return (Map<String, Object>) response.get("data");
    }
}

package com.ucb.sakila.apigateway.inventory;

import com.ucb.sakila.apigateway.shared.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;

// WebServiceTemplate no soporta @LoadBalanced — se resuelve a mano con LoadBalancerClient.
@Component
@RequiredArgsConstructor
public class InventorySoapClient {

    private final WebServiceTemplate webServiceTemplate;
    private final LoadBalancerClient loadBalancerClient;

    @SuppressWarnings("unchecked")
    public <T> T call(Object request) {
        ServiceInstance instance = loadBalancerClient.choose("inventory-service");
        if (instance == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "INVENTORY_SERVICE_UNREACHABLE", "inventory-service is unreachable.");
        }
        String url = instance.getUri() + "/ws";
        try {
            return (T) webServiceTemplate.marshalSendAndReceive(url, request);
        } catch (SoapFaultClientException ex) {
            throw translateFault(ex);
        }
    }

    private ApiException translateFault(SoapFaultClientException ex) {
        String text = ex.getFaultStringOrReason();
        int sep = text.indexOf(": ");
        String code = sep > 0 ? text.substring(0, sep) : "INVENTORY_SERVICE_ERROR";
        String message = sep > 0 ? text.substring(sep + 2) : text;
        return new ApiException(statusForCode(code), code, message);
    }

    private HttpStatus statusForCode(String code) {
        if (code.endsWith("_NOT_FOUND")) return HttpStatus.NOT_FOUND;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

package com.ucb.sakila.inventoryservice.ws;

import com.ucb.sakila.inventoryservice.shared.InventoryServiceException;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.server.endpoint.AbstractSoapFaultDefinitionExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

// Mete "CODE: message" en el faultstring.
@Component
public class FaultExceptionResolver extends AbstractSoapFaultDefinitionExceptionResolver {

    @Override
    protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
        SoapFaultDefinition definition = new SoapFaultDefinition();
        definition.setFaultCode(SoapFaultDefinition.SERVER);
        definition.setFaultStringOrReason(ex instanceof InventoryServiceException ise
                ? ise.getCode() + ": " + ise.getMessage()
                : "INTERNAL_SERVER_ERROR: " + ex.getMessage());
        return definition;
    }
}

package com.ucb.sakila.apigateway.inventory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class InventoryWsConfig {

    @Bean
    public Jaxb2Marshaller inventoryMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.ucb.sakila.apigateway.inventory.ws.generated");
        return marshaller;
    }

    @Bean
    public WebServiceTemplate inventoryWebServiceTemplate(Jaxb2Marshaller inventoryMarshaller) {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(inventoryMarshaller);
        template.setUnmarshaller(inventoryMarshaller);
        return template;
    }
}

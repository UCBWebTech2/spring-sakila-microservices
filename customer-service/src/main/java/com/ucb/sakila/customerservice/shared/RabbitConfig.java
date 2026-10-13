package com.ucb.sakila.customerservice.shared;

import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Una cola de petición-respuesta por operación.
@Configuration
public class RabbitConfig {

    public static final String[] QUEUES = {
            "cities.find-all", "cities.find-one", "cities.create", "cities.update", "cities.remove",
            "countries.find-all", "countries.find-one", "countries.create", "countries.update", "countries.remove",
    };

    @Bean
    public Declarables queues() {
        return new Declarables(java.util.Arrays.stream(QUEUES).map(Queue::new).toArray(Queue[]::new));
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

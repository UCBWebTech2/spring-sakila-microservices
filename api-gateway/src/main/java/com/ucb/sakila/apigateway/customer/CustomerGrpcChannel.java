package com.ucb.sakila.apigateway.customer;

import com.ucb.sakila.apigateway.shared.ApiException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Puerto gRPC leído de la metadata de Eureka. Canal cacheado: ManagedChannel ya reconecta solo.
@Component
@RequiredArgsConstructor
public class CustomerGrpcChannel {

    private final LoadBalancerClient loadBalancerClient;
    private volatile ManagedChannel channel;

    public synchronized ManagedChannel get() {
        if (channel != null && !channel.isShutdown()) return channel;
        ServiceInstance instance = loadBalancerClient.choose("customer-service");
        if (instance == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "CUSTOMER_SERVICE_UNREACHABLE", "customer-service is unreachable.");
        }
        String grpcPort = instance.getMetadata().get("grpc-port");
        if (grpcPort == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "CUSTOMER_SERVICE_UNREACHABLE", "customer-service did not publish its gRPC port.");
        }
        channel = ManagedChannelBuilder.forAddress(instance.getHost(), Integer.parseInt(grpcPort))
                .usePlaintext()
                .build();
        return channel;
    }
}

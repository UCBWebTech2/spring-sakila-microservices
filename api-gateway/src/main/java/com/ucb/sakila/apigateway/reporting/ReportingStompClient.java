package com.ucb.sakila.apigateway.reporting;

import com.ucb.sakila.apigateway.shared.ApiException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

// Sesión STOMP persistente; /queue/errors es compartido, se filtra por "operation".
@Component
@RequiredArgsConstructor
public class ReportingStompClient {

    private final WebSocketStompClient stompClient;
    private final LoadBalancerClient loadBalancerClient;
    private volatile StompSession session;

    private synchronized StompSession getSession() {
        if (session != null && session.isConnected()) return session;
        ServiceInstance instance = loadBalancerClient.choose("reporting-service");
        if (instance == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "REPORTING_SERVICE_UNREACHABLE", "reporting-service is unreachable.");
        }
        String url = "ws://" + instance.getHost() + ":" + instance.getPort() + "/ws";
        try {
            session = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {})
                    .get(5, TimeUnit.SECONDS);
            return session;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "REPORTING_SERVICE_UNREACHABLE", "reporting-service is unreachable.");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T call(String operation, Object payload, Class<T> responseType) {
        StompSession stompSession = getSession();
        CompletableFuture<Object> future = new CompletableFuture<>();

        StompSession.Subscription dataSub = stompSession.subscribe("/user/queue/" + operation,
                new StompFrameHandler() {
                    public Type getPayloadType(StompHeaders headers) { return responseType; }
                    public void handleFrame(StompHeaders headers, Object body) { future.complete(body); }
                });
        StompSession.Subscription errorSub = stompSession.subscribe("/user/queue/errors",
                new StompFrameHandler() {
                    public Type getPayloadType(StompHeaders headers) { return Map.class; }
                    public void handleFrame(StompHeaders headers, Object body) {
                        Map<String, Object> error = (Map<String, Object>) body;
                        if (!operation.equals(error.get("operation"))) return;
                        String code = (String) error.get("code");
                        future.completeExceptionally(new ApiException(
                                statusForCode(code), code, (String) error.get("message")));
                    }
                });

        stompSession.send("/app/" + operation, payload);

        try {
            return (T) future.get(10, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            if (e.getCause() instanceof ApiException apiEx) throw apiEx;
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "REPORTING_SERVICE_ERROR", e.getCause().getMessage());
        } catch (TimeoutException e) {
            throw new ApiException(HttpStatus.GATEWAY_TIMEOUT, "REPORTING_SERVICE_TIMEOUT", "reporting-service timed out.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "REPORTING_SERVICE_ERROR", "Interrupted.");
        } finally {
            dataSub.unsubscribe();
            errorSub.unsubscribe();
        }
    }

    private HttpStatus statusForCode(String code) {
        if ("INVALID_PARAMS".equals(code)) return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

package com.ucb.sakila.reportingservice.reports;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

// Un @SendToUser por operación; los errores van aparte, a /queue/errors.
@Controller
@RequiredArgsConstructor
@Slf4j
public class ReportsController {

    private final ReportsService service;

    @MessageMapping("/getActorInfo")
    @SendToUser("/queue/getActorInfo")
    public Map<String, Object> getActorInfo(@Payload PageRequestBody body) {
        return toPage(service.getActorInfo(body.pageOrDefault(), body.limitOrDefault()));
    }

    @MessageMapping("/getCustomerList")
    @SendToUser("/queue/getCustomerList")
    public Map<String, Object> getCustomerList(@Payload PageRequestBody body) {
        return toPage(service.getCustomerList(body.pageOrDefault(), body.limitOrDefault()));
    }

    @MessageMapping("/getFilmList")
    @SendToUser("/queue/getFilmList")
    public Map<String, Object> getFilmList(@Payload PageRequestBody body) {
        return toPage(service.getFilmList(body.pageOrDefault(), body.limitOrDefault()));
    }

    @MessageMapping("/getNicerButSlowerFilmList")
    @SendToUser("/queue/getNicerButSlowerFilmList")
    public Map<String, Object> getNicerButSlowerFilmList(@Payload PageRequestBody body) {
        return toPage(service.getNicerButSlowerFilmList(body.pageOrDefault(), body.limitOrDefault()));
    }

    @MessageMapping("/getSalesByFilmCategory")
    @SendToUser("/queue/getSalesByFilmCategory")
    public Map<String, Object> getSalesByFilmCategory(@Payload PageRequestBody body) {
        return toPage(service.getSalesByFilmCategory(body.pageOrDefault(), body.limitOrDefault()));
    }

    @MessageMapping("/getSalesByStore")
    @SendToUser("/queue/getSalesByStore")
    public Map<String, Object> getSalesByStore(@Payload PageRequestBody body) {
        return toPage(service.getSalesByStore(body.pageOrDefault(), body.limitOrDefault()));
    }

    @MessageMapping("/getStaffList")
    @SendToUser("/queue/getStaffList")
    public Map<String, Object> getStaffList(@Payload PageRequestBody body) {
        return toPage(service.getStaffList(body.pageOrDefault(), body.limitOrDefault()));
    }

    @MessageMapping("/getFilmInStock")
    @SendToUser("/queue/getFilmInStock")
    public List<Integer> getFilmInStock(@Payload FilmStockRequest body) {
        return service.getFilmInStock(body.filmId(), body.storeId());
    }

    @MessageMapping("/getFilmNotInStock")
    @SendToUser("/queue/getFilmNotInStock")
    public List<Integer> getFilmNotInStock(@Payload FilmStockRequest body) {
        return service.getFilmNotInStock(body.filmId(), body.storeId());
    }

    @MessageMapping("/getCustomerBalance")
    @SendToUser("/queue/getCustomerBalance")
    public Map<String, Object> getCustomerBalance(@Payload CustomerBalanceRequest body) {
        return Map.of("balance", service.getCustomerBalance(body.customerId(), body.effectiveDate()));
    }

    @MessageMapping("/getInventoryHeldByCustomer")
    @SendToUser("/queue/getInventoryHeldByCustomer")
    public Map<String, Object> getInventoryHeldByCustomer(@Payload InventoryIdRequest body) {
        Integer customerId = service.getInventoryHeldByCustomer(body.inventoryId());
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("customerId", customerId);
        return result;
    }

    @MessageMapping("/getInventoryInStock")
    @SendToUser("/queue/getInventoryInStock")
    public Map<String, Object> getInventoryInStock(@Payload InventoryIdRequest body) {
        return Map.of("inStock", service.getInventoryInStock(body.inventoryId()));
    }

    @MessageMapping("/getRewardsReport")
    @SendToUser("/queue/getRewardsReport")
    public List<Map<String, Object>> getRewardsReport(@Payload RewardsRequest body) {
        return service.getRewardsReport(body.minMonthlyPurchases(), body.minDollarAmountPurchased());
    }

    // /queue/errors es compartido; "operation" (de simpDestination) dice a cuál pedido pertenece.
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public Map<String, Object> handleFault(ReportFault fault, Message<?> message) {
        return Map.of("code", fault.getCode(), "message", fault.getMessage(), "operation", operationOf(message));
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public Map<String, Object> handleUnexpected(Exception ex, Message<?> message) {
        log.error("Unexpected error handling a report request", ex);
        return Map.of("code", "INTERNAL_ERROR", "message", "Internal server error.", "operation", operationOf(message));
    }

    private String operationOf(Message<?> message) {
        String destination = SimpMessageHeaderAccessor.getDestination(message.getHeaders());
        if (destination == null) return "";
        int lastSlash = destination.lastIndexOf('/');
        return lastSlash >= 0 ? destination.substring(lastSlash + 1) : destination;
    }

    private Map<String, Object> toPage(PageResult result) {
        return Map.of("data", result.data(), "meta", PageMeta.from(result));
    }
}

package com.ucb.sakila.apigateway.business;

import java.util.Map;

public record RentalDto(int id, String rentalDate, int inventoryId, int customerId, String returnDate,
        int staffId, String lastUpdate) {

    @SuppressWarnings("unchecked")
    static RentalDto from(Map<String, Object> rental) {
        Map<String, Object> staff = (Map<String, Object>) rental.get("staff");
        return new RentalDto(
                Integer.parseInt(rental.get("id").toString()),
                (String) rental.get("rentalDate"),
                (Integer) rental.get("inventoryId"),
                (Integer) rental.get("customerId"),
                (String) rental.get("returnDate"),
                Integer.parseInt(staff.get("id").toString()),
                (String) rental.get("lastUpdate"));
    }
}

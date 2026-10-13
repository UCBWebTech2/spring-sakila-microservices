package com.ucb.sakila.reportingservice.reports;

public record CustomerBalanceRequest(int customerId, String effectiveDate) {
}

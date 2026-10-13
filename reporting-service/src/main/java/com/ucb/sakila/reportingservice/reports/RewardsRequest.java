package com.ucb.sakila.reportingservice.reports;

public record RewardsRequest(int minMonthlyPurchases, String minDollarAmountPurchased) {
}

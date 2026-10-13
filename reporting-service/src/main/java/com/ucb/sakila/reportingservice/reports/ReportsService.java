package com.ucb.sakila.reportingservice.reports;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final JdbcTemplate jdbcTemplate;

    // ORDER BY 1 (posicional): las vistas no comparten un nombre de columna "id" común.
    private PageResult paginateView(String viewName, int page, int limit) {
        int offset = (page - 1) * limit;
        List<Map<String, Object>> data = jdbcTemplate.queryForList(
                "SELECT * FROM " + viewName + " ORDER BY 1 LIMIT ? OFFSET ?", limit, offset);
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + viewName, Long.class);
        return new PageResult(data, page, limit, total);
    }

    public PageResult getActorInfo(int page, int limit) {
        return paginateView("actor_info", page, limit);
    }

    public PageResult getCustomerList(int page, int limit) {
        return paginateView("customer_list", page, limit);
    }

    public PageResult getFilmList(int page, int limit) {
        return paginateView("film_list", page, limit);
    }

    public PageResult getNicerButSlowerFilmList(int page, int limit) {
        return paginateView("nicer_but_slower_film_list", page, limit);
    }

    public PageResult getSalesByFilmCategory(int page, int limit) {
        return paginateView("sales_by_film_category", page, limit);
    }

    public PageResult getSalesByStore(int page, int limit) {
        return paginateView("sales_by_store", page, limit);
    }

    public PageResult getStaffList(int page, int limit) {
        return paginateView("staff_list", page, limit);
    }

    // p_film_count es el nombre del OUT parameter, no un conteo: es un inventory id.
    public List<Integer> getFilmInStock(int filmId, int storeId) {
        assertPositive(filmId, "filmId");
        assertPositive(storeId, "storeId");
        return jdbcTemplate.queryForList(
                "SELECT * FROM film_in_stock(?, ?)", Integer.class, filmId, storeId);
    }

    public List<Integer> getFilmNotInStock(int filmId, int storeId) {
        assertPositive(filmId, "filmId");
        assertPositive(storeId, "storeId");
        return jdbcTemplate.queryForList(
                "SELECT * FROM film_not_in_stock(?, ?)", Integer.class, filmId, storeId);
    }

    // get_customer_balance() usa sintaxis MySQL (IF) rota en Postgres — se recalcula acá.
    public BigDecimal getCustomerBalance(int customerId, String effectiveDate) {
        assertPositive(customerId, "customerId");
        Timestamp date = Timestamp.valueOf(effectiveDate.replace("T", " ").replace("Z", ""));

        BigDecimal rentFees = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(film.rental_rate), 0) FROM film, inventory, rental
                WHERE film.film_id = inventory.film_id AND inventory.inventory_id = rental.inventory_id
                  AND rental.rental_date <= ? AND rental.customer_id = ?
                """, BigDecimal.class, date, customerId);

        BigDecimal overFees = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(
                    CASE WHEN (rental.return_date - rental.rental_date) > (film.rental_duration * interval '1 day')
                         THEN EXTRACT(DAY FROM (rental.return_date - rental.rental_date) - (film.rental_duration * interval '1 day'))
                         ELSE 0 END
                ), 0) FROM rental, inventory, film
                WHERE film.film_id = inventory.film_id AND inventory.inventory_id = rental.inventory_id
                  AND rental.rental_date <= ? AND rental.customer_id = ?
                """, BigDecimal.class, date, customerId);

        BigDecimal payments = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(amount), 0) FROM payment
                WHERE customer_id = ? AND payment_date <= ?
                """, BigDecimal.class, customerId, date);

        return rentFees.add(overFees).subtract(payments);
    }

    public Integer getInventoryHeldByCustomer(int inventoryId) {
        assertPositive(inventoryId, "inventoryId");
        return jdbcTemplate.queryForObject(
                "SELECT inventory_held_by_customer(?)", Integer.class, inventoryId);
    }

    public Boolean getInventoryInStock(int inventoryId) {
        assertPositive(inventoryId, "inventoryId");
        return jdbcTemplate.queryForObject(
                "SELECT inventory_in_stock(?)", Boolean.class, inventoryId);
    }

    public List<Map<String, Object>> getRewardsReport(int minMonthlyPurchases, String minDollarAmountPurchased) {
        assertPositive(minMonthlyPurchases, "minMonthlyPurchases");
        return jdbcTemplate.queryForList(
                "SELECT * FROM rewards_report(?, ?)", minMonthlyPurchases, new BigDecimal(minDollarAmountPurchased));
    }

    private void assertPositive(int value, String field) {
        if (value <= 0) throw new InvalidParamsFault(field + " must be a positive integer.");
    }
}

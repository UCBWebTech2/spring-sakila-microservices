package com.ucb.sakila.customerservice.shared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;

// Traduce una violación de FK de Postgres (23503/23001) a una excepción de dominio.
public final class PgErrors {

    private static final Pattern MISSING = Pattern.compile("=\\((.+?)\\) is not present in table \"(.+?)\"");
    private static final Pattern REFERENCED = Pattern.compile("is (?:still )?referenced from table \"(.+?)\"");

    private PgErrors() {
    }

    public static RuntimeException translate(DataIntegrityViolationException ex) {
        PSQLException psql = findPsqlException(ex);
        String sqlState = psql == null ? null : psql.getSQLState();
        if (!"23503".equals(sqlState) && !"23001".equals(sqlState)) return ex;

        String detail = psql.getServerErrorMessage() == null ? null : psql.getServerErrorMessage().getDetail();
        boolean referencedByDefault = "23001".equals(sqlState);

        if (detail != null) {
            Matcher missing = MISSING.matcher(detail);
            if (missing.find()) return invalidReference(normalize(missing.group(2)), missing.group(1));

            Matcher referenced = REFERENCED.matcher(detail);
            if (referenced.find()) return stillReferenced(normalize(referenced.group(1)));
        }
        return referencedByDefault ? stillReferenced(null) : invalidReference(null, null);
    }

    private static RuntimeException invalidReference(String table, String value) {
        if (table == null) return new InvalidReferenceException("INVALID_REFERENCE", "One of the referenced ids does not exist.");
        return new InvalidReferenceException("INVALID_" + table.toUpperCase() + "_ID", label(table) + " " + value + " does not exist.");
    }

    private static RuntimeException stillReferenced(String table) {
        return new StillReferencedException(table == null
                ? "Cannot delete: other records still reference it."
                : "Cannot delete: it is still referenced by " + table + " records.");
    }

    private static String normalize(String table) {
        return table.replaceAll("_p\\d{4}_\\d{2}$", "");
    }

    private static String label(String table) {
        String text = table.replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static PSQLException findPsqlException(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof PSQLException psql) return psql;
        }
        return null;
    }
}

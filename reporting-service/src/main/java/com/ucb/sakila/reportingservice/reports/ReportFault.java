package com.ucb.sakila.reportingservice.reports;

import lombok.Getter;

@Getter
public abstract class ReportFault extends RuntimeException {

    private final String code;

    protected ReportFault(String code, String message) {
        super(message);
        this.code = code;
    }
}

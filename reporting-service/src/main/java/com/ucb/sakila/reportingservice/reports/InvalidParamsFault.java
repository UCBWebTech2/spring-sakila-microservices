package com.ucb.sakila.reportingservice.reports;

public class InvalidParamsFault extends ReportFault {
    public InvalidParamsFault(String message) {
        super("INVALID_PARAMS", message);
    }
}

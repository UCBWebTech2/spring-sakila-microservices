package com.ucb.sakila.inventoryservice.ws;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;

public final class XmlDates {

    private XmlDates() {}

    public static XMLGregorianCalendar from(LocalDateTime value) {
        try {
            GregorianCalendar calendar = GregorianCalendar.from(value.atZone(ZoneId.systemDefault()));
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

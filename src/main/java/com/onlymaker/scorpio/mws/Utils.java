package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.client.MwsUtl;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;

public class Utils {
    public static XMLGregorianCalendar getXMLGregorianCalendar(LocalDate date) {
        XMLGregorianCalendar result = MwsUtl.getDTF().newXMLGregorianCalendar();
        result.setYear(date.getYear());
        result.setMonth(date.getMonthValue());
        result.setDay(date.getDayOfMonth());
        result.setHour(0);
        result.setMinute(0);
        result.setSecond(0);
        return result;
    }
}

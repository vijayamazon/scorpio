package com.onlymaker.scorpio.mws;

import com.amazonservices.mws.client.MwsUtl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static Pattern SELLER_SKU = Pattern.compile("(?<sku>\\w*\\d+\\w*)-.*(?<size>US\\d+\\.?\\d?|UK\\d+\\.?\\d?|EU\\d+\\.?\\d?)");
    public static Pattern MATCHING_PRODUCT_ASIN = Pattern.compile("<ASIN>(?<asin>[^<]*)");
    public static Pattern MATCHING_PRODUCT_ATTR = Pattern.compile("<ns2:(?<name>[^>]*)>(?<value>[^<]*)");
    public static Pattern MATCHING_PRODUCT_RANK = Pattern.compile("<ProductCategoryId>(?<name>[^<]*)</ProductCategoryId><Rank>(?<rank>[^<]*)</Rank>");

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

    public static String getJsonString(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public static Map<String, String> parseSellerSku(String sellerSku) {
        Map<String, String> map = new HashMap<>();
        Matcher matcher = SELLER_SKU.matcher(sellerSku.toUpperCase());
        if (matcher.find()) {
            map.put("sku", matcher.group("sku"));
            map.put("size", matcher.group("size"));
        } else {
            map.put("sku", "");
            map.put("size", "");
        }
        return map;
    }
}

package com.onlymaker.scorpio.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RunWith(JUnit4.class)
public class DateTest {
    @Test
    public void string2Date() throws ParseException {
        String s = "2019-06-02T07:00:00";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime parse = LocalDateTime.parse(s, dtf);
        System.out.println(parse);
        s = "2019-06-02";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(sdf.parse(s));
    }
}

package com.onlymaker.scorpio.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(JUnit4.class)
public class PatternTest {

    @Test
    public void parseSku() {
        Pattern pattern = Pattern.compile("(?<country>\\w{2})-(?<sku>\\w+\\d+).*(?<size>US\\d+|UK\\d+|EU\\d+)");
        new ArrayList<String>() {{
            add("CLUS-XY624-US5-FBA");
            add("CLUS-XY624-US5-FBA-X0");
            add("CLUS-XY624-US-FBA-X0");
            add("RNDE-XY624-EU46-FBAA");
        }}.forEach(s -> {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                System.out.println("==========");
                System.out.println("country: " + matcher.group("country"));
                System.out.println("sku: " + matcher.group("sku"));
                System.out.println("size: " + matcher.group("size"));
            }
        });
    }
}

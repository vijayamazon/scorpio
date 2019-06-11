package com.onlymaker.scorpio.utils;

import com.onlymaker.scorpio.mws.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.regex.Matcher;

@RunWith(JUnit4.class)
public class PatternTest {

    @Test
    public void parseSku() {
        new ArrayList<String>() {{
            add("CLUS-XY624-US5-FBA");
            add("CLUS-XY624-US-FBA-X0");
            add("RNDE-XY624-EU46-FBA");
            add("CLUS-P8405A-US10-FBA");
            add("ARUS--PC8102-Black-FBA-US10");
            add("KHUK-xy624-uk8-New-FBA");
            add("RNDEM-MRS17059B-EU44-FBA");
            add("RNDE-xy447-EU42-FBA");
            add("CLUS-vk0163-US9.5-FBA-X0");
        }}.forEach(s -> {
            Matcher matcher = Utils.SELLER_SKU.matcher(s.toUpperCase());
            if (matcher.find()) {
                System.out.println("==========");
                System.out.println("sku: " + matcher.group("sku"));
                System.out.println("size: " + matcher.group("size"));
            }
        });
    }
}

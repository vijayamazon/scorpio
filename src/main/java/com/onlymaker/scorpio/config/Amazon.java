package com.onlymaker.scorpio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties("amazon")
public class Amazon {
    private final List<MarketWebService> list = new ArrayList<>();
    public List<MarketWebService> getList() {
        return list;
    }
}

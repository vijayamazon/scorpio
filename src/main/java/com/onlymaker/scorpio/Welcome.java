package com.onlymaker.scorpio;

import com.onlymaker.scorpio.mws.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Welcome implements ApplicationRunner {
    @Value("${mws.mode}")
    String mode;
    @Autowired
    Configuration configuration;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("========== WELCOME ==========");
        System.out.println(String.format("%s %s startup in mode %s", configuration.getAppName(), configuration.getAppVersion(), mode));
    }
}

package com.onlymaker.scorpio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Welcome implements ApplicationRunner {
    @Value("${mws.mode}")
    String mode;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("System startup in mode: " + mode);
    }
}

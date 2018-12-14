package com.onlymaker.scorpio;

import com.onlymaker.scorpio.mws.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Welcome implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Welcome.class);
    @Value("${mws.mode}")
    String mode;
    @Autowired
    Configuration configuration;

    @Override
    public void run(ApplicationArguments args) {
        LOGGER.info("========== WELCOME ==========");
        LOGGER.info("{} {} starting ... mode {}", configuration.getAppName(), configuration.getAppVersion(), mode);
    }
}

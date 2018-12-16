package com.onlymaker.scorpio;

import com.onlymaker.scorpio.config.Amazon;
import com.onlymaker.scorpio.config.AppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Welcome implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Welcome.class);
    @Autowired
    AppInfo appInfo;
    @Autowired
    Amazon amazon;

    @Override
    public void run(ApplicationArguments args) {
        LOGGER.info("========== WELCOME ==========");
        LOGGER.info("Starting {}-{} ...", appInfo.getName(), appInfo.getVersion());
        LOGGER.info("Registering amazon store ... ");
        amazon.getList().forEach(mws -> LOGGER.info("Register store: {}", mws.getStore()));
        LOGGER.info("=============================");
    }
}

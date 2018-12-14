package com.onlymaker.scorpio.task;

import com.onlymaker.scorpio.data.AmazonEntry;
import com.onlymaker.scorpio.data.AmazonEntryRepository;
import com.onlymaker.scorpio.data.AmazonEntrySnapshot;
import com.onlymaker.scorpio.data.AmazonEntrySnapshotRepository;
import com.onlymaker.scorpio.mws.HtmlPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(prefix = "mws", name = "mode", havingValue = "central")
public class AmazonHtmlPageFetch {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonHtmlPageFetch.class);
    private static final long INIT_DELAY = 60000;
    private static final long FIX_DELAY = 8 * 3600000;
    private static final int FETCH_INTERVAL = 1;
    @Autowired
    HtmlPageService htmlPageService;
    @Autowired
    AmazonEntryRepository amazonEntryRepository;
    @Autowired
    AmazonEntrySnapshotRepository amazonEntrySnapshotRepository;

    @Scheduled(initialDelay = INIT_DELAY, fixedDelay = FIX_DELAY)
    public void exec() {
        LOGGER.info("html fetch task ...");
        String lastAsin = "";
        for (AmazonEntry entry : amazonEntryRepository.findAllByStatusOrderByAsin(AmazonEntry.STATUS_ENABLED)) {
            try {
                if (!Objects.equals(lastAsin, entry.getAsin())) {
                    AmazonEntrySnapshot snapshot = htmlPageService.parse(entry);
                    amazonEntrySnapshotRepository.save(snapshot);
                    lastAsin = entry.getAsin();
                    TimeUnit.SECONDS.sleep(FETCH_INTERVAL);
                }
            } catch (Throwable t) {
                LOGGER.info("html fetch error: {}", entry.getAsin(), t);
            }
        }
    }
}

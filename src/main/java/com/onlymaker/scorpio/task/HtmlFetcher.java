package com.onlymaker.scorpio.task;

import com.onlymaker.scorpio.data.AmazonEntry;
import com.onlymaker.scorpio.data.AmazonEntryRepository;
import com.onlymaker.scorpio.data.AmazonEntrySnapshotRepository;
import com.onlymaker.scorpio.mws.HtmlPageService;
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Date;
import java.util.Objects;

//@Service
public class HtmlFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlFetcher.class);
    private static final long SECOND_IN_MS = 1000;
    @Autowired
    HtmlPageService htmlPageService;
    @Autowired
    AmazonEntryRepository amazonEntryRepository;
    @Autowired
    AmazonEntrySnapshotRepository amazonEntrySnapshotRepository;

    @Scheduled(cron = "${fetcher.html}")
    public void everyday() {
        LOGGER.info("crawl html ...");
        fetchHtml();
    }

    private void fetchHtml() {
        String identity = "";
        Iterable<AmazonEntry> iterable = amazonEntryRepository.findByStatusOrderByMarketAscAsin(AmazonEntry.STATUS_ENABLED);
        for (AmazonEntry entry : iterable) {
            try {
                if (!Objects.equals(identity, entry.getMarket() + entry.getAsin())) {
                    amazonEntrySnapshotRepository.save(htmlPageService.parse(entry));
                }
                identity = entry.getMarket() + entry.getAsin();
                Thread.sleep(SECOND_IN_MS);
            } catch (Throwable t) {
                if (t instanceof HttpStatusException) {
                    HttpStatusException e = (HttpStatusException) t;
                    LOGGER.error("{} response code {}", e.getUrl(), e.getStatusCode());
                    if (e.getStatusCode() == 404) {
                        entry.setStatus(AmazonEntry.STATUS_DISABLED);
                        entry.setStopDate(new Date(System.currentTimeMillis()));
                        amazonEntryRepository.save(entry);
                    }
                }
                LOGGER.error("{} fetch html unexpected error: {}", entry.getMarket(), t.getMessage(), t);
            }
        }
    }
}
